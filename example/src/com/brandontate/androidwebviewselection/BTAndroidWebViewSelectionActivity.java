package com.brandontate.androidwebviewselection;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.QuickPopupListener;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BTAndroidWebViewSelectionActivity extends Activity {
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
		webView.setPopupListener( new QuickPopupListener() {
			
			@Override
			public boolean onPrepareMenu(final QuickAction mContextMenu) {
				
				// Add buttons
				//Copy action item
				ActionItem buttonOne = new ActionItem();
				 
				buttonOne.setTitle("Button 1");
				buttonOne.setActionId(1);
				buttonOne.setIcon(getResources().getDrawable(R.drawable.menu_search));
				
				 
				//Highlight action item
				ActionItem buttonTwo = new ActionItem();
				 
				buttonTwo.setTitle("Button 2");
				buttonTwo.setActionId(2);
				buttonTwo.setIcon(getResources().getDrawable(R.drawable.menu_info));
				
				ActionItem buttonThree = new ActionItem();
				
				buttonThree.setTitle("Button 3");
				buttonThree.setActionId(3);
				buttonThree.setIcon(getResources().getDrawable(R.drawable.menu_eraser));
				 
				
				
				mContextMenu.addActionItem(buttonOne);
				
				mContextMenu.addActionItem(buttonTwo);
				
				mContextMenu.addActionItem(buttonThree);
				
				//setup the action item click listener
				mContextMenu.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction source, int pos,
						int actionId) {
						// TODO Auto-generated method stub
						if (actionId == 1) { 
							// Do Button 1 stuff
							Log.i(TAG, "Hit Button 1");
						} 
						else if (actionId == 2) { 
							// Do Button 2 stuff
							Log.i(TAG, "Hit Button 2");
						} 
						else if (actionId == 3) { 
							// Do Button 3 stuff
							Log.i(TAG, "Hit Button 3");
						}
						mContextMenu.dismiss();
					}
					
				});
				return true;
			}
		} );
	}
}