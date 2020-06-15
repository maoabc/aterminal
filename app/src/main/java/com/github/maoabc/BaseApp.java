package com.github.maoabc;

import android.app.Application;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.github.maoabc.util.AppExecutors;

public class BaseApp extends Application {
    private static BaseApp baseApp;
    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors=new AppExecutors();
        baseApp = this;
    }

    public static BaseApp get() {
        return baseApp;
    }

    public AppExecutors getAppExecutors() {
        return mAppExecutors;
    }

    public static void toast(@StringRes int strId) {
        Toast.makeText(get(), strId, Toast.LENGTH_LONG).show();
    }

    public static void toast(String str) {
        Toast.makeText(get(), str, Toast.LENGTH_LONG).show();
    }


    public static void toastTop(final String str) {
        Toast toast = Toast.makeText(get(), str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public static String getResString(@StringRes int strId, Object... args) {
        return BaseApp.get().getString(strId, args);
    }

}
