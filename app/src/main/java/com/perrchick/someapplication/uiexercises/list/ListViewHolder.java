package com.perrchick.someapplication.uiexercises.list;

import android.view.View;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.data.SomePojo;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by perrchick on 14/06/2017.
 */

class ListViewHolder extends RecyclerView.ViewHolder {

    private TextView textView;

    public ListViewHolder(View view) {
        super(view);
        textView = (TextView) view.findViewById(R.id.text_view);
    }

    public void configure(SomePojo somePojo) {
        textView.setText(somePojo.getName());
    }
}
