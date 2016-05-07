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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity for creating slideshow. Use method {@link #getIntentForStart(Context, ArrayList, Credentials)} fro invoking this activity.
 */
public class SlideshowActivity extends AppCompatActivity {
    private static final String TAG = "SLIDESHOW";

    /*
    Keys for passing objects through Intents and Bundles
     */
    private static final String ITEMS_KEY = "items";
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String HAS_APPEARED_KEY = "appeared";
    private static final String HAS_FINISHED_LOAD_KEY = "finished";
    private static final String NOT_FINISHED_IMAGES_KEY = "not_finished";
    private static final String CURRENT_STEP_KEY = "step";
    private static final String IMAGES_KEY = "images";

    /**
     * Amount of images for loading from {@link ThreadPoolImageLoader} at once.
     * This is especially important if user have many pictures and fast internet.
     * In that case {@link OutOfMemoryError} may occur.
     */
    private static final int LOAD_BUNCH = 6;

    /**
     * Timer periodicity
     */
    private static final int checkPeriod = 1000;

    /**
     * Minimum amount of seconds before pictures can change on the screen.
     */
    private static final int checksNumber = 5;

    /**
     * Current second of timer. Timer invokes every second but images should change every five seconds.
     */
    private int currentCheckStep = 0;

    /**
     * True if first image already on the screen.
     */
    private boolean firstImageAppeared = false;

    /**
     * True if this activity receives {@link ThreadPoolImageLoader#BROADCAST_LOAD_FINISHED}.
     */
    private boolean hasFinishedLoad = false;

    /**
     * Images that have loaded but haven't showed on the screen yet.
     */
    private ArrayList<Image> loadedImages;

    /**
     * Queue of {@link ListItem} for loading. They will be submitted to {@link ThreadPoolImageLoader}.
     */
    private Queue<ListItem> itemsForLoad;

    /**
     * Amount {@link ListItem} that was sent for load but wasn't showed on the screen yet.
     */
    private int notFinishedImages;

    /**
     * {@link Credentials} for loading files from yandex disk.
     */
    private Credentials credentials;

    private Handler handler;
    private ProgressBar progressBar;
    private Timer timer;

    /*
    Fragments that changes each other during slideshow. Just for beauty.
     */
    private SlideshowImageFragment singleImageFragment;
    private SlideshowImageFragment doubleImageFragment;
    private SlideshowImageFragment tripleImageFragment;

    /**
     * Receiver of broadcast from {@link ThreadPoolImageLoader}.
     */
    private BroadcastReceiver mImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ThreadPoolImageLoader.BROADCAST_IMAGE_LOADED:
                    loadedImages.add(new Image(intent.getByteArrayExtra(ThreadPoolImageLoader.IMAGE_KEY)));
                    progressBar.setVisibility(View.GONE);
                    //force first image to appear after it's loaded
                    if (!firstImageAppeared) {
                        firstImageAppeared = true;
                        currentCheckStep = checksNumber + 1;
                    }
                    break;
                case ThreadPoolImageLoader.BROADCAST_LOAD_FINISHED:
                    if (itemsForLoad.size() == 0) {
                        hasFinishedLoad = true;
                    }
                    break;
            }
        }
    };

    /**
     * Return intent for passing to {@link #startActivity(Intent)} in order to invoke this Activity.
     * @param context current context
     * @param items items for slideshow
     * @param credentials credentials for loading files from yandex disk
     * @return proper intent
     */
    public static Intent getIntentForStart(Context context, ArrayList<ListItem> items, Credentials credentials) {
        Intent intent = new Intent(context, SlideshowActivity.class);
        intent.putExtra(ITEMS_KEY, items);
        intent.putExtra(CREDENTIALS_KEY, credentials);
        return intent;
    }

    /**
     * Preforms loading of data from {@link Intent}.
     * @param intent proper intent
     */
    private void loadDataFromIntent(final Intent intent) {
        loadedImages = new ArrayList<>();
        ArrayList<ListItem> temp = intent.getParcelableArrayListExtra(ITEMS_KEY);
        if (temp != null) {
            itemsForLoad.addAll(temp);
        }
        credentials = intent.getParcelableExtra(CREDENTIALS_KEY);
        notFinishedImages = 0;

        ThreadPoolImageLoader.getInstance().initExecutor(credentials);
    }

    /**
     * Method for restoring state of activity after rotation. Performs loading of data from {@link Bundle}.
     * @param savedInstanceState proper bundle
     */
    private void loadDataFromBundle(final Bundle savedInstanceState) {
        firstImageAppeared = savedInstanceState.getBoolean(HAS_APPEARED_KEY);
        hasFinishedLoad = savedInstanceState.getBoolean(HAS_FINISHED_LOAD_KEY);
        currentCheckStep = savedInstanceState.getInt(CURRENT_STEP_KEY);
        loadedImages = savedInstanceState.getParcelableArrayList(IMAGES_KEY);
        ArrayList<ListItem> temp = savedInstanceState.getParcelableArrayList(ITEMS_KEY);
        if (temp != null) {
            itemsForLoad.addAll(temp);
        }

        credentials = savedInstanceState.getParcelable(CREDENTIALS_KEY);
        notFinishedImages = savedInstanceState.getInt(NOT_FINISHED_IMAGES_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        handler = new Handler();
        progressBar = ((ProgressBar) findViewById(R.id.activity_slideshow_progress));

        singleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.SINGLE);
        doubleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.DOUBLE);
        tripleImageFragment = SlideshowImageFragment.newInstance(SlideshowImageFragment.TRIPLE);

        itemsForLoad = new LinkedList<>();
        if (savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        } else {
            loadDataFromIntent(getIntent());
        }

        if (firstImageAppeared) {
            progressBar.setVisibility(View.GONE);
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mImageReceiver, new IntentFilter(ThreadPoolImageLoader.BROADCAST_IMAGE_LOADED));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mImageReceiver, new IntentFilter(ThreadPoolImageLoader.BROADCAST_LOAD_FINISHED));

        // If we have valid data just start otherwise finish.
        if (itemsForLoad.size() != 0 && credentials != null) {
            startShow();
        } else {
            hasFinishedLoad = true;
            finishWithDelayIfEmptyAndFinished();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(HAS_APPEARED_KEY, firstImageAppeared);
        outState.putBoolean(HAS_FINISHED_LOAD_KEY, hasFinishedLoad);
        outState.putInt(CURRENT_STEP_KEY, currentCheckStep);
        outState.putParcelableArrayList(IMAGES_KEY, loadedImages);
        outState.putParcelableArrayList(ITEMS_KEY, new ArrayList<>(itemsForLoad));
        outState.putParcelable(CREDENTIALS_KEY, credentials);
        outState.putInt(NOT_FINISHED_IMAGES_KEY, notFinishedImages);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        ThreadPoolImageLoader.getInstance().shutdown();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageReceiver);
        super.onDestroy();
    }

    /**
     * Send bunch of {@link ListItem} to {@link ThreadPoolImageLoader}.
     * @see #LOAD_BUNCH
     */
    private void loadBunch() {
        notFinishedImages += Math.min(itemsForLoad.size(), LOAD_BUNCH);
        List<ListItem> temp = new LinkedList<>();
        for (int i = 0; i < LOAD_BUNCH && itemsForLoad.size() != 0; i++) {
            temp.add(itemsForLoad.poll());
        }
        ThreadPoolImageLoader.getInstance().loadImages(temp);
    }

    /**
     * Initialize timer and start showing images from {@link #loadedImages}.
     */
    private void startShow() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkIfExistsAndPost();
                    }
                });

            }
        }, 0, checkPeriod);
    }

    /**
     * Check if minimum amount of seconds passed since last image change
     * and if {@link #loadedImages} not empty performs changing of images on the screen.
     */
    private void checkIfExistsAndPost() {
        if (++currentCheckStep < checksNumber) return;
        currentCheckStep = 0;

        if (loadedImages.size() == 0 && hasFinishedLoad) {
            finishWithDelayIfEmptyAndFinished();
        }

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

        notFinishedImages -= amount;

        //Start another load if we have few not showed but was sent for load images
        if (notFinishedImages < LOAD_BUNCH / 2) {
            loadBunch();
        }
    }

    /**
     * Replace current fragment by another.
     * @param fragment new fragment
     */
    private void replaceContent(final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_slideshow_fragment_container, fragment, "fragment")
                .commitAllowingStateLoss();
    }

    /**
     * If {@link #loadedImages} is empty and {@link #hasFinishedLoad} is <code>true</code> than finish slideshow.
     */
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
