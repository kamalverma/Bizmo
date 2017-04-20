package com.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class PressableTextView extends TextView
{

	public PressableTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
//		Typeface type = Typeface.createFromAsset(context.getAssets(),"Helvetica.otf"); 
//		setTypeface(type);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		switch (event.getAction()) 
		{
		case MotionEvent.ACTION_DOWN:
			setAlpha(0.5f);
			break;

		case MotionEvent.ACTION_UP:
			setAlpha(1.0f);
			break;
		default:
			setAlpha(1.0f);
			break;
		}
		return super.onTouchEvent(event);
	}
	

}
