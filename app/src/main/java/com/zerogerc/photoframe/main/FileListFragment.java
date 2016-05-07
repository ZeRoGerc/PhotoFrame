package com.zerogerc.photoframe.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
 * Fragment that shows list of files with proper listeners.
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ListItem>> {
    private static final String TAG = "FileListFragment";

    /*
    Keys for passing data through Intent
     */
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String CURRENT_DIR_KEY = "directory";
    private static final String CURRENT_TITLE_KEY = "title";
    private static final String FILES_KEY = "files";

    /**
     * Root directory of disk.
     */
    public static final String ROOT = "/";

    /**
     * MIME type of image.
     */
    private static final String CONTENT_IMAGE = "image";

    /**
     * Credentials for loading data from yandex disk.
     */
    private Credentials credentials;

    /**
     * Directory of displaying list of files.
     */
    private String currentDir;

    /**
     * Adapter for list.
     */
    private ListAdapter adapter;

    /**
     * List of all files from {@link #currentDir}.
     */
    private ArrayList<ListItem> fileList;

    /**
     * List of all images from {@link #currentDir}.
     */
    private ArrayList<ListItem> images;

    /**
     * List that contains number of items form {@link #fileList} in {@link #images}.
     */
    private ArrayList<Integer> numberOfImage;

    /**
     * {@link SwipeRefreshLayout} layout of list.
     */
    private ListFragmentSwipeRefreshLayout mSwipeRefreshLayout;

    private Handler handler;

    /**
     * Create nes Instance of {@link FileListFragment}.
     * @param credentials credentials for loading data from yandex disk
     * @param currentDir directory of displaying list of files
     * @param currentTitle title of {@link ActionBar} to show
     * @return new instance of {@link FileListFragment}
     */
    public static FileListFragment newInstance(final Credentials credentials, final String currentDir, final String currentTitle) {
        Bundle args = new Bundle();

        args.putParcelable(CREDENTIALS_KEY, credentials);
        args.putString(CURRENT_DIR_KEY ,currentDir);
        args.putString(CURRENT_TITLE_KEY, currentTitle);

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create the list fragment's content view by calling the super method
        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Now return the SwipeRefreshLayout as this fragment's content view

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fileList = new ArrayList<>();
                images = new ArrayList<>();
                numberOfImage = new ArrayList<>();
                restartLoader();
            }
        });
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handler = new Handler();

        setHasOptionsMenu(true);

        setEmptyText(getString(R.string.no_files));

        credentials = getArguments().getParcelable(CREDENTIALS_KEY);

        Bundle args = getArguments();
        String title = getResources().getString(R.string.disk);
        if (args != null) {
            currentDir = args.getString(CURRENT_DIR_KEY);
            title = args.getString(CURRENT_TITLE_KEY);
        }
        if (currentDir == null) {
            currentDir = ROOT;
        }

        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(!ROOT.equals(currentDir));
            bar.setTitle(title);
        }
        adapter = new ListAdapter(getActivity());
        setListAdapter(adapter);
        setListShown(false);
        if (savedInstanceState == null) {
            getLoaderManager().initLoader(0, null, this);
        } else {
            fileList = savedInstanceState.getParcelableArrayList(FILES_KEY);
            if (fileList == null) {
                getLoaderManager().initLoader(0, null, this);
            } else {
                initFromFileList();
            }
        }

        setFABListener();
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
        }
        return true;
    }

    @Override
    public void onLoadFinished(Loader<List<ListItem>> loader, List<ListItem> data) {
        setListShown(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);

        if (data.isEmpty()) {
            if (((FilesLoader) loader).getException() != null) {
                setEmptyText(getString(R.string.no_internet));
            }
        } else {
            fileList = new ArrayList<>(data);
            initFromFileList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fileList != null) {
            outState.putParcelableArrayList(FILES_KEY, fileList);
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
            changeDir(item);
        } else {
            if (item.getContentType().contains(CONTENT_IMAGE)) {
                startActivity(PreviewActivity.getIntentForStart(getContext(), images, credentials, numberOfImage.get(position)));
            }
        }
    }

    @Override
    public void onStop() {
        mSwipeRefreshLayout.setRefreshing(false);
        setListShown(false);
        super.onStop();
    }

    /**
     * Restarts current loader
     */
    public void restartLoader() {
        fileList = new ArrayList<>();
        images = new ArrayList<>();
        numberOfImage = new ArrayList<>();
        getLoaderManager().restartLoader(0, null, this);
    }


    /**
     * Init need data from correct {@link #fileList}.
     */
    private void initFromFileList() {
        adapter.setData(fileList);
        setListShown(true);
        images = new ArrayList<>();
        numberOfImage = new ArrayList<>();
        for (ListItem item : fileList) {
            if ((!item.isCollection()) && item.getContentType().contains(CONTENT_IMAGE)) {
                numberOfImage.add(images.size());
                images.add(item);
            } else {
                numberOfImage.add(0);
            }
        }
    }

    /**
     * Replace current fragment with another. Current fragment will be saved on stack.
     * @param fragment new fragment
     */
    private void replaceContent(final FileListFragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.file_list_fragment_container, fragment, fragment.currentDir)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Replace current fragment with fragment that corresponds to given {@link ListItem}.
     * @param item given {@link ListItem}
     */
    private void changeDir(final ListItem item) {
        FileListFragment fragment = FileListFragment.newInstance(credentials, item.getFullPath(), item.getDisplayName());
        replaceContent(fragment);
    }

    /**
     * Set listener to floating action button of activity.
     */
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

    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        return ViewCompat.canScrollVertically(listView, -1);
    }

    /**
     * Adapter for list of files.
     */
    public static class ListAdapter extends ArrayAdapter<ListItem> {
        private final LayoutInflater inflater;

        public ListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Set given list to this adapter.
         * @param data given list
         */
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
                    if (item.getContentType().contains(CONTENT_IMAGE)) {
                        icon.setImageResource(R.drawable.ic_photo_black_48dp);
                    } else {
                        icon.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
                    }
                }
            }
        }
    }

    /**
     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
     * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
     * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
     * expects to be the one which triggers refreshes. In our case the layout's child is the content
     * view returned from
     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * which is a {@link android.view.ViewGroup}.
     *
     * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
     * override the default behavior and properly signal when a gesture is possible. This is done by
     * overriding {@link #canChildScrollUp()}.
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }
}

