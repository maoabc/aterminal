package com.github.maoabc.aterm.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;


@Entity(tableName = "ssh_servers")
public class SshServer {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private String id;

    @NonNull
    @ColumnInfo(name = "host")
    private String host;

    @ColumnInfo(name = "port")
    private int port;

    @NonNull
    @ColumnInfo(name = "user")
    private String username;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "private_key")
    private String privateKey;

    @ColumnInfo(name = "private_key_phase")
    private String privateKeyPhase;

    @ColumnInfo
    private int compression;


    @ColumnInfo(name = "order_index")
    private int order;

    public SshServer() {
        this(UUID.randomUUID().toString(), 22, "", "", "", "");
    }

    @Ignore
    public SshServer(@NonNull String host, int port, @NonNull String username, String password, String privateKey, String privateKeyPhase) {
        this(UUID.randomUUID().toString(), host, port, username, password, privateKey, privateKeyPhase, true, 0);
    }

    @Ignore
    public SshServer(@NonNull String id, @NonNull String host, int port, @NonNull String username, String password, String privateKey, String privateKeyPhase, boolean compress, int order) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.privateKeyPhase = privateKeyPhase;
        this.compression = compress ? 1 : 0;
        this.order = order;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getHost() {
        return host;
    }

    public void setHost(@NonNull String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyPhase() {
        return privateKeyPhase;
    }

    public void setPrivateKeyPhase(String privateKeyPhase) {
        this.privateKeyPhase = privateKeyPhase;
    }

    public int getCompression() {
        return compression;
    }

    public void setCompression(int compression) {
        this.compression = compression;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SshServer sshServer = (SshServer) o;

        return id.equals(sshServer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
