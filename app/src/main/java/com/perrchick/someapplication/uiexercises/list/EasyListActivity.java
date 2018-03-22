package com.perrchick.someapplication.uiexercises.list;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.data.SomePojo;
import com.perrchick.someapplication.ui.EasyRecyclerView;

import java.util.ArrayList;

public class EasyListActivity extends AppCompatActivity {

    private ArrayList<SomePojo> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_list);

        ViewGroup mainLayout = findViewById(R.id.main_layout);
        list = new ArrayList<>();
        EasyRecyclerView<SomePojo> easyRecyclerView = new EasyRecyclerView<>(this, new EasyRecyclerView.CellsFactory<SomePojo>() {
            @Override
            public EasyRecyclerView.CellHolder<SomePojo> create(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, null);
                return new Cell(view);
            }
        });

        for (int i = 0; i < 1000; i++) {
            SomePojo obj = new SomePojo();
            obj.setName("obj" + i);
            list.add(obj);
        }
        mainLayout.addView(easyRecyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        easyRecyclerView.setData(list);
    }

    private class Cell extends EasyRecyclerView.CellHolder<SomePojo> {
        private final TextView textView;

        public Cell(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }

        @Override
        public void configure(SomePojo data) {
            super.configure(data);
            textView.setText(data.getName());
        }

        @Override
        public void prepareForReuse() {
            textView.setText("");
        }
    }
}
