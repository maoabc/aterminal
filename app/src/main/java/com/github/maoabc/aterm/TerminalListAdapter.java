package com.github.maoabc.aterm;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import com.github.maoabc.aterm.databinding.TerminalListItemBinding;
import com.github.maoabc.util.RecyclerViewAdapterChangedCallback;

import java.util.List;

import aterm.terminal.AbstractTerminal;

public class TerminalListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ATermService service;
    private List<AbstractTerminal> terminals;
    private RecyclerViewAdapterChangedCallback mListChangedCallback;

    TerminalListAdapter(ATermService service) {
        this.service = service;
        setList(service.terminals);
    }

    public void setList(List<AbstractTerminal> terminals) {
        if (this.terminals == terminals) {
            return;
        }
        if (this.terminals instanceof ObservableList) {
            //noinspection unchecked
            ((ObservableList) this.terminals).removeOnListChangedCallback(mListChangedCallback);
        }
        this.terminals = terminals;
        if (this.terminals instanceof ObservableList) {
            if (mListChangedCallback == null) {
                mListChangedCallback = new RecyclerViewAdapterChangedCallback(this);
            }
            //noinspection unchecked
            ((ObservableList) this.terminals).addOnListChangedCallback(mListChangedCallback);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TerminalListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.terminal_list_item, parent, false);

        return new TextItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextItemViewHolder) {
            AbstractTerminal terminal = terminals.get(holder.getLayoutPosition());

            TerminalListItemBinding binding = ((TextItemViewHolder) holder).binding;

            binding.setService(service);

            binding.setTerminal(terminal);

            binding.executePendingBindings();
        }

    }

    @Override
    public int getItemCount() {
        return terminals == null ? 0 : terminals.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class TextItemViewHolder extends RecyclerView.ViewHolder {
        private final TerminalListItemBinding binding;

        TextItemViewHolder(@NonNull TerminalListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}

