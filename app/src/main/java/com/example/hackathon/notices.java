package com.example.hackathon;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class notices extends AppCompatActivity {
    private WebView mWebView;
    public static final boolean ASWP_EXTURL = true;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> mFilePathCallback;
    @SuppressLint({"JavascriptInterface", "CutPasteId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notices);
        WebView webView = findViewById(R.id.web);
        mWebView = (WebView) findViewById(R.id.web);

        webView.loadUrl("https://srmsbusnotice.w3spaces.com/");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (ASWP_EXTURL && !url.startsWith("https://srmsbusnotice.w3spaces.com")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        class WebAppInterface {

            public WebAppInterface(Context context) {
            }
        }
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                    mFilePathCallback = filePathCallback;
                } catch (ActivityNotFoundException e) {
                    mFilePathCallback = null;
                    filePathCallback.onReceiveValue(null);
                    Toast.makeText(getApplicationContext(), "No app found to open file chooser.", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mFilePathCallback != null) {
                    Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    mFilePathCallback.onReceiveValue(result);
                    mFilePathCallback = null;
                }
            } else {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}