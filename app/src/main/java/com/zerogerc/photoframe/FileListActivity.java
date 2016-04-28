package com.zerogerc.photoframe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.zerogerc.photoframe.login.LoginActivity;

public class FileListActivity extends FragmentActivity {
    private static final String LOG_TAG = "MAIN";

    private static final int LOGIN_CODE = 1;

    public static final String FRAGMENT_TAG = "list_fragment";

    public static String accessToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_CODE);
    }

    private void startLoading() {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FileListFragment(), FRAGMENT_TAG)
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
