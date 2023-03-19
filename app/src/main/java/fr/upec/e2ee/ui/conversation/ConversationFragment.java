package fr.upec.e2ee.ui.conversation;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.E2EE;
import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentConversationBinding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Cipher;
import fr.upec.e2ee.protocol.Conversation;
import fr.upec.e2ee.ui.home.HomeFragment;

public class ConversationFragment extends Fragment {
    private MyState myState;
    private Conversation conversation;
    private @NonNull FragmentConversationBinding binding;

    public static ConversationFragment newInstance() {
        return new ConversationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_right));

        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentConversationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle bundle = this.getArguments();
        assert bundle != null;
        int index = bundle.getInt("Conv");
        conversation = myState.getMyConversations().getConversation(index);

        final ImageButton copyButton = binding.copyConvButton;
        final ImageButton pasteButton = binding.pasteConvButton;
        final ImageButton cipherButton = binding.cipherConvButton;
        final ImageButton decipherButton = binding.decipherConvButton;
        final ImageButton shareButton = binding.shareConvButton;
        final ImageButton deleteButton = binding.deleteConvButton;
        final EditText messageTextZone = binding.message;

        //Copy
        copyButton.setOnClickListener(view -> {
            if (!messageTextZone.getText().toString().isEmpty()) {
                Tools.copyToClipboard("Message", messageTextZone.getText().toString());
            } else {
                messageTextZone.setError(getResources().getText(R.string.conv_empty_message).toString());
            }
        });

        //Paste
        pasteButton.setOnClickListener(view -> {
            String message = Tools.pasteFromClipboard();
            messageTextZone.setText(message);
        });

        //Cipher
        cipherButton.setOnClickListener(view -> {
            if (!messageTextZone.getText().toString().isEmpty()) {
                try {
                    String cipheredMessage = Tools.toBase64(Cipher.cipher(Tools.toSecretKey(conversation.getSecretKey()), messageTextZone.getText().toString().getBytes(StandardCharsets.UTF_8)));
                    messageTextZone.setText(cipheredMessage);
                } catch (GeneralSecurityException e) {
                    Toast.makeText(E2EE.getContext(), R.string.err_unex, Toast.LENGTH_SHORT).show();
                }
            } else {
                messageTextZone.setError(getResources().getText(R.string.conv_empty_message).toString());
            }
        });

        //Decipher
        decipherButton.setOnClickListener(view -> {
            if (!messageTextZone.getText().toString().isEmpty()) {
                try {
                    String decipheredMessage = new String(Cipher.decipher(Tools.toSecretKey(conversation.getSecretKey()), Tools.toBytes(messageTextZone.getText().toString())));
                    messageTextZone.setText(decipheredMessage);
                } catch (GeneralSecurityException e) {
                    Toast.makeText(E2EE.getContext(), R.string.err_unex, Toast.LENGTH_SHORT).show();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(E2EE.getContext(), R.string.err_msg_wrong, Toast.LENGTH_SHORT).show();
                }
            } else {
                messageTextZone.setError(getResources().getText(R.string.conv_empty_message).toString());
            }
        });

        //Share
        shareButton.setOnClickListener(view -> {
            if (!messageTextZone.getText().toString().isEmpty()) {
                Tools.shareIntent(messageTextZone.getText().toString());
            } else {
                messageTextZone.setError(getResources().getText(R.string.conv_empty_message).toString());
            }
        });

        //Delete
        deleteButton.setOnClickListener(view -> {
            myState.getMyConversations().deleteConversation(conversation);
            Toast.makeText(E2EE.getContext(), R.string.conv_deleted, Toast.LENGTH_SHORT).show();

            try {
                myState.save();
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            Fragment fragment = HomeFragment.newInstance();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .commit();
        });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}