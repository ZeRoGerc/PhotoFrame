package com.zerogerc.photoframe.main;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.R;
import com.zerogerc.photoframe.preview.PreviewActivity;
import com.zerogerc.photoframe.slideshow.SlideshowActivity;
import com.zerogerc.photoframe.util.FilesLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZeRoGerc on 27/04/16.
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ListItem>> {
    private static final String TAG = "FileListFragment";
    private static final String CREDENTIALS_KEY = "credentials";

    private static final String CURRENT_DIR_KEY = "current_directory";

    public static final String ROOT = "/";

    private static final String CONTENT_IMAGE = "image";

    private Credentials credentials;
    private String currentDir;

    private ListAdapter adapter;
    private ArrayList<ListItem> images;
    private ArrayList<Integer> numberOfImage;

    public static FileListFragment newInstance(final Credentials credentials, final String currentDir) {

        Bundle args = new Bundle();

        args.putParcelable(CREDENTIALS_KEY, credentials);
        args.putString(CURRENT_DIR_KEY ,currentDir);

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        setEmptyText(getString(R.string.no_files));

        credentials = getArguments().getParcelable(CREDENTIALS_KEY);

        Bundle args = getArguments();
        if (args != null) {
            currentDir = args.getString(CURRENT_DIR_KEY);
        }
        if (currentDir == null) {
            currentDir = ROOT;
        }

        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(!ROOT.equals(currentDir));
            bar.setTitle(currentDir);
        }
        adapter = new ListAdapter(getActivity());
        setListAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);

        setFABListener();
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<List<ListItem>> onCreateLoader(int id, Bundle args) {
        return new FilesLoader(getActivity(), credentials, currentDir);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_file_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
            case R.id.action_settings:
                break;
        }
        return true;
    }

    @Override
    public void onLoadFinished(Loader<List<ListItem>> loader, List<ListItem> data) {
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
        if (data.isEmpty()) {
            //TODO: show message on the empty screen
        } else {
            adapter.setData(data);
            images = new ArrayList<>();
            numberOfImage = new ArrayList<>();
            for (ListItem item : data) {
                if ((!item.isCollection()) && item.getContentType().contains(CONTENT_IMAGE)) {
                    numberOfImage.add(images.size());
                    images.add(item);
                } else {
                    numberOfImage.add(0);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ListItem>> loader) {
        adapter.setData(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ListItem item = ((ListItem) getListAdapter().getItem(position));
        if (item.isCollection()) {
            changeDir(item.getFullPath());
        } else {
            if (item.getContentType().contains("image")) {
                startActivity(PreviewActivity.getIntentForStart(getContext(), images, credentials, numberOfImage.get(position)));
//                replaceContent(PreviewFragment.newInstance(images, credentials, numberOfImage.get(position)));
            }
        }
    }

    private void replaceContent(final Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.file_list_fragment_container, fragment, FileListActivity.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    private void changeDir(final String dir) {
        FileListFragment fragment = FileListFragment.newInstance(credentials, dir);
        replaceContent(fragment);
    }

    private void setFABListener() {
        FloatingActionButton fab = ((FloatingActionButton) getActivity().findViewById(R.id.fab));
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (images != null && images.size() > 0) {
                        startActivity(SlideshowActivity.getIntentForStart(getActivity(), images, credentials));
                    }
                }
            });
        }

    }

    public static class ListAdapter extends ArrayAdapter<ListItem> {
        private final LayoutInflater inflater;

        public ListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<ListItem> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item, parent, false);
            } else {
                view = convertView;
            }

            ListItem item = getItem(position);
            loadContent(view, item);

            return view;
        }

        /**
         * Load content of {@link ListItem} to given <code>View</code>.
         * @param view given view
         * @param item given entity
         */
        private void loadContent(View view, ListItem item) {
            TextView title = ((TextView) view.findViewById(R.id.list_item_title));
            if (title != null) {
                title.setText(item.getDisplayName());
            }
            ImageView icon = ((ImageView) view.findViewById(R.id.list_item_icon));
            if (icon != null) {
                if (item.isCollection()) {
                    icon.setImageResource(R.drawable.ic_folder_black_48dp);
                } else {
                    if (item.getContentType().contains("image")) {
                        icon.setImageResource(R.drawable.ic_photo_black_48dp);
                    } else {
                        icon.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
                    }
                }
            }
        }
    }
}

