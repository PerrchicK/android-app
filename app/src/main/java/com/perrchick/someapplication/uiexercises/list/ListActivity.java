package com.perrchick.someapplication.uiexercises.list;

import android.os.Bundle;
import com.perrchick.someapplication.R;
import com.perrchick.someapplication.data.SomePojo;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListActivity extends AppCompatActivity {

    private ArrayList<SomePojo> data;
    private RecyclerView listView;
    private ListViewAdapter listViewAdapter;
    private AppCompatMultiAutoCompleteTextView multiAutoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        createData();

        //Volley;

        listView = (RecyclerView) findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        multiAutoCompleteTextView = findViewById(R.id.autocomplete_list);
    }

    private void createData() {
        data = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            SomePojo obj = new SomePojo();
            obj.setName("obj" + i);
            data.add(obj);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        listViewAdapter = new ListViewAdapter(data);
        listView.setAdapter(listViewAdapter);
    }
}
