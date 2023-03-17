package fr.upec.e2ee.ui.directory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentDirectoryBinding;
import fr.upec.e2ee.mystate.MyState;

public class DirectoryFragment extends Fragment {
    ListView l;
    MyState myState;
    private FragmentDirectoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentDirectoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final FloatingActionButton buttonAdd = binding.fabAddContact;

        buttonAdd.setOnClickListener(v -> {
            View contactDialogView = inflater.inflate(R.layout.contacts_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(contactDialogView)
                    .setTitle(R.string.cont_add)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            final EditText username = contactDialogView.findViewById(R.id.username);
            final EditText pubKey = contactDialogView.findViewById(R.id.pubKey);

            dialog.setOnShowListener(dialog1 -> {
                final Button buttonOK = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonOK.setOnClickListener(v1 -> {
                    String textUsername = username.getText().toString();
                    String textPubKey = pubKey.getText().toString();
                    String parsedPubKey = null;
                    try {
                        parsedPubKey = Tools.keyParser(textPubKey);
                    } catch (IllegalArgumentException e) {
                        pubKey.setError(getResources().getText(R.string.err_wrong_pubkey));
                    }
                    if (textUsername.isEmpty()) {
                        username.setError(getResources().getText(R.string.err_empty_name));
                    } else if (textPubKey.isEmpty()) {
                        pubKey.setError(getResources().getText(R.string.err_empty_pubkey));
                    } else if (parsedPubKey == null || !Tools.isECPubKey(Tools.toBytes(Tools.keyParser(textPubKey)))) {
                        pubKey.setError(getResources().getText(R.string.err_wrong_pubkey));
                    } else {
                        myState.getMyDirectory().addPerson(textUsername, Tools.toBytes(Tools.keyParser(textPubKey)));
                        try {
                            myState.save();
                            dialog.dismiss();
                        } catch (IOException | GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                final Button buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonCancel.setOnClickListener(v2 -> {
                    dialog.cancel();
                });
            });

            dialog.show();
        });

        System.out.println("debug readfile to display directory in a listview ");
        l = root.findViewById(R.id.list);
        ArrayAdapter<String> arr;

        //arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, contacts);
        try {
            arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, convertHashMapToList(myState.getMyDirectory().readFile()));
        } catch (IOException | GeneralSecurityException e) {
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
        } catch (IOException | GeneralSecurityException e) {
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
