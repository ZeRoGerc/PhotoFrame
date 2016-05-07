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
import com.zerogerc.photoframe.R;
import com.zerogerc.photoframe.util.ByteDownloader;

import java.io.IOException;

/**
 * Fragment that shows one Image.
 */
public class ImageFragment extends Fragment implements LoaderCallbacks<ByteDownloader>{
    /*
    Keys for passing objects through intent
     */
    private static final String ITEM_KEY = "item";
    private static final String CREDENTIALS_KEY = "credentials";

    /**
     * Item for retrieving image.
     */
    private ListItem item;

    /**
     * {@link Credentials} for loading data from yandex disk.
     */
    private Credentials credentials;

    /**
     * Image on the fragment.
     */
    private ImageView image;

    /**
     * ProgressBar on the fragment.
     */
    private ProgressBar progressBar;

    /**
     * Create new instance of the {@link ImageFragment}
     * @param image item fro loading image
     * @param credentials credentials for loading data from yandex disk
     * @return instance if {@link ImageFragment}
     */
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
}
