package com.zerogerc.photoframe.main;

import android.app.Application;
import android.content.Context;

/**
 * Singleton application.
 * Main advantage of using singleton is the ability of getting <code>Context</code> from anywhere.
 */
public class PhotoFrameApp extends Application {
    public static final String USER_ID = "12c6774f2763474591d36590bb7b252b";

    /**
     * Name of shared preferences to store all data.
     */
    public static final String SHARED_NAME = "prefs";

    /**
     * Key that stores <code>OAuth</code> token in {@link android.content.SharedPreferences}.
     */
    public static final String SHARED_PREF_TOKEN = "token";

    /**
     * Key that stores expire date of <code>OAuth</code> token if {@link android.content.SharedPreferences}.
     */
    public static final String SHARED_PREF_EXPIRE = "expire";

    /**
     * Instance of application.
     */
    private static PhotoFrameApp mInstance;

    /**
     * Get current application context.
     * @return current application context
     */
    public static Context getContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }
}
