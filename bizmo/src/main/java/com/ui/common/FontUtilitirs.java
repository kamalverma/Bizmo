package com.ui.common;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtilitirs
{
	public static Typeface getTypeface(String fontName, Context ctx)
	{
		return Typeface.createFromAsset(ctx.getAssets(), "fonts/Lato/"+fontName);
	}
	public static Typeface calibriBlack(Context ctx)
	{
		return Typeface.createFromAsset(ctx.getAssets(), "Calibri.ttf");
	}
	
	public static Typeface calibriBold(Context ctx)
	{
		return Typeface.createFromAsset(ctx.getAssets(), "cbold.ttf");
	}

}
