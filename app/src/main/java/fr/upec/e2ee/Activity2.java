package fr.upec.e2ee;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import fr.upec.e2ee.mystate.MyState;

public class Activity2 extends AppCompatActivity {
    final String KEY_CARD_COUNT = "cardCount";
    final String KEY_CARD_CONTENTS = "cardContents";
    Button add;
    AlertDialog dialog;
    LinearLayout layout;
    int cardCount = 0;
    MyState myState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            myState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setContentView(R.layout.activity2);
        add = findViewById(R.id.add);
        add.setBackgroundColor(Color.BLUE);
        layout = findViewById(R.id.container);

        buildDialog();
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        if (savedInstanceState != null) {
            cardCount = savedInstanceState.getInt(KEY_CARD_COUNT);
            ArrayList<String> cardContents = savedInstanceState.getStringArrayList(KEY_CARD_CONTENTS);

            for (String content : cardContents) {
                try {
                    addCard(content);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            myState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cardCount = savedInstanceState.getInt("cardCount");
        ArrayList<String> cardContents = savedInstanceState.getStringArrayList("cardContents");
        Log.d("Activity2", "onRestoreInstanceState: cardCount=" + cardCount);

        if (cardContents != null) {
            for (String content : cardContents) {
                try {
                    addCard(content);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        EditText name = view.findViewById(R.id.name);
        builder.setView(view);
        builder.setTitle("Enter name").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (myState == null) {
                    System.out.println("fuckkkk");

                } else {
                    System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooo");
                }
                myState.getMyDirectory().addPerson(name.getText().toString(), Tools.toBytes(pasteClipBoard()));
                try {
                    addCard(name.getText().toString());
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog = builder.create();
    }

    private void addCard(String name) throws GeneralSecurityException, IOException {
        myState.load();
        View view = getLayoutInflater().inflate(R.layout.card, null);

        TextView nameView = view.findViewById(R.id.name);
        Button delete = view.findViewById((R.id.delete));
        delete.setBackgroundColor(Color.RED);
        nameView.setText(name);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(view);
                Toast.makeText(getBaseContext(), "contacts supprimé", Toast.LENGTH_SHORT).show();
            }
        });

        layout.addView((view));
        cardCount++;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        try {
            myState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Stocker les données importantes de l'activité dans l'objet Bundle
        savedInstanceState.putInt("cardCount", layout.getChildCount());

        ArrayList<String> cardContents = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View cardView = layout.getChildAt(i);
            TextView cardTextView = cardView.findViewById(R.id.name);
            cardContents.add(cardTextView.getText().toString());
        }
        savedInstanceState.putStringArrayList("cardContents", cardContents);
        Log.d("Activity2", "onSaveInstanceState: cardCount=" + cardCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            myState.load();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("Activity2", "onDestroy()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restaurer l'état de l'activité
        onRestoreInstanceState(new Bundle());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Sauvegarder l'état de l'activité lorsqu'elle est mise en pause
        onSaveInstanceState(new Bundle());
    }

    public void onBackPressed() {
        super.onBackPressed();
        onSaveInstanceState(new Bundle());
    }

    public String pasteClipBoard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            return clipData.getItemAt(0).getText().toString();

        }
        return null;

    }


}

