package com.zerogerc.photoframe.preview;

import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;

import java.util.ArrayList;

/**
 * Created by ZeRoGerc on 02/05/16.
 */
public class PreviewFragment extends Fragment {
    public static final String ITEM_KEY = "item";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String INITIAL_ITEM_KEY = "initial";

    private ArrayList<ListItem> items;
    private Credentials credentials;

    private String previousTitle;

    public static PreviewFragment newInstance(ArrayList<ListItem> images, Credentials credentials, int initialItem) {

        Bundle args = new Bundle();
        args.putParcelableArrayList(ITEM_KEY, images);
        args.putParcelable(CREDENTIALS_KEY, credentials);
        args.putInt(INITIAL_ITEM_KEY, initialItem);

        PreviewFragment fragment = new PreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        int initial = 0;
        if (bundle != null) {
            items = bundle.getParcelableArrayList(ITEM_KEY);
            credentials = bundle.getParcelable(CREDENTIALS_KEY);
            initial = bundle.getInt(INITIAL_ITEM_KEY);
        }

        ViewPager pager = ((ViewPager) view.findViewById(R.id.preview_fragment_pager));
        ImageAdapter pagerAdapter = new ImageAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                changeTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        changeTitle(initial);
        pager.setCurrentItem(initial);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            previousTitle = bar.getTitle().toString();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (previousTitle != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle(previousTitle);
        }
    }

    private void changeTitle(int currentNumber) {
        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            Resources resources = getResources();
            String title = String.format(resources.getString(R.string.pager_title), currentNumber + 1, items.size());
            bar.setTitle(title);
        }
    }

    private class ImageAdapter extends FragmentStatePagerAdapter {

        public ImageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(items.get(position), credentials);
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }
}
