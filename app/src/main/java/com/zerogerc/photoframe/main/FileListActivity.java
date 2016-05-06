package com.zerogerc.photoframe.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.yandex.disk.client.Credentials;
import com.zerogerc.photoframe.R;
import com.zerogerc.photoframe.login.LoginActivity;

import java.util.Calendar;

public class FileListActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MAIN";

    private static final int LOGIN_CODE = 1;

    public static final String FRAGMENT_TAG = "list_fragment";

    private Credentials credentials;

//    private FileListFragment fileListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences(PhotoFrameApp.SHARED_NAME, MODE_PRIVATE);
        long valid = prefs.getLong(PhotoFrameApp.SHARED_PREF_EXPIRE, 0);

        if (Calendar.getInstance().getTimeInMillis() / 1000 > valid) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_CODE);
        } else {
            credentials = new Credentials(PhotoFrameApp.USER_ID, prefs.getString(PhotoFrameApp.SHARED_PREF_TOKEN, null));
            startLoading();
        }
    }

    private void startLoading() {
//        fileListFragment = FileListFragment.newInstance(credentials, FileListFragment.ROOT);
        getSupportFragmentManager().beginTransaction()
                .replace(
                        R.id.file_list_fragment_container,
                        FileListFragment.newInstance(credentials, FileListFragment.ROOT, getResources().getString(R.string.disk)), FRAGMENT_TAG)
                .commitAllowingStateLoss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_feedback) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("text/email");
            email.putExtra(Intent.EXTRA_EMAIL, new String[] { "zerogerc@gmail.com" });
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            startActivity(Intent.createChooser(email, getResources().getString(R.string.send_feedback_label)));
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOGIN_CODE:
                if (resultCode == RESULT_OK) {
                    String accessToken = data.getStringExtra(LoginActivity.ACCESS_TOKEN_KEY);
                    Long expireDate = data.getLongExtra(LoginActivity.EXPIRE_TIME_KEY, 0);

                    SharedPreferences.Editor prefs = getSharedPreferences(PhotoFrameApp.SHARED_NAME, MODE_PRIVATE).edit();
                    prefs.putString(PhotoFrameApp.SHARED_PREF_TOKEN, accessToken);
                    prefs.putLong(PhotoFrameApp.SHARED_PREF_EXPIRE, expireDate);
                    prefs.apply();

                    credentials = new Credentials(PhotoFrameApp.USER_ID, accessToken);
                    startLoading();
                } else {
                    Log.e(LOG_TAG, "Error while retrieving access token");
                }
        }
    }
}
