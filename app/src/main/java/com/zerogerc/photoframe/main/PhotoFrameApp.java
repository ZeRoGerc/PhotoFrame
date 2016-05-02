package com.zerogerc.photoframe.main;

import android.app.Application;
import android.content.Context;

/**
 * Singleton application.
 * Main advantage of using singleton is the ability of getting <code>Context</code> from anywhere.
 * As far as I know is common Android practice.
 */
public class PhotoFrameApp extends Application {
    public static final String USER_ID = "12c6774f2763474591d36590bb7b252b";

    public static final String SHARED_NAME = "prefs";
    public static final String SHARED_PREF_TOKEN = "token";
    public static final String SHARED_PREF_EXPIRE = "expire";

    private static PhotoFrameApp mInstance;

    private static PhotoFrameApp getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }
}
