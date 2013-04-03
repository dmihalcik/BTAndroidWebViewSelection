package com.brandontate.androidwebviewselection;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * This javascript interface allows the page to communicate that text has been selected by the user.
 * 
 * @author btate
 *
 */
public class TextSelectionJavascriptInterface {

	/** The TAG for logging. */
	private static final String TAG = "TextSelectionJavascriptInterface";

	private static final boolean D = false;
	
	/** The javascript interface name for adding to web view. */
	private final String interfaceName = "TextSelection";
	
	/** The webview to work with. */
	private TextSelectionJavascriptInterfaceListener listener;

	private Handler mHandler;

	/**
	 * Constructor accepting context.
	 * @param c
	 */
	public TextSelectionJavascriptInterface(){
		super();
		mHandler = new Handler();
	}
	
	/**
	 * Constructor accepting context and listener.
	 * @param c
	 * @param listener
	 */
	public TextSelectionJavascriptInterface(TextSelectionJavascriptInterfaceListener listener){
		this();
		this.listener = listener;
	}
	
	/**
	 * Handles javascript errors.
	 * @param error
	 */
	@JavascriptInterface
	public void jsError(final String error){
		if( D ) Log.e( TAG, error );
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiJSError( error );
			}
		} );
	}
	
	/**
	 * Gets the interface name
	 * @return
	 */
	public String getInterfaceName(){
		return this.interfaceName;
	}
	
	/**
	 * Put the app in "selection mode".
	 */
	@JavascriptInterface
	public void startSelectionMode(){
		if( D ) Log.i( TAG, "startSelectionMode()" );
		if( null == listener ) return;
		mHandler.post( new MyRunnable(){
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiStartSelectionMode();
			}} );
	}
	
	private abstract class MyRunnable implements Runnable {
		@Override
		public void run() {
			if(listener == null) return;
			exec(listener);
		}

		protected abstract void exec(TextSelectionJavascriptInterfaceListener listener);
	}
	
	/**
	 * Take the app out of "selection mode".
	 */
	@JavascriptInterface
	public void endSelectionMode(){
		if( D ) Log.i( TAG, "endSelectionMode()" );
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiEndSelectionMode();
			}
		} );
	}
	
	/**
	 * Show the context menu
	 * @param range
	 * @param text
	 * @param bounds
	 * @param showHighlight
	 * @param showUnHighlight
	 */
	@JavascriptInterface
	public void selectionChanged(final String range, final String text, final String handleBounds, final String menuBounds, final boolean flipped){
		if( D ) Log.v( TAG, "selectionChanged(\"" + range + "\", \"" + text + "\", \"" + handleBounds + "\", \"" + menuBounds + "\", " + flipped + ")" );
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiSelectionChanged(range, text, handleBounds, menuBounds, flipped);
			}
		} );
	}
	
	@JavascriptInterface
	public void setContentWidth(final float contentWidth){
		if( D ) Log.v( TAG, "setContentWidth(" + contentWidth + ")" );
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiSetContentWidth(contentWidth);
			}
		} );
	}
	
}
