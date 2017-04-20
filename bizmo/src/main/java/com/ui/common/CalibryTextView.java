package com.ui.common;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CalibryTextView extends TextView
{

	public CalibryTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    Typeface type = FontUtilitirs.calibriBlack(context);
		setTypeface(type);

	}

	public CalibryTextView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    Typeface type = FontUtilitirs.calibriBlack(context);
		setTypeface(type);

	}

	public CalibryTextView(Context context) {
	    super(context);
	    Typeface type = FontUtilitirs.calibriBlack(context);
		setTypeface(type);

	}

}
