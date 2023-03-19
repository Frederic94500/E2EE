package fr.upec.e2ee.ui.about;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import fr.upec.e2ee.R;
import fr.upec.e2ee.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        var res = getResources();
        String text = res.getString(R.string.ab_title) + "\n\n" +
                res.getString(R.string.ab_home) + "\n" +
                res.getString(R.string.ab_directory) + "\n" +
                res.getString(R.string.ab_id) + "\n" +
                res.getString(R.string.ab_conv) + "\n\n" +
                res.getString(R.string.ab_created);
        textView.setText(text);
        Button old = binding.button;
        old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Créer un intent pour lancer la nouvelle activité
                Intent intent = new Intent(getActivity(), Activity2.class);

                // Démarrer l'activité
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
