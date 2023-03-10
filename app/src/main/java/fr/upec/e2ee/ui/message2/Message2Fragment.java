package fr.upec.e2ee.ui.message2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;

import fr.upec.e2ee.E2EE;
import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentMessage2Binding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Communication;
import fr.upec.e2ee.protocol.Conversation;
import fr.upec.e2ee.protocol.SecretBuild;
import fr.upec.e2ee.ui.home.HomeFragment;

public class Message2Fragment extends Fragment {
    private MyState myState;
    private SecretBuild mySecretBuild;
    private Button generateMessage2Button;
    private Button resetMessage2Button;
    private Button shareMessage2Button;
    private Button copyMessage2Button;
    private Button pasteMessage2Button;
    private EditText otherMessage2Text;
    private Button validateMessage2Text;
    private Executor executor;
    private androidx.biometric.BiometricPrompt biometricPrompt;
    private BiometricPrompt.AuthenticationCallback biometricAuthCallback;
    private BiometricPrompt.PromptInfo promptInfo;
    private @NonNull FragmentMessage2Binding binding;
    private String myMessage2;

    public static Message2Fragment newInstance() {
        return new Message2Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentMessage2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle bundle = this.getArguments();
        assert bundle != null;
        byte[] bytesSC = bundle.getByteArray("SC");
        mySecretBuild = new SecretBuild(null, bytesSC);

        generateMessage2Button = binding.generateM2Button;
        resetMessage2Button = binding.resetM2Button;
        shareMessage2Button = binding.shareM2Button;
        copyMessage2Button = binding.copyM2Button;
        pasteMessage2Button = binding.pasteM2Button;
        otherMessage2Text = binding.otherM2Text;
        validateMessage2Text = binding.validateM2Button;

        executor = ContextCompat.getMainExecutor(E2EE.getContext());
        biometricAuthCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(E2EE.getContext(), "Error on authentication!", Toast.LENGTH_SHORT).show();
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                try {
                    myMessage2 = Communication.createMessage2(myState.getMyPrivateKey(), mySecretBuild);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }

                generateMessage2Button.setEnabled(false);
                resetMessage2Button.setEnabled(true);
                shareMessage2Button.setEnabled(true);
                copyMessage2Button.setEnabled(true);
                pasteMessage2Button.setEnabled(true);
                otherMessage2Text.setEnabled(true);
                validateMessage2Text.setEnabled(true);

                Toast.makeText(E2EE.getContext(), "Message 2 generated!", Toast.LENGTH_SHORT).show();

                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(E2EE.getContext(), "Authentication cancelled", Toast.LENGTH_SHORT).show();
                super.onAuthenticationFailed();
            }
        };
        biometricPrompt = new androidx.biometric.BiometricPrompt(this, executor, biometricAuthCallback);
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("E2EE Biometric for signing a message")
                .setNegativeButtonText("Cancel")
                .build();

        //Generate Message 2
        generateMessage2Button.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });

        //Reset Message 2
        resetMessage2Button.setOnClickListener(view -> {
            biometricPrompt.cancelAuthentication();
            otherMessage2Text.setText("");

            generateMessage2Button.setEnabled(true);
            resetMessage2Button.setEnabled(false);
            shareMessage2Button.setEnabled(false);
            copyMessage2Button.setEnabled(false);
            pasteMessage2Button.setEnabled(false);
            otherMessage2Text.setEnabled(false);
            validateMessage2Text.setEnabled(false);
        });

        //Share Message 2
        shareMessage2Button.setOnClickListener(view -> startActivity(Tools.shareIntent((myMessage2))));

        //Copy Message 2
        copyMessage2Button.setOnClickListener(view -> Tools.copyToClipboard("Message2", myMessage2));

        //Paste Message 2
        pasteMessage2Button.setOnClickListener(view -> {
            String paste = Tools.pasteFromClipboard();
            otherMessage2Text.setText(paste);
        });

        //Validate Message 2
        validateMessage2Text.setOnClickListener(view -> {
            try {
                Conversation conversation = Communication.handleMessage2(myState.getMyDirectory(), mySecretBuild, otherMessage2Text.getText().toString());
                myState.addAConversation(conversation);
                myState.save();

                Toast.makeText(E2EE.getContext(), "Conversation created!", Toast.LENGTH_SHORT).show();
                Fragment fragment = HomeFragment.newInstance();
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, fragment)
                        .addToBackStack(null)
                        .commit();
            } catch (NoSuchElementException e) {
                Toast.makeText(E2EE.getContext(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            myState.save();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        binding = null;
    }
}