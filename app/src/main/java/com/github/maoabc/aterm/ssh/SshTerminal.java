package com.github.maoabc.aterm.ssh;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.maoabc.aterm.ATermSettings;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import aterm.terminal.AbstractTerminal;

public class SshTerminal extends AbstractTerminal {
    private static final int MSG_NOTIFY_PTY_WRITE = 1;
    private static final int MSG_NOTIFY_PTY_FLUSH = 2;
    private static final int MSG_NOTIFY_PTY_RESIZE = 3;
    public static final String TAG = "SSH";

    private final ByteQueue mByteQueue = new ByteQueue(4096);

    private final String mKey;
    private String mTitle;
    private Session session;
    private volatile ChannelShell shell;
    private Handler mWriterHandler;

    @NonNull
    private String host;

    private int port = 22;

    private String username;

    private String password;

    private String privateKey = "";
    private String passphrase = "";
    private HandlerThread mHandlerThread;

    public SshTerminal(ATermSettings settings, @NonNull String host, int port,
                       @NonNull String username, String password,
                       String privateKey, String passphrase,
                       @NonNull String key) {
        super(50, 30, 100, settings.getColorScheme()[0], settings.getColorScheme()[1]);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.mKey = key;
        this.mTitle = key;
    }


    @Override
    public void start() {
        try {
            JSch jSch = new JSch();
            if (!TextUtils.isEmpty(privateKey)) {
                jSch.addIdentity(username + "@" + host,
                        privateKey.getBytes(), null,
                        passphrase == null ? null : passphrase.getBytes());
            }

            session = jSch.getSession(username, host, port);
            session.setConfig("PreferredAuthentications", "publickey,password");

            session.setConfig("StrictHostKeyChecking", "no");

            session.setConfig("ConnectTimeout", "30");

            session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
            session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
            session.setConfig("compression_level", "-1");

            if (TextUtils.isEmpty(privateKey) && !TextUtils.isEmpty(password)) {
                session.setPassword(password.getBytes());
            }

            CountDownLatch latch = new CountDownLatch(1);
            new Thread("Pty reader") {
                @Override
                public void run() {
                    try {
                        session.connect(30000);

                        shell = (ChannelShell) session.openChannel("shell");
                        shell.setPtyType("xterm-256color");
                        shell.connect();
                        shell.setOutputStream(new OutputStream() {
                            byte[] buf = new byte[1];

                            @Override
                            public void write(int b) throws IOException {
                                buf[0] = (byte) b;
                                write(buf, 0, 1);
                            }

                            @Override
                            public void write(byte[] b) throws IOException {
                                write(b, 0, b.length);
                            }

                            @Override
                            public void write(byte[] b, int off, int len) throws IOException {
                                inputWrite(b, off, len);
                            }

                            @Override
                            public void close() throws IOException {
                                if (mDestroyCallback != null) {
                                    mDestroyCallback.onDestroy(SshTerminal.this, shell.getExitStatus());
                                }
                            }
                        });
                        latch.countDown();
                    } catch (Exception e) {
                        latch.countDown();
                        Log.e(TAG, "Pty reader: ", e);
                        if (mDestroyCallback != null) {
                            mDestroyCallback.onDestroy(SshTerminal.this, -1);
                        }
                    }
                }
            }.start();
            try {
                latch.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        } catch (JSchException e) {
            Log.e(TAG, "Init jsch: ", e);
            if (mDestroyCallback != null) {
                mDestroyCallback.onDestroy(SshTerminal.this, -1);
            }
        }
        //写入数据到pty，消息循环
        mHandlerThread = new HandlerThread("Pty writer");
        mHandlerThread.start();
        mWriterHandler = new Handler(mHandlerThread.getLooper()) {
            OutputStream outputStream;

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_NOTIFY_PTY_WRITE: {
                        byte[] bytes = new byte[4096];
                        try {
                            int read = mByteQueue.read(bytes, 0, Math.min(mByteQueue.getBytesAvailable(), bytes.length));
                            if (read > 0) {
                                if (outputStream == null) {
                                    if (shell == null) {
                                        Log.e(TAG, "Not start shell");
                                        return;
                                    }
                                    outputStream = shell.getOutputStream();
                                }
                                outputStream.write(bytes, 0, read);

                            }
                        } catch (IOException | InterruptedException e) {
                            Log.e(TAG, "writeToPty: ", e);
                        }
                        break;
                    }
                    case MSG_NOTIFY_PTY_RESIZE: {
                        if (shell != null) shell.setPtySize(msg.arg1, msg.arg2, 0, 0);
                        break;
                    }
                    case MSG_NOTIFY_PTY_FLUSH: {
                        try {
                            if (outputStream != null) outputStream.flush();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };

    }

    @NonNull
    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public void setTitle(@NonNull String title) {
        this.mTitle = title;
    }

    @NonNull
    @Override
    public String getKey() {
        return mKey;
    }

    @Override
    protected void setPtyWindowSize(int cols, int rows) {
        if (mWriterHandler == null) {
            Log.e(TAG, "setPtyWindowSize: Handler null");
            return;
        }
        Message message = mWriterHandler.obtainMessage(MSG_NOTIFY_PTY_RESIZE, cols, rows);
        mWriterHandler.sendMessage(message);
    }

    @Override
    protected void closePty() {
        if (shell != null) {
            shell.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        if (mHandlerThread != null) mHandlerThread.quitSafely();
    }


    @Override
    protected int scrollRowSize() {
        //todo
        return 1000;
    }

    @Override
    public void flushToPty() {
        if (mWriterHandler == null) {
            Log.e(TAG, "flushToPty: ");
            return;
        }
        mWriterHandler.sendEmptyMessage(MSG_NOTIFY_PTY_FLUSH);
    }

    @Override
    public void release() {

    }


    @Override
    public void writeToPty(byte[] bytes, int len) {
        if (mWriterHandler == null) {
            return;
        }
        try {
            mByteQueue.write(bytes, 0, len);
            mWriterHandler.sendEmptyMessage(MSG_NOTIFY_PTY_WRITE);
        } catch (InterruptedException e) {
            Log.e(TAG, "writeToPty", e);
        }

    }
}
