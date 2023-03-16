package fr.upec.e2ee.ui.gallery;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    MyState myState;
    AlertDialog dialog;
    private EditText username;
    private EditText pubKey;
    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        try {
            myState = MyState.load();
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
        final TextView textDirectory = binding.testDirectory;
        final Button buttonAdd = binding.add;
        final Button buttonDelete = binding.delete;
        String random = myState.getMyDirectory().getSize() + "";
        textDirectory.setText(random);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // raffraichir la view;;;;;
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
                                String textUsername = username.getText().toString();
                                String textPubKey = pubKey.getText().toString();
                                if (textUsername.isEmpty() || textPubKey.isEmpty()) {
                                    Toast.makeText(getActivity(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                                    // Ne pas fermer le dialogue
                                    return;
                                } else {
                                    if (Tools.isECPubKey(Tools.toBytes(textPubKey))) {
                                        myState.getMyDirectory().addPerson(textUsername, Tools.toBytes(textPubKey));
                                        String random2 = myState.getMyDirectory().getSize() + "";
                                        textDirectory.setText(random2);
                                        try {
                                            myState.save();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        } catch (GeneralSecurityException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "Erreur clé public invalide", Toast.LENGTH_SHORT).show();
                                        return;

                                    }

                                }

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
        System.out.println("debug readfile to display directory in a listview ");
        l = root.findViewById(R.id.list);
        ArrayAdapter<String> arr;

        //arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, contacts);
        try {


            arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, convertHashMapToList(myState.getMyDirectory().readFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        l.setAdapter(arr);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedContact = (String) parent.getItemAtPosition(position);
                byte[] publicKey = myState.getMyDirectory().getPerson(selectedContact);
                String publicKeyString = Tools.toBase64(publicKey);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Public Key:\n\n" + publicKeyString)
                        .setTitle("Contact Information")
                        .setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        l.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Récupérer l'élément sélectionné
                String selected = (String) parent.getItemAtPosition(position);

                // Supprimer l'élément de la liste et de l'ArrayAdapter
                arr.remove(selected);
                myState.getMyDirectory().deletePerson(selected);

                // Mettre à jour la ListView
                arr.notifyDataSetChanged();

                // Afficher un message pour confirmer la suppression
                Toast.makeText(getActivity(), "Supprimé : " + selected, Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            myState.save();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        binding = null;
    }

    public ArrayList<String> convertHashMapToList(HashMap<String, byte[]> map) {
        System.out.println("debug converthashmap  to display contacts");
        System.out.println(">>>" + map.keySet() + "map keyset                                   map keyset ");
        ArrayList<String> list = new ArrayList<>(map.keySet());
        System.out.println(list);
        return list;
    }


}