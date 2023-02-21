package fr.upec.e2ee.ui.identity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import fr.upec.e2ee.databinding.FragmentIdentityBinding;
import fr.upec.e2ee.databinding.FragmentSlideshowBinding;
import fr.upec.e2ee.ui.slideshow.SlideshowViewModel;

public class IdentityFragment extends Fragment {
    private @NonNull FragmentIdentityBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        IdentityViewModel identityViewModel =
                new ViewModelProvider(this).get(IdentityViewModel.class);

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
