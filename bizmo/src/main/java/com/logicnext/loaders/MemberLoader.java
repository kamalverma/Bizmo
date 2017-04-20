package com.logicnext.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.bizmo.engine.ChatEngine;

public  class MemberLoader extends CursorLoader 
{
	ChatEngine chatEngine;
	
	String constraints;
	
	public MemberLoader(Context context, ChatEngine c) {
		super(context);
		chatEngine= c;
		
	}
	private static final String TAG = "DumbLoader";
	@Override
	public Cursor loadInBackground() {
		return chatEngine.getMembers(constraints);
	}
	
	public void setConstraints(String cons)
	{
		constraints= cons;
	}

}
