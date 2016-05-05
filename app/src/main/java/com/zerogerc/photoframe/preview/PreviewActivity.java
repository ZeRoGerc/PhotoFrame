package com.zerogerc.photoframe.preview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity {
    public static final String ITEM_KEY = "item";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String INITIAL_ITEM_KEY = "initial";

    private ArrayList<ListItem> items;
    private Credentials credentials;

    public static Intent getIntentForStart(Context context, ArrayList<ListItem> images, Credentials credentials, int initialItem) {
        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(ITEM_KEY, images);
        intent.putExtra(CREDENTIALS_KEY, credentials);
        intent.putExtra(INITIAL_ITEM_KEY, initialItem);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int initial = 0;
        if (intent != null) {
            items = intent.getParcelableArrayListExtra(ITEM_KEY);
            credentials = intent.getParcelableExtra(CREDENTIALS_KEY);
            initial = intent.getIntExtra(INITIAL_ITEM_KEY, 0);
        }

        ViewPager pager = ((ViewPager) findViewById(R.id.preview_activity_pager));
        if (pager != null) {
            ImageAdapter pagerAdapter = new ImageAdapter(getSupportFragmentManager());
            pager.setAdapter(pagerAdapter);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    changeTitle(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            changeTitle(initial);
            pager.setCurrentItem(initial);
        }
    }

    private void changeTitle(int currentNumber) {
        android.support.v7.app.ActionBar bar = getSupportActionBar();
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
