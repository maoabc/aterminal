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

import aterm.terminal.AbstractTerminal;

public class SshTerminal extends AbstractTerminal {
    private static final int MSG_NOTIFY_PTY_WRITE = 1;
    private static final int MSG_NOTIFY_PTY_FLUSH = 2;
    private static final int MSG_NOTIFY_PTY_RESIZE = 3;
    public static final String TAG = "SSH";

    private final ByteQueue mByteQueue = new ByteQueue(4096);

    private final String mKey;
    private String mTitle;
    private volatile Session mSession;
    private volatile ChannelShell mShell;
    private Handler mWriterHandler;
    private int mRows;
    private int mCols;

    @NonNull
    private String mHost;

    private int mPort = 22;

    private String mUsername;

    private String mPassword;

    private String mPrivateKey = "";
    private String mPassphrase = "";
    private HandlerThread mHandlerThread;

    public SshTerminal(ATermSettings settings, @NonNull String host, int port,
                       @NonNull String username, String password,
                       String privateKey, String passphrase,
                       @NonNull String key) {
        super(50, 30, 100, settings.getColorScheme()[0], settings.getColorScheme()[1]);
        mRows = 50;
        mCols = 30;
        this.mHost = host;
        this.mPort = port;
        this.mUsername = username;
        this.mPassword = password;
        this.mPrivateKey = privateKey;
        this.mPassphrase = passphrase;
        this.mKey = key;
        this.mTitle = key;
    }


    @Override
    public void start() {


        new Thread("Pty reader") {
            @Override
            public void run() {
                try {
                    JSch jSch = new JSch();
                    if (!TextUtils.isEmpty(mPrivateKey)) {
                        try {
                            jSch.addIdentity(mUsername + "@" + mHost,
                                    mPrivateKey.getBytes(), null,
                                    mPassphrase == null ? null : mPassphrase.getBytes());
                        } catch (JSchException e) {
                            Log.e(TAG, "addIdentity: ", e);
                        }

                    }

                    mSession = jSch.getSession(mUsername, mHost, mPort);
                    mSession.setConfig("PreferredAuthentications", "publickey,password");
                    //todo 检查服务器key
                    mSession.setConfig("StrictHostKeyChecking", "no");

                    mSession.setConfig("ConnectTimeout", "30");

                    mSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                    mSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                    mSession.setConfig("compression_level", "-1");

                    if (TextUtils.isEmpty(mPrivateKey) && !TextUtils.isEmpty(mPassword)) {
                        mSession.setPassword(mPassword.getBytes());
                    }

                    mSession.connect(30000);

                    mShell = (ChannelShell) mSession.openChannel("shell");
                    mShell.setPtyType("xterm-256color");
                    mShell.connect();
                    mShell.setOutputStream(new OutputStream() {
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
                                mDestroyCallback.onDestroy(SshTerminal.this, mShell.getExitStatus());
                            }
                        }
                    });
                    mShell.setPtySize(mCols, mCols, 0, 0);
                } catch (Exception e) {
                    Log.e(TAG, "Pty reader: ", e);
                    if (mDestroyCallback != null) {
                        mDestroyCallback.onDestroy(SshTerminal.this, -1);
                    }
                }
            }
        }.start();
        //写入数据到pty，消息循环
        mHandlerThread = new HandlerThread("Pty writer");
        mHandlerThread.start();
        mWriterHandler = new Handler(mHandlerThread.getLooper()) {
            OutputStream outputStream;

            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_NOTIFY_PTY_WRITE: {
                        byte[] bytes = new byte[4096];
                        try {
                            int read = mByteQueue.read(bytes, 0, Math.min(mByteQueue.getBytesAvailable(), bytes.length));
                            if (read > 0) {
                                if (outputStream == null) {
                                    if (mShell == null) {
                                        Log.e(TAG, "Not start shell");
                                        return;
                                    }
                                    outputStream = mShell.getOutputStream();
                                }
                                outputStream.write(bytes, 0, read);

                            }
                        } catch (IOException | InterruptedException e) {
                            Log.e(TAG, "writeToPty: ", e);
                        }
                        break;
                    }
                    case MSG_NOTIFY_PTY_RESIZE: {
                        if (mShell != null) mShell.setPtySize(msg.arg1, msg.arg2, 0, 0);
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
        mCols = cols;
        mRows = rows;
        Message message = mWriterHandler.obtainMessage(MSG_NOTIFY_PTY_RESIZE, cols, rows);
        mWriterHandler.sendMessage(message);
    }

    @Override
    protected void closePty() {
        if (mShell != null) {
            mShell.disconnect();
        }
        if (mSession != null) {
            mSession.disconnect();
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
