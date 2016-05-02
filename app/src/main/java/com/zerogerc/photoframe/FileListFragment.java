package com.zerogerc.photoframe;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZeRoGerc on 27/04/16.
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ListItem>> {
    private static final String TAG = "FileListFragment";

    private static final String CURRENT_DIR_KEY = "example.current.dir";

    public static final String USER_ID = "12c6774f2763474591d36590bb7b252b";

    private static final int GET_FILE_TO_UPLOAD = 100;

    private static final String ROOT = "/";

    private static final String CONTENT_IMAGE = "image";

    private Credentials credentials;
    private String currentDir;

    private ListExampleAdapter adapter;
    private ArrayList<ListItem> images;
    private ArrayList<Integer> numberOfImage;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_files));

        setHasOptionsMenu(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        credentials = new Credentials(USER_ID, FileListActivity.accessToken);

        Bundle args = getArguments();
        if (args != null) {
            currentDir = args.getString(CURRENT_DIR_KEY);
        }
        if (currentDir == null) {
            currentDir = ROOT;
        }

        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(!ROOT.equals(currentDir));
            bar.setTitle(currentDir);
        }
        adapter = new ListExampleAdapter(getActivity());
        setListAdapter(adapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
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
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_file_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(getContext(), SlideshowActivity.class);
                intent.putExtra(SlideshowActivity.ITEMS_KEY, images);
                intent.putExtra(SlideshowActivity.CREDENTIALS_KEY, credentials);
                startActivity(intent);
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
                replaceContent(PreviewFragment.newInstance(images, credentials, numberOfImage.get(position)));
            }
        }
    }

    private void replaceContent(final Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, FileListActivity.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    private void changeDir(final String dir) {
        Bundle args = new Bundle();
        args.putString(CURRENT_DIR_KEY, dir);

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment, FileListActivity.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public static class ListExampleAdapter extends ArrayAdapter<ListItem> {
        private final LayoutInflater inflater;

        public ListExampleAdapter(Context context) {
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
                view = inflater.inflate(R.layout.layout_hierarchy_entity, parent, false);
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
            TextView title = ((TextView) view.findViewById(R.id.hierarchy_entity_title));
            if (title != null) {
                title.setText(item.getDisplayName());
            }
        }
    }
}

