package com.github.maoabc.aterm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.github.maoabc.BaseApp;
import com.github.maoabc.aterm.ssh.SshTerminal;
import com.github.maoabc.util.FileUtils;
import com.github.maoabc.util.MimeTypes;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import aterm.terminal.AbstractTerminal;

public class ATermService extends Service {
    public static final String TAG = "ATermService";

    private static int number = 1;

    public static final String PREFERENCES_NAME = "aterm_preferences";
    private static final int RUNNING_NOTIFICATION = 1;
    public static final String SERVICE_ACTION_START_SERVICE = "aterm.action.startService";

    public static final String SERVICE_ACTION_CHANGE_DIRECTORY = "aterm.action.changeDirectory";

    public static final String SERVICE_ACTION_EXEC_COMMAND = "aterm.action.execCommand";

    public static final String SERVICE_ACTION_SEND_COMMAND = "aterm.action.sendCommand";

    public static final String SERVICE_ACTION_STOP_SERVICE = "aterm.action.stopService";

    public static final String EXTRA_WORKING_DIRECTORY = "aterm.action.extra.pwd";
    public static final String EXTRA_ARGUMENTS = "aterm.action.extra.arguments";

    public static final String EXTRA_ENVIRONMENTS = "aterm.action.extra.environments";

    public static final String EXTRA_COMMAND = "aterm.action.extra.command";

    private Handler handler = new Handler();

    public final ObservableList<AbstractTerminal> terminals = new ObservableArrayList<>();


    //用于layout中判断当前终端
    public final ObservableField<AbstractTerminal> curTerminal = new ObservableField<>();

    public final MutableLiveData<AbstractTerminal> currentTerminal = new MutableLiveData<>();

    SharedPreferences mPreferences;
    ATermSettings mSettings;

    class TSBinder extends Binder {
        ATermService getService() {
            Log.i("TermService", "Activity binding to service");
            return ATermService.this;
        }
    }

    private static synchronized int nextId() {
        return number++;
    }

    private final IBinder mTSBinder = new TSBinder();

    public ATermService() {

    }


    public void createTerminalIfNeed() {
        if (!terminals.isEmpty()) {
            return;
        }
        AbstractTerminal terminal = createLocalTerminal();
        terminal.start();
        addTerminal(terminal);
    }

    private boolean hasTerm(String key) {
        for (AbstractTerminal terminal : terminals) {
            if (terminal.getKey().equals(key)) {
                return true;
            }

        }
        return false;
    }


    public SshTerminal createSshTerminal(String host, int port, String username, String password, String privateKey, String passphase) {
        String key;
        do {
            key = SshTerminal.TAG + username + nextId();
        } while (hasTerm(key));

        return new SshTerminal(mSettings, host, port, username, password, privateKey, passphase, key);
    }

    public AndroidTerminal createLocalTerminal() {
        String key;
        do {
            key = AndroidTerminal.TAG + nextId();
        } while (hasTerm(key));

        return new AndroidTerminal(mSettings, "/system/bin/sh", new String[]{"-"}, null, key, false);
    }

    public AndroidTerminal createTerminal(@NonNull String executePath, String[] args, String[] env) {
        int lastSlashIndex = executePath.lastIndexOf('/');
        String processName = (lastSlashIndex == -1 ? executePath : executePath.substring(lastSlashIndex + 1));
        String key;
        String suf = "";
        int i = 1;
        do {
            key = processName + suf;
            suf = "" + i++;
        } while (hasTerm(key));

        return new AndroidTerminal(mSettings, executePath, args, env, key, true);
    }

    public void addTerminal(AbstractTerminal terminal) {
        terminals.add(terminal);
        setCurrentTerminal(terminal);
    }

    public void setCurrentTerminal(AbstractTerminal terminal) {
        int currentItem = terminals.indexOf(terminal);
        setCurrentTerminal(currentItem);
    }

    private void setCurrentTerminal(int currentItem) {
        if (currentItem >= 0 && currentItem < terminals.size()) {
            AbstractTerminal terminal = terminals.get(currentItem);
            currentTerminal.setValue(terminal);
            curTerminal.set(terminal);
        } else {
            currentTerminal.setValue(null);
            curTerminal.set(null);
        }
    }

    public void removeTerminal(AbstractTerminal terminal) {
        int index = terminals.indexOf(terminal);
        if (index != -1) {
            terminals.remove(index);
            terminal.release();
            AbstractTerminal currentTerminal = this.currentTerminal.getValue();
            if (terminal.equals(currentTerminal)) {
                setCurrentTerminal(Math.min(index, terminals.size() - 1));
            } else {
                setCurrentTerminal(terminals.indexOf(currentTerminal));
            }
        }
        if (terminals.isEmpty()) {
            Intent intent = new Intent(this, ATermService.class);
            intent.setAction(ATermService.SERVICE_ACTION_STOP_SERVICE);
            startService(intent);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mTSBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = "";
        if (intent != null) {
            action = intent.getAction();
        }
        if (SERVICE_ACTION_STOP_SERVICE.equals(action)) {
            stopSelf();
            EventBus.getDefault().post(new FinishTerminalActivityEvent());
        } else if (SERVICE_ACTION_CHANGE_DIRECTORY.equals(action)) {
            AbstractTerminal terminal = currentTerminal.getValue();
            if (terminal == null) {//第一次启动可能没任何终端，先创建个本地终端，然后设置为当前
                terminal = createLocalTerminal();
                terminal.start();
                addTerminal(terminal);
            }

            try {
                byte result = 'u' - 'a' + '\001';
                if (intent.hasExtra(EXTRA_WORKING_DIRECTORY)) {
                    String dir = intent.getStringExtra(EXTRA_WORKING_DIRECTORY);
                    if (!TextUtils.isEmpty(dir)) {
                        byte[] b = {result};
                        terminal.writeToPty(b, 1);
                        byte[] bytes = ("cd \"" + dir.trim() + "\"\n").getBytes(StandardCharsets.UTF_8);
                        terminal.writeToPty(bytes, bytes.length);
                        terminal.flushToPty();
                    }
                }
            } catch (Exception e) {
            }

            startActivity(new Intent(this, ATermActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        } else if (SERVICE_ACTION_EXEC_COMMAND.equals(action)) {
            String[] args = intent.getStringArrayExtra(EXTRA_ARGUMENTS);
            if (args == null || args.length == 0) {
                Toast.makeText(getBaseContext(), "Arguments is empty", Toast.LENGTH_SHORT).show();
            } else {
                String execute = args[0];

                String[] env = intent.getStringArrayExtra(EXTRA_ENVIRONMENTS);

//                Log.d(TAG, "onStartCommand: exec command" + args);
                AndroidTerminal terminal = createTerminal(execute, args, env);
                terminal.setDestroyCallback(this::execFinishWaitKeyDown);
                terminal.start();
                addTerminal(terminal);

                startActivity(new Intent(this, ATermActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }

        } else if (SERVICE_ACTION_START_SERVICE.equals(action)) {
            startActivity(new Intent(this, ATermActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (SERVICE_ACTION_SEND_COMMAND.equals(action)) {
            AbstractTerminal terminal = currentTerminal.getValue();
            if (terminal == null) {//第一次启动可能没任何终端，先创建个本地终端，然后设置为当前
                terminal = createLocalTerminal();
                terminal.start();
                addTerminal(terminal);
            }
//            Log.d(TAG, "onStartCommand: SEND command "+terminal);

            try {
                byte result = 'u' - 'a' + '\001';
                if (intent.hasExtra(EXTRA_COMMAND)) {
                    String cmd = intent.getStringExtra(EXTRA_COMMAND);
                    if (!TextUtils.isEmpty(cmd)) {
                        byte[] b = {result};
                        terminal.writeToPty(b, 1);
                        byte[] bytes = (cmd.trim() + "\n").getBytes(StandardCharsets.UTF_8);
                        terminal.writeToPty(bytes, bytes.length);
                        terminal.flushToPty();
                    }
                }
            } catch (Exception e) {
            }
            startActivity(new Intent(this, ATermActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {//根据文件名执行脚本
            if (intent != null) {
                try {
                    Uri data = intent.getData();
                    if (data != null && MimeTypes.isShellScript(data.getPath())) {
                        String filePath = FileUtils.getFileFromUri(data);
                        File tempFile;
                        if (filePath == null) {
                            tempFile = createTempFile(data);
                            filePath = tempFile.getAbsolutePath();
                        } else {
                            tempFile = null;
                        }


                        if (!TextUtils.isEmpty(filePath)) {
                            String executePath = "/system/bin/sh";
                            String[] args = new String[3];
                            args[0] = executePath;
                            args[1] = "-c";
                            args[2] = "sh " + filePath;

                            AndroidTerminal terminal = createTerminal(executePath, args, null);
                            terminal.setDestroyCallback((terminal1, exitCode) -> {
                                if (tempFile != null) tempFile.delete();
                                execFinishWaitKeyDown(terminal1, exitCode);
                            });
                            terminal.start();

                            addTerminal(terminal);

                            startActivity(new Intent(this, ATermActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } else {
                            BaseApp.toast("Can't run");
                        }
                    }
                } catch (Exception e) {
                    BaseApp.toast(e.getLocalizedMessage());
                }
            }
        }

        return START_STICKY;
    }

    private File createTempFile(Uri data) throws IOException {
        InputStream input = getContentResolver().openInputStream(data);
        File tempFile = File.createTempFile("script", "temp.sh");
        FileOutputStream os = new FileOutputStream(tempFile);
        FileUtils.copyStreamAndClose(input, new OutputStream() {
            private long writeCount;

            @Override
            public void write(int b) throws IOException {
                os.write(b);
                writeCount++;
                checkOutsize();
            }

            @Override
            public void flush() throws IOException {
                os.flush();
            }

            @Override
            public void write(byte[] b) throws IOException {
                os.write(b);
                writeCount += b.length;
                checkOutsize();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                os.write(b, off, len);
                writeCount += len;
                checkOutsize();
            }

            @Override
            public void close() throws IOException {
                os.close();
            }

            private void checkOutsize() throws IOException {
                if (writeCount > 1024 * 1024) {
                    throw new IOException("File is too large");
                }
            }
        });
        return tempFile;
    }

    private void execFinishWaitKeyDown(AbstractTerminal terminal, int exitCode) {
        if (terminal instanceof AndroidTerminal) {
            AndroidTerminal androidTerminal = (AndroidTerminal) terminal;

            byte[] bytes = BaseApp.getResString(R.string.terminal_session_exit_msg, exitCode).getBytes(StandardCharsets.UTF_8);
            terminal.writeToPty(bytes, bytes.length);
            terminal.flushToPty();
            //等待按键输入，然后执行完毕退出
            try {
                androidTerminal.waitKeyDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                removeTerminal(terminal);

                if (!terminals.isEmpty()) {
                    EventBus.getDefault().post(new FinishTerminalActivityEvent());
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notifyIntent = new Intent(this, ATermActivity.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

        String channel = createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_terminal)
                .setContentText(getString(R.string.term_session_running))
                .setContentIntent(pendingIntent);
        Intent intent = new Intent(this, ATermService.class).setAction(SERVICE_ACTION_STOP_SERVICE);
        builder.addAction(android.R.drawable.ic_delete, getString(R.string.close),
                PendingIntent.getService(this, 0, intent, 0));
        try {
            startForeground(RUNNING_NOTIFICATION, builder.build());
        } catch (Exception e) {
            stopForeground(true);
        }

        mPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        mSettings = new ATermSettings(mPreferences);


    }

    private String createNotificationChannel() {
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "term sessions";
            NotificationChannel channel = new NotificationChannel(channelId, "term sessions", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        return channelId;
    }

    public boolean onTerminalItemLongClick(AbstractTerminal terminal) {
        EventBus.getDefault().post(new TerminalLongClickEvent(terminal));
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        for (AbstractTerminal terminal : terminals) {
            terminal.release();
        }
        terminals.clear();
        currentTerminal.setValue(null);
        curTerminal.set(null);
        BaseApp.toast(R.string.closed_term_session);
    }

    public static class TerminalLongClickEvent {
        public final AbstractTerminal terminal;

        TerminalLongClickEvent(AbstractTerminal terminal) {
            this.terminal = terminal;
        }
    }

    public static class FinishTerminalActivityEvent {
        FinishTerminalActivityEvent() {
        }
    }
}
