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
import java.util.concurrent.RejectedExecutionException;


/**
 * Singleton class for loading images from yandex disk.
 * It sends {@link #BROADCAST_IMAGE_LOADED} with loaded image after every image load.
 * It sends {@link #BROADCAST_LOAD_FINISHED} if all load task finished.
 */
public class ThreadPoolImageLoader {
    /**
     * This <code>Broadcast</code> is sent after every image load.
     */
    public static final String BROADCAST_IMAGE_LOADED = "image_loaded";
    /**
     * This <code>Broadcast</code> is sent after all load tasks have finished.
     */
    public static final String BROADCAST_LOAD_FINISHED = "load_finished";
    /**
     * Key for retrieving image from Broadcast.
     */
    public static final String IMAGE_KEY = "image";

    /**
     * Service for loading images in many threads.
     */
    private ExecutorService threadPool;

    private Handler handler;

    //TODO: use phaser
    /**
     * Amount of not loaded images.
     */
    private int remainImages = 0;

    /**
     * Credentials for loading files from yandex disk.
     */
    private Credentials credentials;

    /**
     * Instance of this class.
     */
    private static ThreadPoolImageLoader mInstance;
    static {
        mInstance = new ThreadPoolImageLoader();
    }

    /**
     * Return current instance of {@link ThreadPoolImageLoader}.
     * @return current instance
     */
    public static ThreadPoolImageLoader getInstance() {
        return mInstance;
    }

    private ThreadPoolImageLoader() {
        handler = new Handler(Looper.getMainLooper());
    }


    /**
     * Reinit {@link ThreadPoolImageLoader} with empty tasks pool.
     * @param credentials credentials for loading files from yandex disk
     *                    @see Credentials
     */
    public void initExecutor(final Credentials credentials) {
        this.credentials = credentials;
        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.remainImages = 0;
    }

    /**
     * Add items pool of task for downloading.
     * @param items images to download
     */
    public void loadImages(final List<ListItem> items) {
        //Create new instance of threadPool because threadPool is not reusable.
        for (final ListItem item : items) {
            if (item.getContentType().contains("image")) {
                remainImages++;
                try {
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
                                decreaseCounter();
                            }
                        }
                    });
                } catch (RejectedExecutionException e) {
                    //If task cannot been executed just decrease counter
                    decreaseCounter();
                }
            }
        }
    }

    private void decreaseCounter() {
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

    /**
     * Send broadcast with loaded image. The identifier of <code>Broadcast</code> is {@link #BROADCAST_IMAGE_LOADED}.
     * @param image data to send
     *              @see #BROADCAST_IMAGE_LOADED
     */
    private void sendMessage(byte[] image) {
        Intent intent = new Intent(BROADCAST_IMAGE_LOADED);
        intent.putExtra(IMAGE_KEY, image);
        LocalBroadcastManager.getInstance(PhotoFrameApp.getContext()).sendBroadcast(intent);
    }

    /**
     * Send finish broadcast.
     * @see #BROADCAST_LOAD_FINISHED
     */
    private void sendFinishBroadcast() {
        final Intent intent = new Intent(BROADCAST_LOAD_FINISHED);
        LocalBroadcastManager.getInstance(PhotoFrameApp.getContext()).sendBroadcast(intent);
    }

    /**
     * Finish all task in pool.
     */
    public void shutdown() {
        remainImages = 0;
        sendFinishBroadcast();
        threadPool.shutdownNow();
        threadPool = null;
    }
}
