package com.zerogerc.photoframe.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zerogerc.photoframe.R;
import com.zerogerc.photoframe.main.PhotoFrameApp;

import java.util.Calendar;

/**
 * Activity for initial authorization of user and gaining access token.
 * For retrieving result from {@link Intent} use {@link #ACCESS_TOKEN_KEY} and {@link #EXPIRE_TIME_KEY}.
 */
public class LoginActivity extends Activity {
    private static String LOG_TAG = "LOG_FRAG";

    /**
     * Key that stores <code>OAuth</code> access token in resulting {@link Intent}.
     */
    public static final String ACCESS_TOKEN_KEY = "access_token";

    /**
     * Key that stores expire data of <code>OAuth</code> access token in resulting {@link Intent}.
     */
    public static final String EXPIRE_TIME_KEY = "expire";

    /**
     * <code>OAuth</code> request to server.
     */
    private static final String AUTH_LINK = "https://oauth.yandex.ru/authorize?" +
            "response_type=token" +
            "&client_id=" + PhotoFrameApp.USER_ID;

    /*
    Key Strings in server response.
     */
    private static final String ACCESS_TOKEN = "access_token=";
    private static final String EXPIRES_IN = "expires_in=";
    private static final String ERROR = "error=";

    /**
     * WebView of this Activity.
     */
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login_fragment);

        WebView webView = ((WebView) findViewById(R.id.login_fragment_web_view));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new LoginClient());
        webView.loadUrl(AUTH_LINK);
    }

    /**
     * Destroy {@link #webView} properly when no longer needed.
     */
    private void destroyWebView() {
        if(webView != null) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.pauseTimers();
            webView = null;
        }
    }

    /**
     * Retrieves value of given key from given url.
     * @param url given url
     * @param match given key
     * @return value of given key
     */
    private String getString(final String url, final String match) {
        int left = url.indexOf(match);
        if (left == -1) {
            Log.e(LOG_TAG, "Given url doesn't contain " + match);
            return null;
        } else {
            left += match.length();
            int right = left;
            while (right < url.length() && url.charAt(right) != '&') {
                right++;
            }
            return url.substring(left, right);
        }
    }

    /**
     * Finish {@link LoginActivity} with resulting {@link Intent}.
     * @param accessToken retrieved access_token
     * @param expiresIn retrieved exprires_in
     * @see #ACCESS_TOKEN_KEY
     * @see #EXPIRE_TIME_KEY
     */
    private void finishWithResult(final String accessToken, final long expiresIn) {
        Intent result = new Intent();
        result.putExtra(ACCESS_TOKEN_KEY, accessToken);
        long seconds = Calendar.getInstance().getTimeInMillis() / 1000;

        result.putExtra(EXPIRE_TIME_KEY, seconds + expiresIn);
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Finish Activity with Error.
     */
    private void finishWithError() {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Custom <code>WebView</code> for login that automatically finishes Activity.
     */
    private class LoginClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, url);
            if (url.contains(ACCESS_TOKEN)) {
                String accessToken = getString(url, ACCESS_TOKEN);
                String expire = getString(url, EXPIRES_IN);

                destroyWebView();
                finishWithResult(accessToken, Long.parseLong(expire));
            } else if (url.contains(ERROR)) {
                destroyWebView();
                finishWithError();
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }
    }
}
