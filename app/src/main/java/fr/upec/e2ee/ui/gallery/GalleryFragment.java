package fr.upec.e2ee.ui.gallery;

import android.content.Intent;
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

import fr.upec.e2ee.Activity2;
import fr.upec.e2ee.databinding.FragmentGalleryBinding;
import fr.upec.e2ee.mystate.MyState;

public class GalleryFragment extends Fragment {
    private MyState myState;
    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textGallery;
        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final Button buttonview = binding.afficher;
        buttonview.setOnClickListener(v -> startActivity(new Intent(getContext(), Activity2.class)));

        final Button dummy = binding.dummyButton;
        dummy.setOnClickListener(view -> {
            myState.getMyDirectory().addPerson("me", myState.getMyPublicKey().getEncoded());
            try {
                myState.save();
            } catch (IOException | GeneralSecurityException e) {
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