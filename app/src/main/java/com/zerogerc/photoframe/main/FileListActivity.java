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

/**
 * This in MainActivity of application. It checks the validity of <code>OAuth</code> token, starts
 * {@link LoginActivity} if needed. After that load {@link FileListFragment}.
 */
public class FileListActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MAIN";

    /**
     * Code for getting the result of {@link LoginActivity}.
     */
    private static final int LOGIN_CODE = 1;

    /**
     * Credentials for loading data from yandex disk.
     */
    private Credentials credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences(PhotoFrameApp.SHARED_NAME, MODE_PRIVATE);
        long valid = prefs.getLong(PhotoFrameApp.SHARED_PREF_EXPIRE, 0);

        // Check validity of OAuth token
        if (Calendar.getInstance().getTimeInMillis() / 1000 > valid) {
            //If not valid start LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_CODE);
        } else {
            //If valid create credentials and load FileListFragment
            credentials = new Credentials(PhotoFrameApp.USER_ID, prefs.getString(PhotoFrameApp.SHARED_PREF_TOKEN, null));

            if (savedInstanceState == null) {
                startLoading();
            }
        }
    }

    /**
     * Load {@link FileListFragment} on the content of this Activity.
     */
    private void startLoading() {
        getSupportFragmentManager().beginTransaction()
                .replace(
                        R.id.file_list_fragment_container,
                        FileListFragment.newInstance(credentials, FileListFragment.ROOT, getResources().getString(R.string.disk)), FileListFragment.ROOT)
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
                    //start FileListFragment with proper Credentials
                    startLoading();
                } else {
                    Log.e(LOG_TAG, "Error while retrieving access token");
                }
        }
    }
}
