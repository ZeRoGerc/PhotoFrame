package com.zerogerc.photoframe.slideshow;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.zerogerc.photoframe.R;
import com.zerogerc.photoframe.main.PhotoFrameApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment for showing images on the screen.
 */
public class SlideshowImageFragment extends Fragment {
    private static final String TAG = "SLIDESHOW_FRAGMENT";

    /*
    Keys of passing data through Intent
     */
    private static final String IMAGES_KEY = "images";
    private static final String AMOUNT_KEY = "amount";

    /**
     * Type of fragment with one image.
     */
    public static final int SINGLE = 1;

    /**
     *  Type of fragment with two images.
     */
    public static final int DOUBLE = 2;

    /**
     * Type of fragment with three images.
     */
    public static final int TRIPLE = 3;


    private List<ImageView> imageViews;

    private ArrayList<Image> images;

    private Handler handler;

    /**
     * Get instance of {@link SlideshowImageFragment} with proper type.
     * @param type proper type. Can be {@link #SINGLE}, {@link #DOUBLE} or {@link #TRIPLE}.
     * @return instance of {@link SlideshowImageFragment}
     */
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
        handler = new Handler();
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

        /*
        Load data using Glide with proper animations
         */
        if (images != null) {
            int delay = 200;
            for (int i = 0; i < images.size(); i++) {
                final int copy = i;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(PhotoFrameApp.getContext()).load(images.get(copy).getData()).animate(animationObject).into(imageViews.get(copy));
                    }
                }, i * delay);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(IMAGES_KEY, images);
        super.onSaveInstanceState(outState);
    }

    /**
     * Set images to this fragment.
     * @param imgs images to show in Views of this fragment
     */
    public void prepareImages(Image... imgs) {
        images.clear();
        Collections.addAll(images, imgs);
    }

    /**
     * Get id of first, second or third {@link ImageView} from xml resource.
     * @param i number of image. Must be 0 <= i < 3
     * @return id of {@link ImageView} from xml resource
     */
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

    /**
     * Get proper layout for fragment type
     * @param type {@link #SINGLE}, {@link #DOUBLE} or {@link #TRIPLE}.
     * @return proper layout
     */
    private static int getLayout(int type) {
        switch (type) {
            case SINGLE:
                return R.layout.slideshow_single_image;
            case DOUBLE:
                return R.layout.slideshow_double_image;
            default: //TRIPLE
                return R.layout.slideshow_triple_image;
        }
    }


    /**
     * Animation for {@link ImageView}. Fast scale on slow alpha simultaneously started.
     */
    private static ViewPropertyAnimation.Animator animationObject = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            // if it's a custom view class, cast it here
            // then find subviews and do the animations
            // here, we just use the entire view for the fade animation
            view.setAlpha(0f);

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f );
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
            fadeAnim.setDuration(500);
            scaleX.setDuration(100);
            scaleY.setDuration(100);

            fadeAnim.start();
            scaleX.start();
            scaleY.start();
        }
    };
}
