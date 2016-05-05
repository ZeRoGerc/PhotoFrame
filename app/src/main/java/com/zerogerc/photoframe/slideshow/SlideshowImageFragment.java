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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlideshowImageFragment extends Fragment {
    private static final String TAG = "SLIDESHOW_FRAGMENT";

    private static final String IMAGES_KEY = "images";
    private static final String AMOUNT_KEY = "amount";

    public static final int SINGLE = 1;
    public static final int DOUBLE = 2;
    public static final int TRIPLE = 3;

    private List<ImageView> imageViews;
    private ArrayList<Image> images;

    public static SlideshowImageFragment newInstance(int type) {

        Bundle args = new Bundle();

        args.putInt(AMOUNT_KEY, type);
        SlideshowImageFragment fragment = new SlideshowImageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public SlideshowImageFragment() {
        imageViews = new ArrayList<>();
        images = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayout(getArguments().getInt(AMOUNT_KEY)), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViews = new ArrayList<>();
        for (int i = 0; i < getArguments().getInt(AMOUNT_KEY); i++) {
            imageViews.add(((ImageView) view.findViewById(getImageId(i))));
        }

        if (savedInstanceState != null) {
            images = savedInstanceState.getParcelableArrayList(IMAGES_KEY);
        }

        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                Glide.with(this).load(images.get(i).getData()).into(imageViews.get(i));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(IMAGES_KEY, images);
        super.onSaveInstanceState(outState);
    }

    public void prepareImages(Image... imgs) {
        images.clear();
        Collections.addAll(images, imgs);
    }

    private static int getImageId(int i) {
        switch (i) {
            case 0:
                return R.id.slideshow_image_first;
            case 1:
                return R.id.slideshow_image_second;
            default:
                return R.id.slideshow_image_third;
        }
    }

    private static int getLayout(int amount) {
        switch (amount) {
            case 1:
                return R.layout.slideshow_single_image;
            case 2:
                return R.layout.slideshow_double_image;
            default:
                return R.layout.slideshow_triple_image;
        }
    }
}
