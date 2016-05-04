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
public class SlideshowTripleImageFragment extends Fragment {
    public static final String FIRST_IMAGE_KEY = "first_image";
    public static final String SECOND_IMAGE_KEY = "second_image";
    public static final String THIRD_IMAGE_KEY = "third_image";

    public static SlideshowTripleImageFragment newInstance(byte[] image1, byte[] image2, byte[] image3) {

        Bundle args = new Bundle();
        args.putByteArray(FIRST_IMAGE_KEY, image1);
        args.putByteArray(SECOND_IMAGE_KEY, image2);
        args.putByteArray(THIRD_IMAGE_KEY, image3);

        SlideshowTripleImageFragment fragment = new SlideshowTripleImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slideshow_triple_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView image1 = ((ImageView) view.findViewById(R.id.slideshow_triple_image_first));
        ImageView image2 = ((ImageView) view.findViewById(R.id.slideshow_triple_image_second));
        ImageView image3 = ((ImageView) view.findViewById(R.id.slideshow_triple_image_third));

        Bundle bundle = getArguments();
        if (bundle != null) {
            //Just load our data on all available ImageViews
            Glide.with(this).load(bundle.getByteArray(FIRST_IMAGE_KEY)).into(image1);
            Glide.with(this).load(bundle.getByteArray(SECOND_IMAGE_KEY)).into(image2);
            Glide.with(this).load(bundle.getByteArray(THIRD_IMAGE_KEY)).into(image3);
        }
    }
}
