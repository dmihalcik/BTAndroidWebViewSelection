package com.brandontate.androidwebviewselection;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BTWebViewClient extends WebViewClient {
	/**
	 * 
	 */
	private final BTWebView btWebView;

	/**
	 * @param btWebView
	 */
	public BTWebViewClient(BTWebView btWebView) {
		this.btWebView = btWebView;
	}

	public void onScaleChanged(WebView view, float oldScale, float newScale) {
		this.btWebView.mCurrentScale = newScale;
	}
}