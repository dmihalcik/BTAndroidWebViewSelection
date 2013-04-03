package com.brandontate.androidwebviewselection;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class BTAndroidWebViewSelectionActivity extends Activity {
	private WebView webView;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_webview_example);
        webView = (WebView) findViewById(R.id.webView);
        
        // Load up the android asset file
        String filePath = "file:///android_asset/content.html";
        // Load the url
        webView.loadUrl(filePath);
    }
}