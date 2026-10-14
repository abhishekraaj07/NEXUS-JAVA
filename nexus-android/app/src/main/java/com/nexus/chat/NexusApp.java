package com.nexus.chat;

import android.app.Application;

public class NexusApp extends Application {
    private static NexusApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static NexusApp getInstance() {
        return instance;
    }
}
