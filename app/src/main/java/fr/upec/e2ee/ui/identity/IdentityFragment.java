package fr.upec.e2ee.ui.identity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.E2EE;
import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentIdentityBinding;
import fr.upec.e2ee.mystate.MyState;

public class IdentityFragment extends Fragment {
    MyState myState;
    private @NonNull FragmentIdentityBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentIdentityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textIdentity;
        textView.setOnClickListener(view -> {
            textView.setText(Tools.toBase64(myState.getMyPublicKey().getEncoded()));
        });

        //Share button
        final Button shareButton = binding.shareButton;
        shareButton.setOnClickListener(view -> startActivity(Tools.shareIntent(Tools.toPEMFormat(myState.getMyPublicKey().getEncoded()))));

        //Copy button
        final Button copyButton = binding.copyButton;
        copyButton.setOnClickListener(view -> Tools.copyToClipboard("PubKey", Tools.toPEMFormat(myState.getMyPublicKey().getEncoded())));

        //Replace button
        final Button replaceButton = binding.replaceButton;
        replaceButton.setOnClickListener(view -> {
            try {
                myState.replaceMyKeyPair();
                myState.save();
                Toast.makeText(E2EE.getContext(), R.string.id_replaced, Toast.LENGTH_SHORT).show();
                textView.setText(Tools.toBase64(myState.getMyPublicKey().getEncoded()));
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        //Generate QRCode button
        final ImageButton genQRC = binding.idGenQrc;
        genQRC.setBackgroundColor(Color.rgb(148, 0, 211));
        genQRC.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            final View qrCodeView = layoutInflater.inflate(R.layout.qrcode_alertdialog, null);
            final ImageView qrcode = qrCodeView.findViewById(R.id.qrc_view);

            Bitmap bitmap = Tools.generateQRCode(Tools.toBase64(myState.getMyPublicKey().getEncoded()));
            qrcode.setImageBitmap(bitmap);
            builder.setView(qrCodeView)
                    .setTitle(R.string.qrc_pubkey)
                    .show();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
