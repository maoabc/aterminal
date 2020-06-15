package com.github.maoabc.aterm;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.maoabc.aterm.databinding.ListItemBinding;
import com.github.maoabc.aterm.db.entities.SshServer;
import com.github.maoabc.aterm.viewmodel.AddTerminalViewModel;
import com.github.maoabc.aterm.viewmodel.SshServerOption;
import com.github.maoabc.aterm.viewmodel.TerminalItem;
import com.github.maoabc.util.Precondition;
import com.github.maoabc.util.RecyclerViewAdapterChangedCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class AddTerminalDialogFragment extends DialogFragment {

    private AddTerminalViewModel mViewModel;

    public static AddTerminalDialogFragment newInstance() {

        Bundle args = new Bundle();

        AddTerminalDialogFragment fragment = new AddTerminalDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddTerminalViewModel.class);
        EventBus.getDefault().register(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final FragmentActivity context = getActivity();
        Precondition.checkNotNull(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, null);
        recyclerView.setAdapter(new ServerItemAdapter(mViewModel));


        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.new_terminal)
                .setView(recyclerView)
                .setPositiveButton(R.string.close, null)
                .setNeutralButton(R.string.new_ssh_server, null)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button neutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            if (neutral != null) {
                neutral.setOnClickListener(v -> {
                    CreateSshServerDialogFragment fragment = CreateSshServerDialogFragment.newInstance(R.string.new_ssh_server, new SshServerOption());
                    fragment.setResultCallback(this::addServer);
                    fragment.show(getParentFragmentManager(), null);
                });
            }
        });
        return alertDialog;
    }

    private void addServer(SshServerOption option) {
        if (option == null) {
            return;
        }
        int port = 22;
        try {
            port = Integer.parseInt(option.getPort().toString()) & 0xffff;
        } catch (Exception e) {
        }
        SshServer sshServer;
        if (TextUtils.isEmpty(option.getId())) {
            sshServer = new SshServer(
                    option.getHost().toString(),
                    port,
                    option.getUsername().toString(),
                    option.getPassword().toString(),
                    option.getPrivateKey().toString(),
                    option.getPassphrase().toString());
        } else {
            sshServer = new SshServer(
                    option.getId(),
                    option.getHost().toString(),
                    port,
                    option.getUsername().toString(),
                    option.getPassword().toString(),
                    option.getPrivateKey().toString(),
                    option.getPassphrase().toString(),
                    true,
                    option.getOrder());
        }
        mViewModel.addServer(sshServer);
    }

    @Subscribe
    public void onItemClick(TerminalItem.ItemClickEvent event) {
        dismiss();
    }

    @Subscribe
    public void onItemLongClick(TerminalItem.ItemLongClickEvent event) {
        if (event.item.getSshServer() == null) {
            return;
        }

        SshServerOption option = new SshServerOption(event.item.getSshServer());
        CreateSshServerDialogFragment fragment = CreateSshServerDialogFragment.newInstance(R.string.edit_ssh_server, option);
        fragment.setResultCallback(this::addServer);

        fragment.show(getParentFragmentManager(), null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    static class ServerItemAdapter extends RecyclerView.Adapter<ServerItemAdapter.ListItemViewHolder> {

        private final AddTerminalViewModel mViewModel;

        private List<TerminalItem> mItemList;

        private RecyclerViewAdapterChangedCallback mListChangedCallback;

        ServerItemAdapter(@NonNull AddTerminalViewModel viewModel) {
            this.mViewModel = viewModel;
            setList(mViewModel.terminals);
        }

        public void setList(@NonNull List<TerminalItem> items) {
            if (this.mItemList == items) {
                return;
            }
            if (this.mItemList instanceof ObservableList) {
                //noinspection unchecked
                ((ObservableList) this.mItemList).removeOnListChangedCallback(mListChangedCallback);
            }
            this.mItemList = items;
            if (this.mItemList instanceof ObservableList) {
                if (mListChangedCallback == null) {
                    mListChangedCallback = new RecyclerViewAdapterChangedCallback(this);
                }
                //noinspection unchecked
                ((ObservableList) this.mItemList).addOnListChangedCallback(mListChangedCallback);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item, parent, false);
            return new ListItemViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
            int layoutPosition = holder.getLayoutPosition();
            TerminalItem item = mItemList.get(layoutPosition);
            ListItemBinding binding = holder.binding;

            binding.setViewModel(mViewModel);
            binding.setItem(item);

            binding.executePendingBindings();

        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }

        static class ListItemViewHolder extends RecyclerView.ViewHolder {
            private final ListItemBinding binding;

            ListItemViewHolder(@NonNull ListItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

    }
}
