package com.zerogerc.photoframe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Fragment for initial authorization of user and gaining access token.
 */
public class LoginFragment extends DialogFragment {
    private static String LOG_TAG = "LOG_FRAG";

    private static String authLink = "https://oauth.yandex.ru/authorize?" +
            "response_type=token" +
            "&client_id=12c6774f2763474591d36590bb7b252b";

    private static String ACCESS_TOKEN = "access_token=";
    private static String ERROR = "error=";

    private WebView webView;


    public static LoginFragment newInstance() {
        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        ViewGroup root = ((ViewGroup) inflater.inflate(R.layout.content_login_fragment, null));

        webView = (WebView) (root.findViewById(R.id.login_fragment_web_view));
        if (webView != null) {
            /*
            I need this hack because I really like dialog fragments but
            WebView don't force keyboard to appear after onTouch.
             see: http://stackoverflow.com/questions/16550737/soft-keyboard-not-displaying-on-touch-in-webview-dialogfragment
             */
            EditText focus = new EditText(getActivity());
            focus.setVisibility(View.GONE);
            root.addView(focus);
            focus.requestFocus();

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new LoginClient());
            webView.loadUrl(authLink);
        }
        builder.setView(root);
        return builder.create();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window w = getDialog().getWindow();
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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

    private class LoginClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, url);
            if (url.contains(ACCESS_TOKEN)) {
                String accesToken = getAccessToken(url);
                Log.d(LOG_TAG, accesToken);
                destroyWebView();
                dismiss();
            } else if (url.contains(ERROR)) {
                destroyWebView();
                dismiss();
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }
    }
}
