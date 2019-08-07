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

    /**
     * Called only when the adapter creates this view holder.
     */
    public ListViewHolder(View view) {
        super(view);

        textView = (TextView) view.findViewById(R.id.text_view);
    }

    /**
     * Used whenever the adapter is going to use the view of this holder.
     */
    public void configure(SomePojo somePojo) {
        textView.setText(somePojo.getName());
    }

    /**
     * Used for cleanup, called whenever the adapter gets an event that the view of this holder is no longer necessary.
     */
    public void prepareForReuse() {
        textView.setText("");
    }
}
