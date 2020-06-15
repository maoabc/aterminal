package com.github.maoabc.util;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class RecyclerViewAdapterChangedCallback extends ObservableList.OnListChangedCallback {
    private final WeakReference<RecyclerView.Adapter> adapterWeakReference;

    public RecyclerViewAdapterChangedCallback(RecyclerView.Adapter adapter) {
        this.adapterWeakReference = new WeakReference<>(adapter);
    }

    @Override
    public void onChanged(ObservableList sender) {
        RecyclerView.Adapter adapter = adapterWeakReference.get();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterWeakReference.get();
        if (adapter != null) {
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }

    }

    @Override
    public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterWeakReference.get();
        if (adapter != null) {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

    }

    @Override
    public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
        RecyclerView.Adapter adapter = adapterWeakReference.get();
        if (adapter != null) {
            for (int i = 0; i < itemCount; i++) {
                adapter.notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }

    }

    @Override
    public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterWeakReference.get();
        if (adapter != null) {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

    }
}
