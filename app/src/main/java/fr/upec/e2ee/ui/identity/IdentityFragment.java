package fr.upec.e2ee.ui.identity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentIdentityBinding;
import fr.upec.e2ee.mystate.MyState;

public class IdentityFragment extends Fragment {
    MyState mystate;
    private @NonNull FragmentIdentityBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            mystate = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        IdentityViewModel identityViewModel =
                new ViewModelProvider(this).get(IdentityViewModel.class);

        binding = FragmentIdentityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Show Public Key
        final TextView textView = binding.textIdentity;
        identityViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        textView.setOnClickListener(view -> {
            textView.setText(Tools.toBase64(mystate.getMyPublicKey().getEncoded()));
        });

        //Share button
        final Button shareButton = binding.shareButton;
        shareButton.setOnClickListener(view -> startActivity(Tools.shareIntent(Tools.toPEMFormat(mystate.getMyPublicKey().getEncoded()))));

        //Copy button
        final Button copyButton = binding.copyButton;
        copyButton.setOnClickListener(view -> Tools.copyToClipboard("PubKey", Tools.toPEMFormat(mystate.getMyPublicKey().getEncoded())));

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
