package com.logicnext.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.bizmo.engine.ChatEngine;

public  class ChatLoader extends CursorLoader 
{
	
	ChatEngine chatEngine;
	String Uid;

	
	public ChatLoader(Context context, ChatEngine c, String uid) {
		super(context);
		chatEngine= c;
		
		Uid= uid;
	}
	private static final String TAG = "DumbLoader";
	@Override
	public Cursor loadInBackground() {
		return chatEngine.getChats(Uid);
		
	}

}
