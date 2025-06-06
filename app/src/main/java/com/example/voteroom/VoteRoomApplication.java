package com.example.voteroom;

import android.app.Application;
import com.example.voteroom.util.AppLifecycleListener;

public class VoteRoomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLifecycleListener.init(this);
    }
}
