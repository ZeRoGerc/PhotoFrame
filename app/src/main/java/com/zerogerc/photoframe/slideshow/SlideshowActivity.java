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
import android.view.View;
import android.widget.ProgressBar;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowActivity extends AppCompatActivity {
    public static final String ITEMS_KEY = "items";
    public static final String CREDENTIALS_KEY = "credentials";

    private static final String HAS_APPEARED_KEY = "appeared";
    private static final String HAS_FINISHED_LOAD_KEY = "finished";
    private static final String CURRENT_STEP_KEY = "step";
    private static final String IMAGES_KEY = "images";

    private static final int checkPeriod = 1000;
    private static final int checksNumber = 5;

    private int currentCheckStep = 0;
    private Handler handler;
    private boolean firstImageAppeared = false;
    private ProgressBar progressBar;

    private Timer timer;
    private boolean hasFinishedLoad = false;
    private ArrayList<Image> loadedImages;

    private SlideshowImageFragment singleImageFragment;
    private SlideshowImageFragment doubleImageFragment;
    private SlideshowImageFragment tripleImageFragment;

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
                    loadedImages.add(new Image(intent.getByteArrayExtra(ThreadPoolImageLoader.IMAGE_KEY)));
                    progressBar.setVisibility(View.GONE);
                    break;
                case ThreadPoolImageLoader.BROADCAST_LOAD_FINISHED:
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

        singleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.SINGLE);
        doubleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.DOUBLE);
        tripleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.TRIPLE);

        if (savedInstanceState == null) {
            loadedImages = new ArrayList<>();
            ThreadPoolImageLoader.getInstance().startLoad(credentials, items);
        } else {
            firstImageAppeared = savedInstanceState.getBoolean(HAS_APPEARED_KEY);
            hasFinishedLoad = savedInstanceState.getBoolean(HAS_FINISHED_LOAD_KEY);
            currentCheckStep = savedInstanceState.getInt(CURRENT_STEP_KEY);
            loadedImages = savedInstanceState.getParcelableArrayList(IMAGES_KEY);
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
        outState.putParcelableArrayList(IMAGES_KEY, loadedImages);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        ThreadPoolImageLoader.getInstance().shutdown();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        timer.cancel();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageReceiver);
        super.onDestroy();
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

    private void checkIfExistsAndPost() {
        if (++currentCheckStep < checksNumber) return;
        currentCheckStep = 0;

        int amount = 0;
        SlideshowImageFragment fragment = null;
        if (loadedImages.size() > 2) {
            fragment = tripleImageFragment;
            amount = 3;
        } else if (loadedImages.size() > 1) {
            fragment = doubleImageFragment;
            amount = 2;
        } else if (loadedImages.size() > 0) {
            fragment = singleImageFragment;
            amount = 1;
        }

        if (fragment != null) {
            Image[] images = new Image[amount];
            loadedImages.subList(0, amount).toArray(images);
            fragment.prepareImages(images);
            for (int i = 0; i < amount; i++) {
                loadedImages.remove(0);
            }
            replaceContent(fragment);
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
                    ThreadPoolImageLoader.getInstance().shutdown();
                    finish();
                }
            }, checkPeriod * checksNumber);
        }
    }
}
