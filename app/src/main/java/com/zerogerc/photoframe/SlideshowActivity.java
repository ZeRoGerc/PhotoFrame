package com.zerogerc.photoframe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowActivity extends AppCompatActivity {
    public static final String ITEMS_KEY = "items";
    public static final String CREDENTIALS_KEY = "credentials";

    private Handler handler;

    private ArrayList<ListItem> items;
    private Credentials credentials;

    private Queue<byte[]> loadedImages;
    private BroadcastReceiver mImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadedImages.add(intent.getByteArrayExtra(ThreadPoolImageLoader.IMAGE_KEY));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        loadedImages = new LinkedList<>();

        items = getIntent().getParcelableArrayListExtra(ITEMS_KEY);
        credentials = getIntent().getParcelableExtra(CREDENTIALS_KEY);

        handler = new Handler();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mImageReceiver, new IntentFilter(ThreadPoolImageLoader.BROADCAST_IMAGE_LOADED));

        if (items != null && credentials != null) {
            startShow();
        }
    }

    private void startShow() {
        ThreadPoolImageLoader loader = ThreadPoolImageLoader.getInstance();
        loader.startLoad(credentials, items);

        int period = 5000;

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkIfExistsAndPost();
                    }
                });

            }
        }, 0, period);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mImageReceiver);
        super.onDestroy();
    }

    private void checkIfExistsAndPost() {
        if (loadedImages.size() > 0) {
            replaceContent(SlideshowSingleImageFragment.newInstance(loadedImages.poll()));
        }
    }

    private void replaceContent(final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.slideshow_fragment_container, fragment, "fragment")
                .addToBackStack(null)
                .commit();
    }
}
