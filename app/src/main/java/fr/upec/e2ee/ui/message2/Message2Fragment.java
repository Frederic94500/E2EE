package fr.upec.e2ee.ui.message2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.databinding.FragmentMessage2Binding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.SecretBuild;

public class Message2Fragment extends Fragment {
    private MyState myState;
    private SecretBuild mySecretBuild;
    private @NonNull FragmentMessage2Binding binding;

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