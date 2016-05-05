package com.zerogerc.photoframe.preview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;
import com.zerogerc.photoframe.util.ByteDownloader;
import com.zerogerc.photoframe.R;

import java.io.IOException;

public class ImageFragment extends Fragment implements LoaderCallbacks<ByteDownloader>{
    public static final String ITEM_KEY = "item";
    public static final String CREDENTIALS_KEY = "credentials";

    private ListItem item;
    private Credentials credentials;
    private ImageView image;

    private ProgressBar progressBar;

    public static ImageFragment newInstance(ListItem image, Credentials credentials) {

        Bundle args = new Bundle();
        args.putParcelable(ITEM_KEY, image);
        args.putParcelable(CREDENTIALS_KEY, credentials);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_image_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            item = bundle.getParcelable(ITEM_KEY);
            credentials = bundle.getParcelable(CREDENTIALS_KEY);
        }

        image = ((ImageView) view.findViewById(R.id.preview_fragment_image));
        progressBar = ((ProgressBar) view.findViewById(R.id.preview_fragment_progress));

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ByteDownloader> onCreateLoader(int id, Bundle args) {
        return new AsyncImageLoader(getContext(), item, credentials);
    }

    @Override
    public void onLoadFinished(Loader<ByteDownloader> loader, ByteDownloader data) {
        progressBar.setVisibility(View.GONE);
        if (data != null) {
            //load image in the screen
            Glide.with(this).load(data.getData()).into(image);
        } else {
            //TODO: print error message on the screen
        }
    }

    @Override
    public void onLoaderReset(Loader<ByteDownloader> loader) {
        item = null;
    }

    private static class AsyncImageLoader extends AsyncTaskLoader<ByteDownloader> {
        private ListItem item;
        private Credentials credentials;

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
}
