package com.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MyImageView extends ImageView 
{
	public MyImageView(Context context) 
	{
		super(context);
	}
	

	public MyImageView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	public MyImageView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		switch (event.getAction()) 
		{
		case MotionEvent.ACTION_DOWN:
			setAlpha(50);
			break;

		case MotionEvent.ACTION_UP:
			setAlpha(255);
			break;
		default:
			setAlpha(255);
			break;
		}
		return super.onTouchEvent(event);
	}
}

