package com.brandontate.androidwebviewselection;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.blahti.drag.DragController;
import com.blahti.drag.DragLayer;
import com.blahti.drag.DragListener;
import com.blahti.drag.DragSource;
import com.blahti.drag.MyAbsoluteLayout;

public class BTWebView extends WebView implements TextSelectionJavascriptInterfaceListener, 
	OnTouchListener, OnLongClickListener, DragListener{
	
	/** The logging tag. */
	private static final String TAG = "BTWebView";

	private static final boolean D = false;

	protected float mCurrentScale = 0;
	
	/** The context menu. */
	protected Object popupListener;
	
	/** The drag layer for selection. */
	protected DragLayer mSelectionDragLayer;
	
	/** The drag controller for selection. */
	protected DragController mDragController;
	
	/** The start selection handle. */
	protected ImageView mStartSelectionHandle;
	
	/** the end selection handle. */
	protected ImageView mEndSelectionHandle;
	
	/** The selection bounds. */
	protected Rect mSelectionBounds = null;
	
	/** The selected range. */
	protected String selectedRange = "";
	
	/** The selected text. */
	protected String selectedText = "";
	
	/** Javascript interface for catching text selection. */
	protected TextSelectionJavascriptInterface textSelectionJSInterface = null;
	
	/** Selection mode flag. */
	protected boolean inSelectionMode = false;
	
	/** Flag to stop from showing context menu twice. */
	protected Object actionMode;
	
	/** The current content width. */
	protected int contentWidth = 0;
	
	
	/** Identifier for the selection start handle. */
	protected final int SELECTION_START_HANDLE = 0;
	
	/** Identifier for the selection end handle. */
	protected final int SELECTION_END_HANDLE = 1;
	
	/** Last touched selection handle. */
	protected int mLastTouchedSelectionHandle = -1;
	
	private boolean mScrolling = false;
	private float mScrollDiffY = 0;
	private float mLastTouchY = 0;
	private float mScrollDiffX = 0;
	private float mLastTouchX = 0;
	
	/// if the user is currently dragging a handle around
	private boolean dragging;
	// Number of active 'loadUrl(javascript:)' we are waiting for
	private int eventsActive = 0;

	private JSONObject actionInfo;
	
	public BTWebView(Context context) {
		super(context);
		this.setup(context);
	}
	
	public BTWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setup(context);
		
	}

	public BTWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setup(context);
	
	}
	
	
	//*****************************************************
	//*
	//*		Touch Listeners
	//*
	//*****************************************************
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Context ctx = getContext();
		float xPoint = getDensityIndependentValue(event.getX(), ctx) / getDensityIndependentValue(currentScale(), ctx);
		float yPoint = getDensityIndependentValue(event.getY(), ctx) / getDensityIndependentValue(currentScale(), ctx);
		
		// TODO: Need to update this to use this.getScale() as a factor.
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			
			String startTouchUrl = String.format(Locale.US,
					"javascript:android.selection.startTouch(%f, %f);", 
					xPoint, yPoint);
			
			mLastTouchX = xPoint;
			mLastTouchY = yPoint;
			
			this.loadUrl(startTouchUrl);
			
			// Flag scrolling for first touch
			//if(!this.isInSelectionMode())
				//mScrolling = true;
			
			
			
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			// Check for scrolling flag
			if(!mScrolling){
				this.endSelectionMode();
			}
			
			mScrollDiffX = 0;
			mScrollDiffY = 0;
			mScrolling = false;
			
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE){
			
			mScrollDiffX += (xPoint - mLastTouchX);
			mScrollDiffY += (yPoint - mLastTouchY);
			
			mLastTouchX = xPoint;
			mLastTouchY = yPoint;
			
			
			// Only account for legitimate movement.
			if(Math.abs(mScrollDiffX) > 10 || Math.abs(mScrollDiffY) > 10){
				mScrolling = true;
				
			}
			
			
		}
		
		// If this is in selection mode, then nothing else should handle this touch
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private float currentScale() {
		if( 0 == mCurrentScale ) {
			mCurrentScale = getScale();
		}
		return mCurrentScale;
	}

	@Override 
	public boolean onLongClick(View v){
		
		// Tell the javascript to handle this if not in selection mode
		//if(!this.isInSelectionMode()){
			this.loadUrl("javascript:android.selection.longTouch();");
			mScrolling = true;
		//}
		
		
		// Don't let the webview handle it
		return true;
	}
	
	//*****************************************************
	//*
	//*		Setup
	//*
	//*****************************************************
	
	/**
	 * Setups up the web view.
	 * @param context
	 */
	protected void setup(Context context){
		// On Touch Listener
		this.setOnLongClickListener(this);
		this.setOnTouchListener(this);
	
		
		// Webview setup
		WebSettings settings = this.getSettings();
		configureWebViewSettings( settings );
		
		// Zoom out fully
		//this.getSettings().setLoadWithOverviewMode(true);
		//this.getSettings().setUseWideViewPort(true);
		
		// Javascript interfaces
		this.textSelectionJSInterface = new TextSelectionJavascriptInterface(this);		
		this.addJavascriptInterface(this.textSelectionJSInterface, this.textSelectionJSInterface.getInterfaceName());
		
		this.setWebViewClient( new BTWebViewClient(this) );
		
		
		// Create the selection handles
		createSelectionLayer(context);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint( "SetJavaScriptEnabled" )
	protected void configureWebViewSettings(WebSettings settings) {
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		if( Build.VERSION.SDK_INT < 8 ) {
			settings.setPluginsEnabled(true);
		} else {
			settings.setPluginState( WebSettings.PluginState.ON_DEMAND );
		}
	}
	
	
	//*****************************************************
	//*
	//*		Selection Layer Handling
	//*
	//*****************************************************
	
	/**
	 * Creates the selection layer.
	 * 
	 * @param context
	 */
	protected void createSelectionLayer(Context context){
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mSelectionDragLayer = (DragLayer) inflater.inflate(R.layout.selection_drag_layer, null);
		
		
		// Make sure it's filling parent
		this.mDragController = new DragController(context);
		this.mDragController.setDragListener(this);
		this.mDragController.addDropTarget(mSelectionDragLayer);
		this.mSelectionDragLayer.setDragController(mDragController);
		
		
		this.mStartSelectionHandle = (ImageView) this.mSelectionDragLayer.findViewById(R.id.startHandle);
		this.mStartSelectionHandle.setTag( SELECTION_START_HANDLE );
		this.mEndSelectionHandle = (ImageView) this.mSelectionDragLayer.findViewById(R.id.endHandle);
		this.mEndSelectionHandle.setTag( SELECTION_END_HANDLE );
		
		OnTouchListener handleTouchListener = new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				boolean handledHere = false;

			    final int action = event.getAction();

			    // Down event starts drag for handle.
			    if (action == MotionEvent.ACTION_DOWN) {
			       handledHere = startDrag (v);
			       mLastTouchedSelectionHandle = (Integer) v.getTag();
			    }
			    
			    return handledHere;
				
				
			}
			
		
		};
		
		this.mStartSelectionHandle.setOnTouchListener(handleTouchListener);
		this.mEndSelectionHandle.setOnTouchListener(handleTouchListener);
		
		
	}
	
	/**
	 * Starts selection mode.
	 * 
	 * @param	selectionBounds
	 */
	public void startSelectionMode(){
		if(mSelectionBounds == null)
			return;
		
		addView(mSelectionDragLayer);
		
		drawSelectionHandles();

		
		Context ctx = getContext();
		int contentHeight = (int) Math.ceil(getDensityDependentValue(getContentHeight(), ctx));
		
		// Update Layout Params
		ViewGroup.LayoutParams layerParams = mSelectionDragLayer.getLayoutParams();
		layerParams.height = contentHeight;
		layerParams.width = contentWidth;
		mSelectionDragLayer.setLayoutParams(layerParams);
	}
	
	/**
	 * Ends selection mode.
	 */
	@SuppressLint("NewApi")
	public void endSelectionMode(){
		removeView(mSelectionDragLayer);
		if(null != actionMode){
			if( actionMode instanceof ActionMode ) {
				ActionMode t = (ActionMode) actionMode;
				actionMode = null;
				t.finish();
			} else {
				android.view.ActionMode t = (android.view.ActionMode) actionMode;
				actionMode = null;
				t.finish();
			} 
		}
		mSelectionBounds = null;
		mLastTouchedSelectionHandle = -1;
		loadUrl("javascript: android.selection.clearSelection();");
	}
	
	/**
	 * Calls the handler for drawing the selection handles.
	 */
	private void drawSelectionHandles(){
			MyAbsoluteLayout.LayoutParams startParams = (com.blahti.drag.MyAbsoluteLayout.LayoutParams) mStartSelectionHandle.getLayoutParams();
			startParams.x = (int) (mSelectionBounds.left - mStartSelectionHandle.getDrawable().getIntrinsicWidth());
			startParams.y = (int) (mSelectionBounds.top);
		
			// Stay on screen.
			startParams.x = (startParams.x < 0) ? 0 : startParams.x;
			startParams.y = (startParams.y < 0) ? 0 : startParams.y;
			
			mStartSelectionHandle.setLayoutParams(startParams);
			
			MyAbsoluteLayout.LayoutParams endParams = (com.blahti.drag.MyAbsoluteLayout.LayoutParams) mEndSelectionHandle.getLayoutParams();
			endParams.x = (int) mSelectionBounds.right;
			endParams.y = (int) mSelectionBounds.bottom;
			
			// Stay on screen
			endParams.x = (endParams.x < 0) ? 0 : endParams.x;
			endParams.y = (endParams.y < 0) ? 0 : endParams.y;
			
			mEndSelectionHandle.setLayoutParams(endParams);

		}

	/**
	 * Checks to see if this view is in selection mode.
	 * @return
	 */
	public boolean isInSelectionMode(){
		
		return this.mSelectionDragLayer.getParent() != null;
	
		
	}
	
	//*****************************************************
	//*
	//*		DragListener Methods
	//*
	//*****************************************************
	
	/**
	 * Start dragging a view.
	 *
	 */    
	private boolean startDrag (View v)
	{
	    // Let the DragController initiate a drag-drop sequence.
	    // I use the dragInfo to pass along the object being dragged.
	    // I'm not sure how the Launcher designers do this.
	    Object dragInfo = v;
	    mDragController.startDrag (v, mSelectionDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
	    return true;
	}
	
	
	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		// TODO Auto-generated method stub
		dragging = true;
		eventsActive = 0;
	}

	@Override
	public void onDragMove(int x, int y) {
		if( D ) Log.d( TAG, "onDragMove" );
		if( 0 == eventsActive ) {
			boolean touchedStart = mLastTouchedSelectionHandle == SELECTION_START_HANDLE;
			boolean touchedEnd = mLastTouchedSelectionHandle == SELECTION_END_HANDLE;
			if(touchedStart){
				x = x - this.getScrollX() + mStartSelectionHandle.getWidth();
				y = y - this.getScrollY();
				saveSelectionStart(x, y);
			} else if(touchedEnd) {
				x -= this.getScrollX();
				y -= this.getScrollY();
				saveSelectionEnd(x, y);
			}
		}
	}
	@Override
	public void onDragEnd() {
		if( D ) Log.d( TAG, "onDragEnd" );
		dragging = false;
		boolean touchedStart = mLastTouchedSelectionHandle == SELECTION_START_HANDLE;
		boolean touchedEnd = mLastTouchedSelectionHandle == SELECTION_END_HANDLE;
		if( touchedStart ){
			MyAbsoluteLayout.LayoutParams startHandleParams = (MyAbsoluteLayout.LayoutParams) this.mStartSelectionHandle.getLayoutParams();
			float x = startHandleParams.x - this.getScrollX() + mStartSelectionHandle.getWidth();
			float y = startHandleParams.y - this.getScrollY();
			saveSelectionStart(x, y);
		}
		if( touchedEnd ) {
			MyAbsoluteLayout.LayoutParams endHandleParams = (MyAbsoluteLayout.LayoutParams) this.mEndSelectionHandle.getLayoutParams();
			float x = endHandleParams.x - this.getScrollX();
			float y = endHandleParams.y - this.getScrollY();
			saveSelectionEnd(x, y);
		}
	}

	public void saveSelectionEnd(float x, float y) {
		Context ctx = getContext();
		final float scale = getDensityIndependentValue(currentScale(), ctx);
		float endX = getDensityIndependentValue(x, ctx) / scale;
		float endY = getDensityIndependentValue(y, ctx) / scale;
		if(endX > 0 && endY > 0){
			String saveEndString = String.format(Locale.US, "javascript: android.selection.setEndPos(%f, %f);", endX, endY);
			eventsActive++;
			if( D ) Log.d( TAG, "dragging: " + saveEndString );
			this.loadUrl(saveEndString);
		}
	}

	public void saveSelectionStart(float x, float y) {
		Context ctx = getContext();
		final float scale = getDensityIndependentValue(currentScale(), ctx);
		final float startX = getDensityIndependentValue(x, ctx) / scale;
		final float startY = getDensityIndependentValue(y, ctx) / scale;
		if(startX > 0 && startY > 0){
			String saveStartString = String.format(Locale.US, "javascript: android.selection.setStartPos(%f, %f);", startX, startY);
			eventsActive++;
			if( D ) Log.d( TAG, "dragging: " + saveStartString );
			this.loadUrl(saveStartString);
		}
	}
	
	
	//*****************************************************
	//*
	//*		Context Menu Creation
	//*
	//*****************************************************
	
	/**
	 * Shows the context menu using the given region as an anchor point.
	 * @param etc 
	 * @param region
	 */
	@SuppressLint("NewApi")
	private void showContextMenu(Rect displayRect, JSONObject etc){
		this.actionInfo = etc;
		
		// Don't show this twice
		if(null != actionMode){
			if( actionMode instanceof ActionMode ) {
				ActionMode t = (ActionMode) actionMode;
				t.invalidate();
			} else {
				android.view.ActionMode t = (android.view.ActionMode) actionMode;
				t.invalidate();
			} 
			return;
		}
		
		// Don't use empty rect
		//if(displayRect.isEmpty()){
		if(displayRect.right <= displayRect.left){
			return;
		}
		
		// The action menu
		Context ctx = getContext();
		if(getParent() != null && ctx != null){
			if( ctx instanceof SherlockFragmentActivity ) {
				actionMode = ((SherlockFragmentActivity) ctx).startActionMode( (ActionMode.Callback) popupListener );
			} else if( ctx instanceof SherlockActivity ) {
				actionMode = ((SherlockActivity) ctx).startActionMode( (ActionMode.Callback) popupListener );
			} else if( ctx instanceof SherlockPreferenceActivity) {
				actionMode = ((SherlockPreferenceActivity) ctx).startActionMode( (ActionMode.Callback) popupListener );
			} else if( ctx instanceof SherlockExpandableListActivity) {
				actionMode = ((SherlockExpandableListActivity) ctx).startActionMode( (Callback) popupListener );
			} else if( ctx instanceof SherlockListActivity) {
				actionMode = ((SherlockListActivity) ctx).startActionMode( (Callback) popupListener );
			} else if( ctx instanceof Activity ) {
				actionMode = ((Activity) ctx).startActionMode( (android.view.ActionMode.Callback) popupListener );
			} 
		}
	}
	
	public JSONObject getActionInfo() {
		return actionInfo;
	}
	public void setSupportPopupListener(ActionMode.Callback popupListener) {
		this.popupListener = popupListener;
	}
	public void setPopupListener(android.view.ActionMode.Callback popupListener) {
		this.popupListener = popupListener;
	}
	
	//*****************************************************
	//*
	//*		Text Selection Javascript Interface Listener
	//*
	//*****************************************************
	
	
	/**
	 * Shows/updates the context menu based on the range
	 */
	public void tsjiJSError(String error){
		Log.e(TAG, "JSError: " + error);
	}
	
	
	/**
	 * The user has started dragging the selection handles.
	 */
	public void tsjiStartSelectionMode(){
		
		this.startSelectionMode();
		
		
	}
	
	/**
	 * The user has stopped dragging the selection handles.
	 */
	public void tsjiEndSelectionMode(){
		
		this.endSelectionMode();
	}
	
	/**
	 * The selection has changed
	 * @param range
	 * @param text
	 * @param handleBounds
	 * @param menuBounds
	 * @param showHighlight
	 * @param showUnHighlight
	 */
	public void tsjiSelectionChanged(String range, String text, 
			String handleBounds, String menuBounds, boolean flipped, 
			JSONObject etc){
		eventsActive--;
		if( dragging ) return;
		if( 0 < eventsActive ) return;
		try {
			JSONObject selectionBoundsObject = new JSONObject(handleBounds);
			
			Context ctx = getContext();
			float scale = getDensityIndependentValue(currentScale(), ctx);
			
			Rect handleRect = new Rect();
			handleRect.left = (int) (getDensityDependentValue(selectionBoundsObject.getInt("left"), getContext()) * scale);
			handleRect.top = (int) (getDensityDependentValue(selectionBoundsObject.getInt("top"), getContext()) * scale);
			handleRect.right = (int) (getDensityDependentValue(selectionBoundsObject.getInt("right"), getContext()) * scale);
			handleRect.bottom = (int) (getDensityDependentValue(selectionBoundsObject.getInt("bottom"), getContext()) * scale);
			
			this.mSelectionBounds = handleRect;
			this.selectedRange = range;
			this.selectedText = text;

			JSONObject menuBoundsObject = new JSONObject(menuBounds);
			
			Rect displayRect = new Rect();
			displayRect.left = (int) (getDensityDependentValue(menuBoundsObject.getInt("left"), getContext()) * scale);
			displayRect.top = (int) (getDensityDependentValue(menuBoundsObject.getInt("top") - 25, getContext()) * scale);
			displayRect.right = (int) (getDensityDependentValue(menuBoundsObject.getInt("right"), getContext()) * scale);
			displayRect.bottom = (int) (getDensityDependentValue(menuBoundsObject.getInt("bottom") + 25, getContext()) * scale);
			
			if(!this.isInSelectionMode()){
				this.startSelectionMode();
			}
			
			// This will send the menu rect
			this.showContextMenu(displayRect, etc);
			
			drawSelectionHandles();
			if(flipped) {
				if( D ) Log.d( TAG, "flipping carets");
				this.loadUrl("javascript: android.selection.flipCarets();");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Receives the content width for the page.
	 */
	public void tsjiSetContentWidth(float contentWidth){
		Context ctx = getContext();
		this.contentWidth = (int) this.getDensityDependentValue(contentWidth, ctx);
	}

	
	//*****************************************************
	//*
	//*		Density Conversion
	//*
	//*****************************************************
	
	/**
	 * Returns the density dependent value of the given float
	 * @param val
	 * @param ctx
	 * @return
	 */
	public float getDensityDependentValue(float val, Context ctx){
		
		// Get display from context
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		// Calculate min bound based on metrics
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		
		return val * (metrics.densityDpi / 160f);
		
		//return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, metrics);
		
	}

	/**
	 * Returns the density independent value of the given float
	 * @param val
	 * @param ctx
	 * @return
	 */
	public float getDensityIndependentValue(float val, Context ctx){
		
		// Get display from context
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		// Calculate min bound based on metrics
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		
		
		return val / (metrics.densityDpi / 160f);
		
		//return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, val, metrics);
		
	}
}
