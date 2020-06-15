
package com.github.maoabc.aterm;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Terminal emulator settings
 */
public class ATermSettings {

    private int mOrientation;
    private int mFontSize;
    private int mColorId;
    private int mBackKeyAction;
    private int mControlKeyId;
    private int mFnKeyId;
    private int mUseCookedIME;
    private String mShell;
//    private String mInitialCommand;
//    private String mTermType;
//    private boolean mCloseOnExit;
//    private boolean mVerifyPath;
//    private String mHomePath;

    private int mBackgroundAlpha = 0xff;

    private boolean mAltSendsEsc;

    private boolean mMouseTracking;

    private boolean mUseKeyboardShortcuts;

    private static final String ORIENTATION_KEY = "orientation";
    public static final String FONTSIZE_KEY = "fontsize";
    public static final String COLOR_KEY = "color";
    private static final String BACKACTION_KEY = "backaction";
    public static final String CONTROLKEY_KEY = "controlkey";
    public static final String FNKEY_KEY = "fnkey";
    private static final String IME_KEY = "ime";
    private static final String SHELL_KEY = "shell";
    private static final String INITIALCOMMAND_KEY = "initialcommand";
    public static final String TERMTYPE_KEY = "termtype";
    private static final String CLOSEONEXIT_KEY = "close_window_on_process_exit";
    private static final String VERIFYPATH_KEY = "verify_path";
    private static final String PATHEXTENSIONS_KEY = "do_path_extensions";
    private static final String PATHPREPEND_KEY = "allow_prepend_path";
    private static final String HOMEPATH_KEY = "home_path";
    private static final String ALT_SENDS_ESC = "alt_sends_esc";
    private static final String MOUSE_TRACKING = "mouse_tracking";
    private static final String USE_KEYBOARD_SHORTCUTS = "use_keyboard_shortcuts";
    public static final String BACKGROUND_ALPHA = "background_alpha";

    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;
    public static final int BLUE = 0xff344ebd;
    public static final int GREEN = 0xff00ff00;
    public static final int AMBER = 0xffffb651;
    public static final int RED = 0xffff0113;
    public static final int HOLO_BLUE = 0xff33b5e5;
    public static final int SOLARIZED_FG = 0xff657b83;
    public static final int SOLARIZED_BG = 0xfffdf6e3;
    public static final int SOLARIZED_DARK_FG = 0xff839496;
    public static final int SOLARIZED_DARK_BG = 0xff002b36;
    public static final int LINUX_CONSOLE_WHITE = 0xffaaaaaa;

    // foreground color, background color
    private static final int[][] COLOR_SCHEMES = {
            {BLACK, WHITE},
            {WHITE, BLACK},
            {WHITE, BLUE},
            {GREEN, BLACK},
            {AMBER, BLACK},
            {RED, BLACK},
            {HOLO_BLUE, BLACK},
            {SOLARIZED_FG, SOLARIZED_BG},
            {SOLARIZED_DARK_FG, SOLARIZED_DARK_BG},
            {LINUX_CONSOLE_WHITE, BLACK}
    };


    public static final int ORIENTATION_UNSPECIFIED = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;

    /**
     * An integer not in the range of real key codes.
     */
    public static final int KEYCODE_NONE = -1;

    public static final int CONTROL_KEY_ID_NONE = 7;
    public static final int[] CONTROL_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };

    public static final int FN_KEY_ID_NONE = 7;
    public static final int[] FN_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };

    public static final int BACK_KEY_STOPS_SERVICE = 0;
    public static final int BACK_KEY_CLOSES_WINDOW = 1;
    public static final int BACK_KEY_CLOSES_ACTIVITY = 2;
    public static final int BACK_KEY_SENDS_ESC = 3;
    public static final int BACK_KEY_SENDS_TAB = 4;
    private static final int BACK_KEY_MAX = 4;

    public ATermSettings(SharedPreferences prefs) {
        readPrefs(prefs);
    }


    public void readPrefs(SharedPreferences prefs) {
        mOrientation = get(prefs, ORIENTATION_KEY, ORIENTATION_PORTRAIT);
        mFontSize = get(prefs, FONTSIZE_KEY, 14);
        mColorId = get(prefs, COLOR_KEY, 1);
        mBackKeyAction = get(prefs, BACKACTION_KEY, 2);
        mControlKeyId = get(prefs, CONTROLKEY_KEY, 5);
        mFnKeyId = get(prefs, FNKEY_KEY, 4);
        mUseCookedIME = get(prefs, IME_KEY, 0);
        mAltSendsEsc = get(prefs, ALT_SENDS_ESC, false);
        mShell = get(prefs, SHELL_KEY, "/system/bin/sh -");
//        mInitialCommand = get(prefs, INITIALCOMMAND_KEY, "");
//        mTermType = get(prefs, TERMTYPE_KEY, "xterm-256color");
//        mCloseOnExit = get(prefs, CLOSEONEXIT_KEY, true);
//        mVerifyPath = get(prefs, VERIFYPATH_KEY, true);
        mMouseTracking = get(prefs, MOUSE_TRACKING, mMouseTracking);
        mUseKeyboardShortcuts = get(prefs, USE_KEYBOARD_SHORTCUTS, false);
//        mHomePath = get(prefs, HOMEPATH_KEY, "/");

        mBackgroundAlpha = prefs.getInt(BACKGROUND_ALPHA, 0xff);

    }

    public int get(SharedPreferences prefs, String key, int defValue) {
        String v = prefs.getString(key, "");
        if (TextUtils.isEmpty(v)) {
            return defValue;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public boolean get(SharedPreferences prefs, String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public String get(SharedPreferences prefs, String key, String defValue) {
        return prefs.getString(key, defValue);
    }


    public int getScreenOrientation() {
        return mOrientation;
    }


    public int getFontSize() {
        return mFontSize;
    }

    public int[] getColorScheme() {
        Log.d("Aterm setting", "getColorScheme: " + mColorId);
        if (mColorId >= 0 && mColorId < COLOR_SCHEMES.length) {
            return COLOR_SCHEMES[mColorId];
        } else {

            return COLOR_SCHEMES[1];
        }
    }


    public int getBackKeyAction() {
        return mBackKeyAction;
    }

    public boolean backKeySendsCharacter() {
        return mBackKeyAction >= BACK_KEY_SENDS_ESC;
    }

    public boolean getAltSendsEscFlag() {
        return mAltSendsEsc;
    }

    public boolean getMouseTrackingFlag() {
        return mMouseTracking;
    }

    public boolean getUseKeyboardShortcutsFlag() {
        return mUseKeyboardShortcuts;
    }

    public int getBackKeyCharacter() {
        switch (mBackKeyAction) {
            case BACK_KEY_SENDS_ESC:
                return 27;
            case BACK_KEY_SENDS_TAB:
                return 9;
            default:
                return 0;
        }
    }

    public int getControlKeyId() {
        return mControlKeyId;
    }

    public int getFnKeyId() {
        return mFnKeyId;
    }

    public int getControlKeyCode() {
        if (mControlKeyId >= 0 && mControlKeyId < CONTROL_KEY_SCHEMES.length) {
            return CONTROL_KEY_SCHEMES[mControlKeyId];
        } else {
            return CONTROL_KEY_SCHEMES[CONTROL_KEY_ID_NONE];
        }
    }

    public int getFnKeyCode() {
        if (mFnKeyId >= 0 && mFnKeyId < FN_KEY_SCHEMES.length) {
            return FN_KEY_SCHEMES[mFnKeyId];
        } else {
            return FN_KEY_SCHEMES[FN_KEY_ID_NONE];
        }
    }

    public boolean useCookedIME() {
        return (mUseCookedIME != 0);
    }

    public String getShell() {
        if (TextUtils.isEmpty(mShell)) {
            return "/system/bin/sh -";
        }
        return mShell;
    }

    public int getBackgroundAlpha() {
        return mBackgroundAlpha;
    }

}
