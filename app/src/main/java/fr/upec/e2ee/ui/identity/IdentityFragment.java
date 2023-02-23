package fr.upec.e2ee.ui.identity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private @NonNull FragmentIdentityBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyState myState = null;
        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        IdentityViewModel identityViewModel =
                new ViewModelProvider(this).get(IdentityViewModel.class);

        identityViewModel.setText(Tools.toBase64(myState.getMyPublicKey().getEncoded()));

        binding = FragmentIdentityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textIdentity;
        identityViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
