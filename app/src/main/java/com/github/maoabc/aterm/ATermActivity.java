package com.github.maoabc.aterm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.github.maoabc.BaseApp;
import com.github.maoabc.aterm.db.entities.SshServer;
import com.github.maoabc.aterm.ssh.SshTerminal;
import com.github.maoabc.aterm.viewmodel.TerminalItem;
import com.github.maoabc.common.fragment.TextFieldDialogFragment;
import com.github.maoabc.common.widget.CheckableButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import aterm.terminal.AbstractTerminal;
import aterm.terminal.TerminalKeys;
import aterm.terminal.TerminalView;
import aterm.terminal.UpdateCallback;


public class ATermActivity extends AppCompatActivity {
    public static final String TAG = ATermActivity.class.getName();
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private final DisplayMetrics metrics = new DisplayMetrics();

    private ATermService mATermService;

    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mATermService = ((ATermService.TSBinder) service).getService();

            init();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            if (mATermService != null) {
                mATermService.currentTerminal.removeObservers(ATermActivity.this);
            }
            mATermService = null;
        }
    };
    SharedPreferences.OnSharedPreferenceChangeListener mTermPreferChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (mATermService != null) {
                mATermService.mSettings.readPrefs(sharedPreferences);
                updateTerminalPrefs(mATermService.mSettings);
            }
        }
    };
    private TerminalView mTerminalView;
    private boolean mHaveFullHwKeyboard;
    private CheckableButton mCtrlChecked;

    private Handler handler = new Handler();
    private DrawerLayout mDrawerLayout;
    private BellUtil bellUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_aterm);
//        Window window = getWindow();
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EventBus.getDefault().register(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);


        mTerminalView = findViewById(R.id.emulator_view);

        findViewById(R.id.btn_esc).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_ESCAPE));
        mCtrlChecked = findViewById(R.id.btn_ctrl);
        mCtrlChecked.setOnClickListener(v -> {
            if (v instanceof Checkable) {
                ((Checkable) v).toggle();

                boolean checked = ((Checkable) v).isChecked();
                int modifiers = mTerminalView.getModifiers();
                mTerminalView.setModifiers(checked ?
                        modifiers | TerminalKeys.VTERM_MOD_CTRL :
                        modifiers & ~TerminalKeys.VTERM_MOD_CTRL);

            }
        });

        findViewById(R.id.btn_tab).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_TAB));
        findViewById(R.id.btn_minus).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_MINUS));
        findViewById(R.id.btn_colon).setOnClickListener(v -> {
            if (mTerminalView != null) {
                AbstractTerminal terminal = mTerminalView.getTerminal();
                if (terminal != null) {
                    byte[] bytes = ":".getBytes();
                    terminal.writeToPty(bytes, bytes.length);
                    terminal.flushToPty();
                }
            }
        });
        findViewById(R.id.btn_left).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_DPAD_LEFT));
        findViewById(R.id.btn_up).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_DPAD_UP));
        findViewById(R.id.btn_down).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_DPAD_DOWN));
        findViewById(R.id.btn_right).setOnClickListener(v -> sendKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT));

        View menuView = findViewById(R.id.btn__nav_overflow);
        menuView.setOnClickListener(v -> {
            Intent intent = new Intent(ATermActivity.this, ATermSettingsActivity.class);
            startActivity(intent);
        });


        Intent intent = new Intent(this, ATermService.class);
        startService(intent);
        if (!bindService(intent, mTSConnection, 0)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
        findViewById(R.id.btn_nav_add_term).setOnClickListener(v -> {
            TerminalManagerDialogFragment fragment = TerminalManagerDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), null);
        });

        bellUtil = BellUtil.getInstance(getApplicationContext());

    }

    @Subscribe
    public void onItemClick(TerminalItem.ItemClickEvent event) {
        if (mATermService == null) {
            return;
        }
        SshServer sshServer = event.item.getSshServer();
        if (sshServer == null) {
            AbstractTerminal localTerminal = mATermService.createLocalTerminal();
            localTerminal.start();
            mATermService.addTerminal(localTerminal);
        } else {//添加ssh终端
            SshTerminal sshTerminal = mATermService.createSshTerminal(sshServer.getHost(), sshServer.getPort(),
                    sshServer.getUsername(), sshServer.getPassword(),
                    sshServer.getPrivateKey(), sshServer.getPrivateKeyPhase());
            sshTerminal.start();
            mATermService.addTerminal(sshTerminal);

        }
    }

    @Subscribe
    public void onEditTerminalTitle(ATermService.TerminalLongClickEvent event) {
        AbstractTerminal terminal = event.terminal;
        TextFieldDialogFragment fragment = TextFieldDialogFragment.newInstance(
                getString(R.string.edit_session_name),
                "", terminal.getTitle());
        fragment.setResultCallback(text -> {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            terminal.setTitle(text);
            if (mATermService != null) {//change item
                int index = mATermService.terminals.indexOf(terminal);
                if (index != -1) {
                    mATermService.terminals.set(index, terminal);
                }
            }

        });
        fragment.show(getSupportFragmentManager(), null);

    }

    @Subscribe
    public void onFinishActivityEvent(ATermService.FinishTerminalActivityEvent event) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTSConnection != null) unbindService(mTSConnection);

        if (mTerminalView != null) {
            mTerminalView.detachCurrentTerminal();
        }
        if (mATermService != null) {
            mATermService.currentTerminal.removeObservers(this);
        }

        if (mATermService != null)
            mATermService.mPreferences.unregisterOnSharedPreferenceChangeListener(mTermPreferChanged);

        mATermService = null;
        mTSConnection = null;
        EventBus.getDefault().unregister(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);
    }

    private void updateTerminalPrefs(ATermSettings settings) {
        TerminalView view = this.mTerminalView;
        if (view == null) {
            return;
        }
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        view.setTextSize(Typeface.MONOSPACE, settings.getFontSize() * metrics.density);

        int[] scheme = settings.getColorScheme();
        view.setDefaultColor(scheme[0], scheme[1]);

        view.setBackgroundAlpha(settings.getBackgroundAlpha());

    }

    private boolean checkHaveFullHwKeyboard(Configuration c) {
        return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
                (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }

    private void init() {
        if (mATermService != null) {
            //监听当前会话改变
            mATermService.currentTerminal.observe(this, terminal -> {
                if (terminal == null) {
                    finish();
                    return;
                }

                if (mDrawerLayout != null) mDrawerLayout.closeDrawer(GravityCompat.START);

                initTerminalView(terminal);
                BaseApp.toastTop(terminal.getTitle());
            });
            mATermService.createTerminalIfNeed();

            mATermService.mPreferences.registerOnSharedPreferenceChangeListener(mTermPreferChanged);

            RecyclerView termList = findViewById(R.id.term_list);
            termList.setAdapter(new TerminalListAdapter(mATermService));

        }
    }


    private void doToggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

    }

    private void initTerminalView(AbstractTerminal terminal) {
        if (mTerminalView == null) {
            return;
        }
        if (!(terminal instanceof AndroidTerminal) || !((AndroidTerminal) terminal).isExitAfterExit()) {
            terminal.setDestroyCallback((terminal1, exitCode) -> {
                handler.post(() -> {
                    if (mATermService != null) {
                        mATermService.removeTerminal(terminal1);
                    }
                });

            });
        }
        if (mATermService != null) updateTerminalPrefs(mATermService.mSettings);

        mTerminalView.setTerminal(terminal);

        mTerminalView.setUpdateCallback(new UpdateCallback() {
            @Override
            public void onUpdate() {
            }

            @Override
            public void onBell() {
                if (bellUtil != null) bellUtil.doBell();
            }
        });

        mTerminalView.setModifiersChangedListener(modifiers -> mCtrlChecked.setChecked((modifiers & TerminalKeys.VTERM_MOD_CTRL) != 0));


    }

    private void sendKeyCode(int keyCode) {
        KeyEvent[] events = {new KeyEvent(KeyEvent.ACTION_DOWN, keyCode),
                new KeyEvent(KeyEvent.ACTION_UP, keyCode)
        };
        for (KeyEvent event : events) {
            dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mTerminalView != null) {
            AbstractTerminal terminal = mTerminalView.getTerminal();
            if (terminal instanceof AndroidTerminal && ((AndroidTerminal) terminal).isExitAfterExit()) {
                ((AndroidTerminal) terminal).exitProcess();
            }
        }
        return super.dispatchKeyEvent(event);
    }
}

