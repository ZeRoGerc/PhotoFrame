package com.zerogerc.photoframe;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yandex.disk.client.ListItem;
import com.zerogerc.photoframe.adapter.BaseAdapter;
import com.zerogerc.photoframe.login.LoginActivity;

import java.util.List;

public class FileListActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MAIN";

    private static final int LOGIN_CODE = 1;

    public static final String USER_ID = "12c6774f2763474591d36590bb7b252b";

    public static final String FRAGMENT_TAG = "list_fragment";

    private static final String ROOT = "/";

    public static String accessToken;

    private BaseAdapter adapter;

    private List<ListItem> files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getIntent() != null && getIntent().getData() != null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_CODE);
//        }

        setContentView(R.layout.activity_file_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFAB();
//        initRecyclerView();
//        if (savedInstanceState == null) {
//            startLoading();
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize recycler view of MainActivity
     */
    private void initRecyclerView() {
//        files = new ArrayList<>();
//
//        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.file_list_recycler_view));
//        if (recyclerView != null) {
//            recyclerView.setHasFixedSize(true);
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//            adapter = new BaseAdapter(this, files);
//            recyclerView.setAdapter(adapter);
//        }
    }


    /**
     * Initialize floating action button of MainActivity and set proper listeners.
     */
    private void initFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
    }

    private void startLoading() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FileListFragment(), FRAGMENT_TAG)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOGIN_CODE:
                if (resultCode == RESULT_OK) {
                    accessToken = data.getStringExtra(LoginActivity.ACCESS_TOKEN_KEY);
                    startLoading();
                } else {
                    Log.e(LOG_TAG, "Error while retrieving access token");
                }
        }
    }
}
