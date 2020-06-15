package com.github.maoabc.aterm.viewmodel;

import android.view.View;

import com.github.maoabc.aterm.db.entities.SshServer;

import org.greenrobot.eventbus.EventBus;


public class TerminalItem {
    private final SshServer sshServer;

    TerminalItem(SshServer sshServer) {
        this.sshServer = sshServer;
    }

    public String getName() {
        if (sshServer == null) {
            return "Local Terminal";
        }
        return sshServer.getUsername() + "@" + sshServer.getHost();
    }

    public SshServer getSshServer() {
        return sshServer;
    }

    public boolean isLocal() {
        return sshServer == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerminalItem that = (TerminalItem) o;

        return sshServer != null && sshServer.equals(that.sshServer);
    }

    @Override
    public int hashCode() {
        return sshServer != null ? sshServer.hashCode() : super.hashCode();
    }

    public void onClick(View v) {
        EventBus.getDefault().post(new ItemClickEvent(this));
    }

    public boolean onLongClick(View v) {
        if (sshServer == null) {
            return false;
        }
        EventBus.getDefault().post(new ItemLongClickEvent(this));
        return true;
    }

    public static class ItemClickEvent {
        public final TerminalItem item;

        public ItemClickEvent(TerminalItem item) {
            this.item = item;
        }
    }

    public static class ItemLongClickEvent {
        public final TerminalItem item;

        public ItemLongClickEvent(TerminalItem item) {
            this.item = item;
        }
    }
}
