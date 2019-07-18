package com.perrchick.someapplication.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Perry on 29/01/2018.
 */
public class EasyRecyclerView<DataClass> extends RecyclerView {
    private final RecyclerViewAdapter adapter;
    private CellsFactory<DataClass> cellsFactory;
    private ArrayList<DataClass> dataBackup;

    public EasyRecyclerView(Context context, CellsFactory<DataClass> cellsFactory) {
        super(context);
        adapter = new RecyclerViewAdapter();
        init(cellsFactory);
    }

    public EasyRecyclerView(Context context, @Nullable AttributeSet attrs, CellsFactory<DataClass> cellsFactory) {
        super(context, attrs);
        adapter = new RecyclerViewAdapter();
        init(cellsFactory);
    }

    public EasyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle, CellsFactory<DataClass> cellsFactory) {
        super(context, attrs, defStyle);
        adapter = new RecyclerViewAdapter();
        init(cellsFactory);
    }

    private void init(CellsFactory<DataClass> cellsFactory) {
        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(getContext()));
        this.cellsFactory = cellsFactory;
    }

    public void setData(List<DataClass> data) {
        assert (getLayoutManager() != null);
        adapter.setData(data);
    }

    public void add(DataClass item) {
        assert (getLayoutManager() != null);
        adapter.add(item);
    }

    public void remove(DataClass item) {
        assert (getLayoutManager() != null);
        adapter.remove(item);
    }

    public EasyRecyclerView<DataClass> withCellType(DataClass tagCellClass) {
        return this;
    }

    public void filter(String filterQuery) {
        synchronized (adapter) {
            if (TextUtils.isEmpty(filterQuery)) {
                if (dataBackup != null) {
                    setData(dataBackup);
                    dataBackup = null;
                }
            } else {
                if (dataBackup == null) {
                    dataBackup = new ArrayList<>(adapter.data);
                }
                List<DataClass> filteredData = new ArrayList<>();
                for (DataClass d : dataBackup) {
                    if (d.toString().contains(filterQuery)) {
                        filteredData.add(d);
                    }
                }
                setData(filteredData);
            }
        }
    }

    public void reload() {
        adapter.notifyDataSetChanged();
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<CellHolder<DataClass>> {
        private List<DataClass> data = new ArrayList<>();

        @Override
        public CellHolder<DataClass> onCreateViewHolder(ViewGroup parent, int viewType) {
            return cellsFactory.create(parent, viewType);
        }

        @Override
        public void onBindViewHolder(CellHolder<DataClass> holder, int position) {
            holder.configure(data.get(position));
        }

        @Override
        public void onViewRecycled(CellHolder<DataClass> holder) {
            super.onViewRecycled(holder);
            holder.prepareForReuse();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<DataClass> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public void add(DataClass item) {
            if (data.add(item)) {
                int idx = data.indexOf(item);
                if (idx >= 0) {
                    notifyItemInserted(idx);
                }
            }
        }

        public void remove(DataClass item) {
            int idx = data.indexOf(item);
            if (idx >= 0) {
                if (data.remove(item)) {
                    notifyItemRemoved(idx);
                }
            }
        }
    }

    public abstract static class CellHolder<CellDataType> extends RecyclerView.ViewHolder {
        private CellDataType data;
        public CellHolder(View itemView) {
            super(itemView);
        }
        public void configure(CellDataType data) {
            this.data = data;
        }
        public abstract void prepareForReuse();

        public CellDataType getData() {
            return data;
        }
    }

    public interface CellsFactory<D> {
        CellHolder<D> create(ViewGroup parent, int viewType);
    }
}
