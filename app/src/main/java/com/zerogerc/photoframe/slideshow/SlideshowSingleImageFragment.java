package com.zerogerc.photoframe.slideshow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zerogerc.photoframe.R;

/**
 * Created by ZeRoGerc on 02/05/16.
 */
public class SlideshowSingleImageFragment extends Fragment {
    public static final String IMAGE_KEY = "image_key";

    private ImageView imageView;

    public static SlideshowSingleImageFragment newInstance(byte[] image) {

        Bundle args = new Bundle();
        args.putByteArray(IMAGE_KEY, image);

        SlideshowSingleImageFragment fragment = new SlideshowSingleImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slideshow_single_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = ((ImageView) view.findViewById(R.id.slideshow_single_image));

        Bundle bundle = getArguments();
        if (bundle != null) {
            //Just load our data on single available imageView
            Glide.with(this).load(bundle.getByteArray(IMAGE_KEY)).into(imageView);
        }
    }
}
