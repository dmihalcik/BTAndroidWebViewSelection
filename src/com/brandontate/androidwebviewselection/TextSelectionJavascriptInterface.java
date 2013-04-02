package com.brandontate.androidwebviewselection;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * This javascript interface allows the page to communicate that text has been selected by the user.
 * 
 * @author btate
 *
 */
public class TextSelectionJavascriptInterface {

	/** The TAG for logging. */
	private static final String TAG = "TextSelectionJavascriptInterface";
	
	/** The javascript interface name for adding to web view. */
	private final String interfaceName = "TextSelection";
	
	/** The webview to work with. */
	private TextSelectionJavascriptInterfaceListener listener;

	private Handler mHandler;

	private Runnable mStartSelectionMode = new MyRunnable(){
		@Override
		protected void exec(TextSelectionJavascriptInterfaceListener listener) {
			listener.tsjiStartSelectionMode();
		}};
	
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
	public void jsError(final String error){
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
	public void startSelectionMode(){
		if( null == listener ) return;
		mHandler.post( mStartSelectionMode );
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
	public void endSelectionMode(){
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
	public void selectionChanged(final String range, final String text, final String handleBounds, final String menuBounds){
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiSelectionChanged(range, text, handleBounds, menuBounds);
			}
		} );
	}
	
	public void setContentWidth(final float contentWidth){
		if( null == listener ) return;
		mHandler.post( new MyRunnable() {
			@Override
			protected void exec(TextSelectionJavascriptInterfaceListener listener) {
				listener.tsjiSetContentWidth(contentWidth);
			}
		} );
	}
	
}
