package fr.upec.e2ee.ui.message1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentMessage1Binding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Communication;
import fr.upec.e2ee.protocol.Message1;
import fr.upec.e2ee.protocol.SecretBuild;

public class Message1Fragment extends Fragment {
    private MyState mystate;
    private Message1 myMessage1 = null;
    private SecretBuild mySecretBuild = null;
    private @NonNull FragmentMessage1Binding binding;

    public static Message1Fragment newInstance() {
        return new Message1Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            mystate = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentMessage1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button generateMessage1Button = binding.generateM1Button;
        final Button resetMessage1Button = binding.resetM1Button;
        final Button shareMessage1Button = binding.shareM1Button;
        final Button copyMessage1Button = binding.copyM1Button;
        final Button pasteMessage1Button = binding.pasteM1Button;
        EditText otherMessage1Text = binding.otherM1Textzone;
        final Button validateMessage1Button = binding.validateM1Button;

        generateMessage1Button.setOnClickListener(view -> {
            try {
                myMessage1 = new Message1(Tools.getCurrentTime(), mystate.getMyNonce());
                mystate.incrementMyNonce();
                mystate.save();
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }

            generateMessage1Button.setEnabled(false);
            resetMessage1Button.setEnabled(true);
            shareMessage1Button.setEnabled(true);
            copyMessage1Button.setEnabled(true);
            pasteMessage1Button.setEnabled(true);
            otherMessage1Text.setEnabled(true);
            validateMessage1Button.setEnabled(true);
        });

        resetMessage1Button.setOnClickListener(view -> {
            otherMessage1Text.setText("");

            generateMessage1Button.setEnabled(true);
            resetMessage1Button.setEnabled(false);
            shareMessage1Button.setEnabled(false);
            copyMessage1Button.setEnabled(false);
            pasteMessage1Button.setEnabled(false);
            otherMessage1Text.setEnabled(false);
            validateMessage1Button.setEnabled(false);
        });

        shareMessage1Button.setOnClickListener(view -> startActivity(Tools.shareIntent(Tools.toBase64(myMessage1.toBytes()))));

        copyMessage1Button.setOnClickListener(view -> Tools.copyToClipboard("Message1", Tools.toBase64(myMessage1.toBytes())));

        pasteMessage1Button.setOnClickListener(view -> {
            String paste = Tools.pasteFromClipboard();
            otherMessage1Text.setText(paste);
        });

        validateMessage1Button.setOnClickListener(view -> {
            try {
                mySecretBuild = Communication.handleMessage1(myMessage1, otherMessage1Text.getText().toString());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mystate.save();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        binding = null;
    }
}