package com.zerogerc.photoframe.slideshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowActivity extends AppCompatActivity {
    public static final String ITEMS_KEY = "items";
    public static final String CREDENTIALS_KEY = "credentials";

    private static final String HAS_APPEARED_KEY = "appeared";
    private static final String HAS_FINISHED_LOAD_KEY = "finished";
    private static final String CURRENT_STEP_KEY = "step";

    private static final int checkPeriod = 1000;
    private static final int checksNumber = 5;
    private int currentCheckStep = 0;

    private Handler handler;

    private boolean firstImageAppeared = false;
    private ProgressBar progressBar;

    private Timer timer;
    private boolean hasFinishedLoad = false;
    private Queue<byte[]> loadedImages;
    private BroadcastReceiver mImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ThreadPoolImageLoader.BROADCAST_IMAGE_LOADED:
                    //force first image to appear after it's loaded
                    if (!firstImageAppeared) {
                        firstImageAppeared = true;
                        currentCheckStep = checksNumber + 1;
                    }
                    loadedImages.add(intent.getByteArrayExtra(ThreadPoolImageLoader.IMAGE_KEY));
                    progressBar.setVisibility(View.GONE);
                    break;
                case ThreadPoolImageLoader.BROADCAST_LOAD_FINISHED:
                    Log.d("LOAD", "FINISHED");
                    hasFinishedLoad = true;
                    finishWithDelayIfEmptyAndFinished();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        handler = new Handler();
        progressBar = ((ProgressBar) findViewById(R.id.activity_slideshow_progress));

        ArrayList<ListItem> items = getIntent().getParcelableArrayListExtra(ITEMS_KEY);
        Credentials credentials = getIntent().getParcelableExtra(CREDENTIALS_KEY);

        if (savedInstanceState == null) {
            loadedImages = new LinkedList<>();
            ThreadPoolImageLoader.getInstance().startLoad(credentials, items);
        } else {
            firstImageAppeared = savedInstanceState.getBoolean(HAS_APPEARED_KEY);
            hasFinishedLoad = savedInstanceState.getBoolean(HAS_FINISHED_LOAD_KEY);
            currentCheckStep = savedInstanceState.getInt(CURRENT_STEP_KEY);

            if (getLastCustomNonConfigurationInstance() != null) {
                loadedImages = ((Queue<byte[]>) getLastCustomNonConfigurationInstance());
            }
        }

        if (firstImageAppeared) {
            progressBar.setVisibility(View.GONE);
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mImageReceiver, new IntentFilter(ThreadPoolImageLoader.BROADCAST_IMAGE_LOADED));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mImageReceiver, new IntentFilter(ThreadPoolImageLoader.BROADCAST_LOAD_FINISHED));

        if (items != null && credentials != null) {
            startShow();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (loadedImages != null) {
            return loadedImages;
        }
        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    public Object getLastCustomNonConfigurationInstance() {
        return super.getLastCustomNonConfigurationInstance();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(HAS_APPEARED_KEY, firstImageAppeared);
        outState.putBoolean(HAS_FINISHED_LOAD_KEY, hasFinishedLoad);
        outState.putInt(CURRENT_STEP_KEY, currentCheckStep);
        super.onSaveInstanceState(outState);
    }

    private void startShow() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkIfExistsAndPost();
                        finishWithDelayIfEmptyAndFinished();
                    }
                });

            }
        }, 0, checkPeriod);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();

        ThreadPoolImageLoader.getInstance().shutdown();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageReceiver);
        super.onDestroy();
    }

    private void checkIfExistsAndPost() {
        if (++currentCheckStep < checksNumber) return;
        currentCheckStep = 0;

        if (loadedImages.size() > 2) {
            replaceContent(SlideshowTripleImageFragment.newInstance(loadedImages.poll(), loadedImages.poll(), loadedImages.poll()));
        } else if (loadedImages.size() > 1) {
            replaceContent(SlideshowDoubleImageFragment.newInstance(loadedImages.poll(), loadedImages.poll()));
        } else {
            if (loadedImages.size() > 0) {
                loadedImages.poll();
                replaceContent(SlideshowSingleImageFragment.newInstance(loadedImages.poll()));
            }
        }
    }

    private void replaceContent(final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_slideshow_fragment_container, fragment, "fragment")
                .commitAllowingStateLoss();
    }

    private void finishWithDelayIfEmptyAndFinished() {
        if (loadedImages.size() == 0 && hasFinishedLoad) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, checkPeriod * checksNumber);
        }
    }
}
