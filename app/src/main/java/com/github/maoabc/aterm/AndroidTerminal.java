package com.github.maoabc.aterm;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import aterm.pty.Pty;
import aterm.terminal.AbstractTerminal;

public class AndroidTerminal extends AbstractTerminal {
    static final String TAG = "Local";
    private static final String ROOT = "/data/data/com.github.maoabc.aterm";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final ATermSettings mSettings;
    private final String mKey;
    private final String mExecutePath;
    private final String[] mArgs;
    private final String[] mEnv;
    private String mTitle;

    //执行结束退出
    private final boolean mExecExit;

    private volatile CountDownLatch mLatch;

    private int mMasterFd = -1;
    private FileInputStream mPtyInput;
    private FileOutputStream mPtyOutput;
    private int mPid;

    public AndroidTerminal(@NonNull ATermSettings settings, @NonNull String executePath, String[] args, String[] env, @NonNull String key, boolean exit) {
        super(10, 50, 1000, settings.getColorScheme()[0], settings.getColorScheme()[1]);
        mSettings = settings;
        this.mEnv = env;
        mKey = key;
        mTitle = key;
        this.mExecutePath = executePath;
        this.mArgs = args;
        this.mExecExit = exit;
    }


    public void start() {

        if (DEBUG) Log.d(TAG, "start: " + getKey());

        try {

            exec();

            new Thread("Terminal reader") {
                @Override
                public void run() {
                    try {
                        byte[] bytes = new byte[4096];
                        while (true) {
                            int len = mPtyInput.read(bytes, 0, bytes.length);
                            if (len == 0) {
                                continue;
                            }
                            if (len == -1) {
                                break;
                            }
                            if (DEBUG) Log.d(TAG, "run: " + new String(bytes, 0, len));
                            //write to terminal
                            inputWrite(bytes, 0, len);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "read from pty: ", e);
                    }
                }
            }.start();
        } catch (IOException e) {
            Log.e(TAG, "start: ", e);
        }
        new Thread("Wait for") {
            @Override
            public void run() {
                int code = waitFor();
                if (DEBUG) Log.d(TAG, "wait: " + code);
                if (mDestroyCallback != null)
                    mDestroyCallback.onDestroy(AndroidTerminal.this, code);
                closePty();
            }
        }.start();
    }

    private void exec() throws IOException {
        final String shell = TextUtils.isEmpty(mExecutePath) ? "/system/bin/sh" : mExecutePath;

        final int[] masterFd = new int[1];
        mPid = Pty.exec(shell, mArgs, initEnv(),
                25, 30, masterFd);
        mMasterFd = masterFd[0];
        FileDescriptor fd = Pty.createFileDescriptor(mMasterFd);
        mPtyInput = new FileInputStream(fd);
        mPtyOutput = new FileOutputStream(fd);
    }

    public boolean isAfterExecExit() {
        return mExecExit;
    }


    public void onAKeyDown(int keyCode) {
        if (mLatch != null) mLatch.countDown();
    }

    public void waitKeyDown() throws InterruptedException {
        if (mLatch == null) {
            mLatch = new CountDownLatch(1);
        }
        mLatch.await();
    }

    //生成环境变量
    private String[] initEnv() {

        String[] extEnv = this.mEnv;
        int extEnvLength = 0;
        if (extEnv != null) {
            extEnvLength = extEnv.length;
        }

        String[] env = new String[extEnvLength + 3];
        int i = 0;
        for (; i < extEnvLength; i++) {
            env[i] = extEnv[i];
        }


        String path = System.getenv("PATH");
        env[i++] = "TERM=xterm-256color";
        env[i++] = "PATH=" + ROOT + "/bin:" + path;
        env[i] = "HOME=" + ROOT;

        if (DEBUG) Log.d(TAG, "initEnv: " + Arrays.toString(env));

        return env;
    }

    //检测路径是否可执行
    private String checkPath(String path) {
        if (path == null) {
            return "";
        }
        String[] dirs = path.split(":");
        StringBuilder checkedPath = new StringBuilder(path.length());
        for (String dirname : dirs) {
            File dir = new File(dirname);
            if (dir.isDirectory() && dir.canExecute()) {
                checkedPath.append(dirname);
                checkedPath.append(":");
            }
        }
        return checkedPath.substring(0, checkedPath.length() - 1);
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
        try {
            Pty.setWindowSize(mMasterFd, rows, cols);
        } catch (IOException e) {
            Log.e(TAG, "setPtyWindowSize: " + getKey(), e);
        }

    }

    @Override
    protected void closePty() {
        try {
            Pty.close(mMasterFd);
        } catch (IOException e) {
            Log.e(TAG, "closePty: ", e);
        }
    }

    private int waitFor() {
        return Pty.waitFor(mPid);
    }

    @Override
    protected int scrollRowSize() {
        //todo 通过外部设置
        return 1000;
    }

    @Override
    public void flushToPty() {

    }

    @Override
    public void release() {
        onAKeyDown(KeyEvent.KEYCODE_ENTER);
    }

    @Override
    public void writeToPty(final byte[] bytes, final int len) {
        //data write to pty
        try {
//            if (DEBUG) Log.d(TAG, "writeToPty: " + new String(HexEncoding.encode(bytes, 0, len)));
            if (mPtyOutput != null) mPtyOutput.write(bytes, 0, len);
        } catch (Exception e) {
            Log.e(TAG, "outputWrite: ", e);
        }
    }

}
