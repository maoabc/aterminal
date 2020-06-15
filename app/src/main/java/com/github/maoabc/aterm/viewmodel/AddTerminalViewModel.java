package com.github.maoabc.aterm.viewmodel;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import com.github.maoabc.BaseApp;
import com.github.maoabc.aterm.db.ATermDatabase;
import com.github.maoabc.aterm.db.entities.SshServer;
import com.github.maoabc.aterm.db.source.SshServerDataSource;


public class AddTerminalViewModel extends ViewModel {
    public final ObservableList<TerminalItem> terminals = new ObservableArrayList<>();
    private SshServerDataSource sshServerDataSource;

    public AddTerminalViewModel() {
        loadTerminals();
    }

    private void loadTerminals() {
        terminals.clear();
        terminals.add(new TerminalItem(null));//local terminal
        BaseApp context = BaseApp.get();
        sshServerDataSource = new SshServerDataSource(ATermDatabase.getInstance(context).sshServerDao(), BaseApp.get().getAppExecutors());
        sshServerDataSource.getSshServers()
                .observeForever(sshServers -> {
                    if (sshServers == null) {
                        return;
                    }
                    for (SshServer server : sshServers) {
                        terminals.add(new TerminalItem(server));
                    }
                });
    }

    public void addServer(SshServer sshServer) {
        if (sshServer.getOrder() == -1) {
            sshServer.setOrder(terminals.size());
        }
        sshServerDataSource
                .addSshServer(sshServer)
                .observeForever(b -> {
                    if (!b) {
                        return;
                    }
                    loadTerminals();
                });
    }

    public void deleteServer(final TerminalItem item) {
        if (item.getSshServer() == null) {
            return;
        }
        sshServerDataSource.delete(item.getSshServer()).observeForever(b -> {
            if (!b) {
                return;
            }
            terminals.remove(item);
        });
    }
}
