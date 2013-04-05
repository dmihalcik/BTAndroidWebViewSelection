package com.brandontate.androidwebviewselection;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DraggableImageView extends ImageView {

	public DraggableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DraggableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DraggableImageView(Context context) {
		super(context);
	}

	
	@Override
	public boolean canScrollHorizontally(int direction) {
		return true;
	}
	
	@Override
	public boolean canScrollVertically(int direction) {
		return true;
	}
}
