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
 * Created by ZeRoGerc on 03/05/16.
 */
public class SlideshowDoubleImageFragment extends Fragment {
    public static final String FIRST_IMAGE_KEY = "first_image";
    public static final String SECOND_IMAGE_KEY = "second_image";

    public static SlideshowDoubleImageFragment newInstance(byte[] image1, byte[] image2) {

        Bundle args = new Bundle();
        args.putByteArray(FIRST_IMAGE_KEY, image1);
        args.putByteArray(SECOND_IMAGE_KEY, image2);

        SlideshowDoubleImageFragment fragment = new SlideshowDoubleImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slideshow_double_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView firstView = ((ImageView) view.findViewById(R.id.slideshow_double_image_first));
        ImageView secondView = ((ImageView) view.findViewById(R.id.slideshow_double_image_second));

        Bundle bundle = getArguments();
        if (bundle != null) {
            //load data on all ImageViews
            Glide.with(this).load(bundle.getByteArray(FIRST_IMAGE_KEY)).into(firstView);
            Glide.with(this).load(bundle.getByteArray(SECOND_IMAGE_KEY)).into(secondView);
        }
    }
}
