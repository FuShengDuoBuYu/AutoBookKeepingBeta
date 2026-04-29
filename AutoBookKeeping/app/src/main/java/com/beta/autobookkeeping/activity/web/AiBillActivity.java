package com.beta.autobookkeeping.activity.web;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.beta.autobookkeeping.R;

public class AiBillActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_bill);

        webView = findViewById(R.id.web_ai_bill);

        initWebView();
        loadAiBillPage();
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient());
    }

    private void loadAiBillPage() {
        String phoneNum = getIntent().getStringExtra("phoneNum");
        Uri uri = Uri.parse("https://recollected-accelerable-lynn.ngrok-free.dev/")
                .buildUpon()
                .appendQueryParameter("phoneNum", phoneNum)
                .build();
        webView.loadUrl(uri.toString());
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
