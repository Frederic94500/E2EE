package fr.upec.e2ee.ui.directory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentDirectoryBinding;
import fr.upec.e2ee.mystate.MyState;

public class DirectoryFragment extends Fragment {
    ListView listView;
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

        ArrayAdapter<String> arr = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myState.getMyDirectory().getListName());
        listView = root.findViewById(R.id.list);
        listView.setAdapter(arr);

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
                //Button OK
                final Button buttonOK = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
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
                    } else if (myState.getMyDirectory().isInDirectory(textUsername)) {
                        username.setError(getResources().getText(R.string.err_already_name));
                    } else if (parsedPubKey == null || !Tools.isECPubKey(Tools.toBytes(Tools.keyParser(textPubKey)))) {
                        pubKey.setError(getResources().getText(R.string.err_wrong_pubkey));
                    } else {
                        myState.getMyDirectory().addPerson(textUsername, Tools.toBytes(Tools.keyParser(textPubKey)));
                        try {
                            myState.save();
                            Toast.makeText(getContext(), R.string.cont_added, Toast.LENGTH_SHORT).show();
                            ArrayAdapter<String> array = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myState.getMyDirectory().getListName());
                            listView.setAdapter(array);
                            dialog.dismiss();
                        } catch (IOException | GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                //Button Cancel
                final Button buttonCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonCancel.setOnClickListener(v2 -> {
                    dialog.cancel();
                });
            });

            dialog.show();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.popup_info) {
                    showPerson((String) parent.getItemAtPosition(position));
                } else if (item.getItemId() == R.id.popup_delete) {
                    deletePerson((String) parent.getItemAtPosition(position));
                }
                return true;
            });
            popupMenu.show();

            return false;
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

    private void showPerson(String name) {
        byte[] publicKey = myState.getMyDirectory().getPerson(name);
        String publicKeyString = Tools.toBase64(publicKey);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.cont_info)
                .setMessage(getResources().getText(R.string.pubKey) + ":\n\n" + publicKeyString)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePerson(String name) {
        // Supprimer l'element
        myState.getMyDirectory().deletePerson(name);

        // Mettre à jour la ListView
        listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myState.getMyDirectory().getListName()));

        // Afficher un message pour confirmer la suppression
        Toast.makeText(getActivity(), getResources().getString(R.string.deleted) + " " + name, Toast.LENGTH_SHORT).show();

        try {
            myState.save();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
