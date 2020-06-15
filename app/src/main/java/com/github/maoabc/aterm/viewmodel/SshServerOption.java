package com.github.maoabc.aterm.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.github.maoabc.aterm.BR;
import com.github.maoabc.aterm.db.entities.SshServer;


public class SshServerOption extends BaseObservable implements Parcelable {
    @NonNull
    private final String id;

    @NonNull
    private String host = "";

    @NonNull
    private String port = "22";

    @NonNull
    private String username = "";
    @NonNull
    private String password = "";

    private boolean compression;


    private boolean enableKey = false;

    @NonNull
    private String privateKey = "";

    @NonNull
    private String passphrase = "";

    private int order = -1;

    public SshServerOption() {
        id = "";
    }

    public SshServerOption(SshServer server) {
        this.id = server.getId();

        this.host = server.getHost();

        this.port = server.getPort() + "";

        this.username = server.getUsername();

        this.password = server.getPassword();

        this.compression = true;

        this.privateKey = server.getPrivateKey();

        this.enableKey = !TextUtils.isEmpty(this.privateKey);

        this.passphrase = server.getPrivateKeyPhase();

        this.order = server.getOrder();

    }

    @NonNull
    @Bindable
    public CharSequence getHost() {
        return host;
    }

    public void setHost(CharSequence host) {
        if (!host.equals(this.host)) {
            this.host = host.toString();
            notifyPropertyChanged(BR.host);
        }
    }

    @NonNull
    @Bindable
    public CharSequence getPort() {
        return port;
    }

    public void setPort(@NonNull CharSequence port) {
        if (!port.equals(this.port)) {
            this.port = port.toString();
            notifyPropertyChanged(BR.port);
        }
    }

    @NonNull
    @Bindable
    public CharSequence getUsername() {
        return username;
    }

    public void setUsername(CharSequence username) {
        if (!username.equals(this.username)) {
            this.username = username.toString();
            notifyPropertyChanged(BR.username);
        }
    }

    @NonNull
    @Bindable
    public CharSequence getPassword() {
        return password;
    }

    public void setPassword(CharSequence password) {
        if (!password.equals(this.password)) {
            this.password = password.toString();
            notifyPropertyChanged(BR.password);
        }
    }

    @Bindable
    public boolean isCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        if (this.compression != compression) {
            this.compression = compression;
            notifyPropertyChanged(BR.compression);
        }
    }


    @Bindable
    public boolean isEnableKey() {
        return enableKey;
    }

    public void setEnableKey(boolean enableKey) {
        if (enableKey != this.enableKey) {
            this.enableKey = enableKey;
            notifyPropertyChanged(BR.enableKey);
        }
    }

    @NonNull
    @Bindable
    public CharSequence getPrivateKey() {
        return privateKey;
    }


    public void setPrivateKey(CharSequence privateKey) {
        if (!privateKey.equals(this.privateKey)) {
            this.privateKey = privateKey.toString();
            notifyPropertyChanged(BR.privateKey);
        }
    }

    @NonNull
    @Bindable
    public CharSequence getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(CharSequence passphrase) {
        if (!passphrase.equals(this.passphrase)) {
            this.passphrase = passphrase.toString();
            notifyPropertyChanged(BR.passphrase);
        }
    }

    @NonNull
    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.host);
        dest.writeString(this.port);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeByte(this.compression ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableKey ? (byte) 1 : (byte) 0);
        dest.writeString(this.privateKey);
        dest.writeString(this.passphrase);
    }

    protected SshServerOption(Parcel in) {
        this.id = in.readString();
        this.host = in.readString();
        this.port = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.compression = in.readByte() != 0;
        this.enableKey = in.readByte() != 0;
        this.privateKey = in.readString();
        this.passphrase = in.readString();
    }

    public static final Creator<SshServerOption> CREATOR = new Creator<SshServerOption>() {
        @Override
        public SshServerOption createFromParcel(Parcel source) {
            return new SshServerOption(source);
        }

        @Override
        public SshServerOption[] newArray(int size) {
            return new SshServerOption[size];
        }
    };
}
