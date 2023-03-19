package fr.upec.e2ee.ui.message1;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.security.GeneralSecurityException;

import fr.upec.e2ee.E2EE;
import fr.upec.e2ee.R;
import fr.upec.e2ee.Tools;
import fr.upec.e2ee.databinding.FragmentMessage1Binding;
import fr.upec.e2ee.mystate.MyState;
import fr.upec.e2ee.protocol.Communication;
import fr.upec.e2ee.protocol.Message1;
import fr.upec.e2ee.protocol.SecretBuild;
import fr.upec.e2ee.ui.message2.Message2Fragment;

public class Message1Fragment extends Fragment {
    private MyState myState;
    private Message1 myMessage1 = null;
    private SecretBuild mySecretBuild = null;
    private @NonNull FragmentMessage1Binding binding;

    public static Message1Fragment newInstance() {
        return new Message1Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TransitionInflater transitionInflater = TransitionInflater.from(requireContext());
        setExitTransition(transitionInflater.inflateTransition(R.transition.fade));
        setEnterTransition(transitionInflater.inflateTransition(R.transition.slide_right));

        try {
            myState = MyState.load();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = FragmentMessage1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button generateMessage1Button = binding.generateM1Button;
        final Button resetMessage1Button = binding.resetM1Button;
        final Button shareMessage1Button = binding.shareM1Button;
        final Button copyMessage1Button = binding.copyM1Button;
        final ImageButton genQRCMessage1Button = binding.qrcM1Button;
        final Button pasteMessage1Button = binding.pasteM1Button;
        EditText otherMessage1Text = binding.otherM1Textzone;
        final Button validateMessage1Button = binding.validateM1Button;

        //Generate Message 1
        generateMessage1Button.setOnClickListener(view -> {
            try {
                myMessage1 = new Message1(Tools.getCurrentTime(), myState.getMyNonce());
                myState.incrementMyNonce();
                myState.save();

                generateMessage1Button.setEnabled(false);
                resetMessage1Button.setEnabled(true);
                shareMessage1Button.setEnabled(true);
                copyMessage1Button.setEnabled(true);
                genQRCMessage1Button.setEnabled(true);
                genQRCMessage1Button.setClickable(true);
                pasteMessage1Button.setEnabled(true);
                otherMessage1Text.setEnabled(true);
                validateMessage1Button.setEnabled(true);

                otherMessage1Text.setText("");

                Toast.makeText(E2EE.getContext(), R.string.m1_generated, Toast.LENGTH_SHORT).show();
            } catch (GeneralSecurityException | IOException e) {
                Toast.makeText(E2EE.getContext(), R.string.err_unex, Toast.LENGTH_SHORT).show();
            }
        });

        //Reset Message 1
        resetMessage1Button.setOnClickListener(view -> {
            otherMessage1Text.setText("");

            generateMessage1Button.setEnabled(true);
            resetMessage1Button.setEnabled(false);
            shareMessage1Button.setEnabled(false);
            copyMessage1Button.setEnabled(false);
            genQRCMessage1Button.setEnabled(false);
            genQRCMessage1Button.setClickable(false);
            pasteMessage1Button.setEnabled(false);
            otherMessage1Text.setEnabled(false);
            validateMessage1Button.setEnabled(false);
        });

        //Share Message 1
        shareMessage1Button.setOnClickListener(view -> startActivity(Tools.shareIntent(Tools.toBase64(myMessage1.toBytes()))));

        //Copy Message 1
        copyMessage1Button.setOnClickListener(view -> Tools.copyToClipboard("Message1", Tools.toBase64(myMessage1.toBytes())));

        //Generate QRCode button
        genQRCMessage1Button.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            final View qrCodeView = layoutInflater.inflate(R.layout.qrcode_alertdialog, null);
            final ImageView qrcode = qrCodeView.findViewById(R.id.qrc_view);

            try {
                Bitmap bitmap = Tools.generateQRCode(Tools.toBase64(myMessage1.toBytes()));
                qrcode.setImageBitmap(bitmap);
                builder.setView(qrCodeView)
                        .setTitle(R.string.qrc_m1)
                        .show();
            } catch (NullPointerException ignored) {
            }
        });

        //Paste Message 1
        pasteMessage1Button.setOnClickListener(view -> {
            String paste = Tools.pasteFromClipboard();
            otherMessage1Text.setText(paste);
        });

        //Validate Message 1
        validateMessage1Button.setOnClickListener(view -> {
            try {
                mySecretBuild = Communication.handleMessage1(myMessage1, otherMessage1Text.getText().toString());
                Fragment fragment = Message2Fragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putByteArray("SC", mySecretBuild.toBytesWithSymKey());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment_content_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } catch (GeneralSecurityException e) {
                Toast.makeText(E2EE.getContext(), R.string.err_unex, Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(E2EE.getContext(), getResources().getText(R.string.err_msg).toString() + e.getMessage(), Toast.LENGTH_LONG).show();
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