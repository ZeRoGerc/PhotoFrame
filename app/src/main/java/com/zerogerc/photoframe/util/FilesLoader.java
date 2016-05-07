package com.zerogerc.photoframe.util;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.ListParsingHandler;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that loads all files as {@link ListItem} from given directory.
 */
public class FilesLoader extends AsyncTaskLoader<List<ListItem>> {
    private static final String LOG_TAG = "FilesLoader";

    /**
     * Credentials for loading data from yandex disk.
     */
    private final Credentials credentials;

    /**
     * Directory to load file list from.
     */
    private final String dir;

    private final Handler handler;

    /**
     * Stores list of files from {@link #dir}.
     */
    private List<ListItem> fileList;

    /**
     * Items retrieved per request.
     */
    private static final int ITEMS_PER_REQUEST = 20;

    /**
     * Create new Instance of {@link FilesLoader}.
     * @param context current context
     * @param credentials credentials for loading data from yandex disk
     * @param dir directory to load file list from
     */
    public FilesLoader(Context context, Credentials credentials, String dir) {
        super(context);
        this.credentials = credentials;
        this.dir = dir;
        this.handler = new Handler();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<ListItem> loadInBackground() {
        fileList = new ArrayList<>();
        TransportClient client = null;
        try {
            client = TransportClient.getInstance(getContext(), credentials);
            client.getList(dir, ITEMS_PER_REQUEST, new ListParsingHandler() {
                // First item in PROPFIND is the current collection name
                boolean ignoreFirstItem = true;

                @Override
                public void onPageFinished(int itemsOnPage) {
                    ignoreFirstItem = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            deliverResult(fileList);
                        }
                    });
                    super.onPageFinished(itemsOnPage);
                }

                @Override
                public boolean handleItem(ListItem item) {
                    if (ignoreFirstItem) {
                        ignoreFirstItem = false;
                        return false;
                    } else {
                        fileList.add(item);
                        return true;
                    }
                }
            });
        } catch (WebdavException | IOException e) {
            return fileList;
        } finally {
            if (client != null) {
                TransportClient.shutdown(client);
            }
        }
        return fileList;
    }
}
