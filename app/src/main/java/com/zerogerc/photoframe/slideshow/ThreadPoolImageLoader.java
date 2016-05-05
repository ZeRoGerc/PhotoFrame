package com.zerogerc.photoframe.slideshow;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;
import com.zerogerc.photoframe.main.PhotoFrameApp;
import com.zerogerc.photoframe.util.ByteDownloader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPoolImageLoader {
    public static final String BROADCAST_IMAGE_LOADED = "image_loaded";
    public static final String BROADCAST_LOAD_FINISHED = "load_finished";
    public static final String IMAGE_KEY = "image";

    private ExecutorService threadPool;
    private Handler handler;
    private int remainImages = 0;

    private static ThreadPoolImageLoader mInstance;

    static {
        mInstance = new ThreadPoolImageLoader();
    }

    public static ThreadPoolImageLoader getInstance() {
        return mInstance;
    }

    private ThreadPoolImageLoader() {
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param items images to download
     */
    public void startLoad(final Credentials credentials, final List<ListItem> items) {
        //Create new instance of threadPool because threadPool is not reusable.
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        remainImages = 0;

        for (final ListItem item : items) {
            if (item.getContentType().contains("image")) {
                remainImages++;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        TransportClient client = null;
                        try {
                            final ByteDownloader downloader = new ByteDownloader();
                            client = TransportClient.getInstance(PhotoFrameApp.getContext(), credentials);
                            client.download(item.getFullPath(), downloader);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    sendMessage(downloader.getData());
                                }
                            });
                        } catch (IOException | WebdavException ex) {
                            ex.printStackTrace();
                        } finally {
                            TransportClient.shutdown(client);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("REMAIN", Integer.toString(remainImages));
                                    if (--remainImages == 0) {
                                        sendFinishBroadcast();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    /**
     * Send broadcast with loaded image. The identifier of <code>Broadcast</code> is {@link #BROADCAST_IMAGE_LOADED}.
     * @param image data to send
     */
    private void sendMessage(byte[] image) {
        Intent intent = new Intent(BROADCAST_IMAGE_LOADED);
        intent.putExtra(IMAGE_KEY, image);
        LocalBroadcastManager.getInstance(PhotoFrameApp.getContext()).sendBroadcast(intent);
    }

    private void sendFinishBroadcast() {
        Log.e("FINISH", "SENDED");
        final Intent intent = new Intent(BROADCAST_LOAD_FINISHED);
        LocalBroadcastManager.getInstance(PhotoFrameApp.getContext()).sendBroadcast(intent);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager.getInstance(PhotoFrameApp.getContext()).sendBroadcast(intent);
            }
        }, 3000);
    }

    /**
     * Finish all task in pool.
     */
    public void shutdown() {
        threadPool.shutdownNow();
    }
}
