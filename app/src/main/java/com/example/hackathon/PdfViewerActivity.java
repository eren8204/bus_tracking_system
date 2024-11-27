package com.example.hackathon;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PdfViewerActivity extends AppCompatActivity {

    private String pdfUrl;
    private String pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        WebView webView = findViewById(R.id.webView);

        // Get PDF URL and name from Intent
        pdfUrl = getIntent().getStringExtra("pdfUrl");
        pdfName = getIntent().getStringExtra("file");

        // Configure WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        // Load PDF in WebView using Google Docs Viewer
        webView.loadUrl("https://docs.google.com/viewer?url=" + pdfUrl);
    }
}
