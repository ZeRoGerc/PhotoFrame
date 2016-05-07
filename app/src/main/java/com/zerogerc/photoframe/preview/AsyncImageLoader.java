package com.zerogerc.photoframe.preview;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;
import com.zerogerc.photoframe.util.ByteDownloader;

import java.io.IOException;

/**
 * Class for loading image of {@link ListItem}.
 */
public class AsyncImageLoader extends AsyncTaskLoader<ByteDownloader> {
    /**
     * {@link ListItem} for retrieving image.
     */
    private ListItem item;

    /**
     * Credentials for loading data from yandex disk.
     */
    private Credentials credentials;

    /**
     * Create new instance of {@link AsyncImageLoader}.
     * @param context current context
     * @param item {@link ListItem} for retrieving image.
     * @param credentials credentials for loading data from yandex disk.
     */
    public AsyncImageLoader(Context context, ListItem item, Credentials credentials) {
        super(context);
        this.item = item;
        this.credentials = credentials;
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ByteDownloader loadInBackground() {
        TransportClient client = null;
        try {
            final ByteDownloader downloader = new ByteDownloader();
            client = TransportClient.getInstance(getContext(), credentials);
            client.download(item.getFullPath(), downloader);
            //return downloader with loaded data
            return downloader;
        } catch (IOException | WebdavException ex) {
            ex.printStackTrace();
        } finally {
            TransportClient.shutdown(client);
        }
        return null;
    }
}
