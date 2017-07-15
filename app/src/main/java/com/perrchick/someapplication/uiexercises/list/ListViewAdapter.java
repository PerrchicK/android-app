package com.perrchick.someapplication.uiexercises.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.data.SomePojo;

import java.util.ArrayList;

/**
 * Created by perrchick on 14/06/2017.
 */

class ListViewAdapter extends RecyclerView.Adapter {

    private final ArrayList<SomePojo> dataList;

    ListViewAdapter(ArrayList<SomePojo> list) {
        this.dataList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, null);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListViewHolder listViewHolder = (ListViewHolder) holder;
        listViewHolder.configure(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
