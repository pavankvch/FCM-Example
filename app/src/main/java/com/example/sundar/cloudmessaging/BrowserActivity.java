package com.example.sundar.cloudmessaging;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Created by sundar on 2/8/16.
 */
public class BrowserActivity extends AppCompatActivity{

    private String url="";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        init();
    }

    private void init() {
        Intent intent=getIntent();

        if(intent!=null){
            url =intent.getStringExtra("url");
            showUrlInWebView();
//            showUrlInBrowser();
        }else{
            Toast.makeText(getApplicationContext(), "No URL Found", Toast.LENGTH_LONG).show();
        }
    }

    private void showUrlInWebView() {
        WebView webView=(WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
