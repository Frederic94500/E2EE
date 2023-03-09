package fr.upec.e2ee.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.upec.e2ee.R;
import fr.upec.e2ee.databinding.FragmentGalleryBinding;
import fr.upec.e2ee.mystate.MyState;

public class GalleryFragment extends Fragment {
    ListView l;
    String tutorials[]
            = {"Algorithms", "Data Structures",
            "Languages", "Interview Corner",
            "GATE", "ISRO CS",
            "UGC NET CS", "CS Subjects",
            "Web Technologies"};
    MyState mystate;
    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        try {
            mystate = MyState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textGallery;
        //final TextView textView = binding.textGallery;
        //galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //final Button buttonview = binding.afficher;
        //buttonview.setOnClickListener(v -> startActivity(new Intent(getContext(), Activity2.class)));
        //buttonview.setOnClickListener(v -> startActivity(new Intent(getContext(), Contacts.class)));
        l = root.findViewById(R.id.list);
        ArrayAdapter<String> arr;

        try {
            arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, convertHashMapToList(mystate.getMyDirectory().readFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        l.setAdapter(arr);
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mystate = MyState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        binding = null;
    }

    public ArrayList<String> convertHashMapToList(HashMap<String, byte[]> map) {
        ArrayList<String> list = new ArrayList<>(map.keySet());
        return list;
    }
}