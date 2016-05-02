package com.zerogerc.photoframe;

import android.app.Application;
import android.content.Context;

/**
 * Singleton application.
 * Main advantage of using singleton is the ability of getting <code>Context</code> from anywhere.
 * As far as I know is common Android practice.
 */
public class PhotoFrameApp extends Application {
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
