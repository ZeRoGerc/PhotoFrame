package com.zerogerc.photoframe.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.yandex.disk.client.Credentials;
import com.zerogerc.photoframe.login.LoginActivity;

import java.util.Calendar;

public class FileListActivity extends FragmentActivity {
    private static final String LOG_TAG = "MAIN";

    private static final int LOGIN_CODE = 1;

    public static final String FRAGMENT_TAG = "list_fragment";

    private Credentials credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, LoginActivity.class);

        SharedPreferences prefs = getSharedPreferences(PhotoFrameApp.SHARED_NAME, MODE_PRIVATE);

        long valid = prefs.getLong(PhotoFrameApp.SHARED_PREF_EXPIRE, 0);
        if (Calendar.getInstance().get(Calendar.SECOND) > valid) {
            startActivityForResult(intent, LOGIN_CODE);
        } else {
            credentials = new Credentials(PhotoFrameApp.USER_ID, prefs.getString(PhotoFrameApp.SHARED_PREF_TOKEN, null));
            startLoading();
        }
    }

    private void startLoading() {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, FileListFragment.newInstance(credentials), FRAGMENT_TAG)
                .commitAllowingStateLoss();
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
