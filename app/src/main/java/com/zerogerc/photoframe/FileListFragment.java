package com.zerogerc.photoframe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;

import java.util.List;

/**
 * Created by ZeRoGerc on 27/04/16.
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<com.yandex.disk.client.ListItem>> {
    private static final String TAG = "ListExampleFragment";

    private static final String CURRENT_DIR_KEY = "example.current.dir";

    public static final String USER_ID = "12c6774f2763474591d36590bb7b252b";

    private static final int GET_FILE_TO_UPLOAD = 100;

    private static final String ROOT = "/";

    private Credentials credentials;
    private String currentDir;

    private ListExampleAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        registerForContextMenu(getListView());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        credentials = new Credentials(USER_ID, FileListActivity.accessToken);

        Bundle args = getArguments();
        if (args != null) {
            currentDir = args.getString(CURRENT_DIR_KEY);
        }
        if (currentDir == null) {
            currentDir = ROOT;
        }
//        getActivity().getActionBar().setDisplayHomeAsUpEnabled(!ROOT.equals(currentDir));

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
        }
    }

    private void changeDir(final String dir) {
        Bundle args = new Bundle();
        args.putString(CURRENT_DIR_KEY, dir);

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, FileListActivity.FRAGMENT_TAG)
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

