package com.brandontate.androidwebviewselection;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BTAndroidWebViewSelectionActivity extends SherlockActivity {
	private static final String TAG = "BTWebView";
	private BTWebView webView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_webview_example);
		webView = (BTWebView) findViewById(R.id.webView);
		
		// Load up the android asset file
		String filePath = "file:///android_asset/content.html";
		// Load the url
		webView.loadUrl(filePath);
		webView.setSupportPopupListener( new ActionMode.Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Add buttons
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.context_menu, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch( item.getItemId() ) {
				case R.id.item1:
					// Do Button 1 stuff
					Log.i(TAG, "Hit Button 1");
					return true;
				case R.id.item2:
					// Do Button 2 stuff
					Log.i(TAG, "Hit Button 2");
					return true;
				case R.id.item3:
					// Do Button 3 stuff
					Log.i(TAG, "Hit Button 3");
					return true;
				}
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				webView.endSelectionMode();
			}});
	}
}