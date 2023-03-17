package fr.upec.e2ee;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class Contacts extends AppCompatActivity {
    ListView l;
    String tutorials[]
            = {"Algorithms", "Data Structures",
            "Languages", "Interview Corner",
            "GATE", "ISRO CS",
            "UGC NET CS", "CS Subjects",
            "Web Technologies"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_file);
        l = findViewById(R.id.list);
        ArrayAdapter<String> arr;
        arr = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, tutorials);
        l.setAdapter(arr);
    }
}
