package fr.upec.e2ee.ui.gallery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
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
    AlertDialog dialog;
    private EditText username;
    private EditText pubKey;
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

        final Button buttonAdd = binding.add;
        final Button buttonDelete = binding.delete;
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.contacts_dialog, null);
                username = view.findViewById(R.id.username);
                pubKey = view.findViewById(R.id.pubKey);
                builder.setView(view)
                        .setTitle("Add Contacts")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Récupérer les valeurs des EditText
                                String text_username = username.getText().toString();
                                String text_pubKey = pubKey.getText().toString();
                                mystate.getMyDirectory().addPerson(text_username, Tools.toBytes(text_pubKey));
                                // Faire quelque chose avec les valeurs récupérées
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Annuler le Dialog
                                dialogInterface.cancel();
                            }
                        });
                builder.create();
                builder.show();
                // Retourner le Dialog créé
                // return builder.create();
            }
        });
        //buttonAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), Activity2.class)));
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