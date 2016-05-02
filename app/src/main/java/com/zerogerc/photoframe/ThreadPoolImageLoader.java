package com.zerogerc.photoframe;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPoolImageLoader {
    public static final String BROADCAST_IMAGE_LOADED = "image_loaded";
    public static final String IMAGE_KEY = "image";

    private ExecutorService threadPool;
    private Handler handler;
    private static ThreadPoolImageLoader mInstance;

    static {
        mInstance = new ThreadPoolImageLoader();
    }

    public static ThreadPoolImageLoader getInstance() {
        return mInstance;
    }

    private ThreadPoolImageLoader() {
//        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * @param items images to download
     */
    public void startLoad(final Credentials credentials, final List<ListItem> items) {
        //Create new instance of threadPool because threadPool is not reusable.
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (final ListItem item : items) {
            if (item.getContentType().contains("image")) {
                threadPool.execute(new Runnable() {
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

    /**
     * Finish all task in pool.
     */
    public void shutdown() {
        threadPool.shutdownNow();
    }
}
