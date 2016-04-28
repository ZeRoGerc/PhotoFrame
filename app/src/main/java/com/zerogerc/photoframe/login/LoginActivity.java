package com.zerogerc.photoframe.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zerogerc.photoframe.FileListFragment;
import com.zerogerc.photoframe.R;

/**
 * Activity for initial authorization of user and gaining access token.
 */
public class LoginActivity extends Activity {
    public static final String ACCESS_TOKEN_KEY = "access_token";

    private static String LOG_TAG = "LOG_FRAG";

    private static final String AUTH_LINK = "https://oauth.yandex.ru/authorize?" +
            "response_type=token" +
            "&client_id=" + FileListFragment.USER_ID;

    private static String ACCESS_TOKEN = "access_token=";
    private static String ERROR = "error=";

    private WebView webView;

    private String accessToken = null;

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
     * Parse access token from given url
     * @param url given url
     * @return access token
     */
    private String getAccessToken(final String url) {
        int left = url.indexOf(ACCESS_TOKEN);
        if (left == -1) {
            Log.e(LOG_TAG, "Given url doesn't contain \"access token=\"");
            return null;
        } else {
            left += ACCESS_TOKEN.length();
            int right = left;
            while (right < url.length() && url.charAt(right) != '&') {
                right++;
            }
            return url.substring(left, right);
        }
    }

    private void finishWithResult(final String accessToken) {
        Intent result = new Intent();
        result.putExtra(ACCESS_TOKEN_KEY, accessToken);
        setResult(RESULT_OK, result);
        finish();
    }

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
                accessToken = getAccessToken(url);
                Log.d(LOG_TAG, accessToken);
                destroyWebView();
                finishWithResult(accessToken);
            } else if (url.contains(ERROR)) {
                destroyWebView();
                finishWithError();
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }
    }
}
