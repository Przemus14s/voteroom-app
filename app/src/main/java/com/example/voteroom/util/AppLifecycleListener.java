package com.example.voteroom.util;

import android.app.Application;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class AppLifecycleListener implements DefaultLifecycleObserver {

    private static boolean isInForeground = false;

    public static boolean isAppInForeground() {
        return isInForeground;
    }

    public static void init(Application application) {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        isInForeground = true;
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        isInForeground = false;
    }
}
