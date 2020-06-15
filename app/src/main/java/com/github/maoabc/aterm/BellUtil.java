package com.github.maoabc.aterm;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;

public class BellUtil {
    private static BellUtil instance = null;
    private final Vibrator mVibrator;
    public static final int MSG_ID = 6555;

    public static BellUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (BellUtil.class) {
                if (instance == null) {
                    instance = new BellUtil(context);
                }
            }
        }

        return instance;
    }

    private static final int DURATION = 30;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID) {
                vibrator();
            }
        }
    };

    private void vibrator() {
        if (mVibrator != null) {
            mVibrator.vibrate(DURATION);
        }
    }

    private long lastBell = 0;


    private BellUtil(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public synchronized void doBell() {
        long now = SystemClock.uptimeMillis();
        long timeSinceLastBell = now - lastBell;

        if (timeSinceLastBell > 0) {
            if (timeSinceLastBell < 3 * DURATION) {
                handler.sendEmptyMessageDelayed(MSG_ID, 3 * DURATION - timeSinceLastBell);
                lastBell = lastBell + 3 * DURATION;
            } else {
                vibrator();
                lastBell = now;
            }
        }
    }

}
