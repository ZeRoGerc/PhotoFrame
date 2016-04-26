package com.zerogerc.photoframe;

import android.content.Context;
import android.os.AsyncTask;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.ListParsingHandler;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;

import java.io.IOException;

/**
 * Class for loading all files from given Directory. All entities received one by one in {@link #onProgressUpdate(Object[])}
 * You should pass directory to {@link #doInBackground(String...)}
 */
public class FilesLoader extends AsyncTask<String, HierarchyEntity, Boolean> {
    private Credentials credentials;
    private Context context;

    /**
     * Credentials used for loading files.
     * @param context current context
     * @param credentials given credentials
     */
    public FilesLoader(Context context, Credentials credentials) {
        this.context = context;
        this.credentials = credentials;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            final TransportClient client = TransportClient.getInstance(context, credentials);
            client.getList(params[0], new ParsingHandler());
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        } catch (WebdavException e2) {
            e2.printStackTrace();
            return false;
        }
        return true;
    }

    private class ParsingHandler extends ListParsingHandler {
        @Override
        public boolean handleItem(ListItem item) {
            publishProgress(new HierarchyEntity(item));
            return true;
        }
    }
}
