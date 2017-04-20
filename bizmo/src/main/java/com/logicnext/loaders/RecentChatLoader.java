package com.logicnext.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import com.bizmo.engine.ChatEngine;

public class RecentChatLoader extends CursorLoader {
	
	ChatEngine chatEngine;
	String Uid;

	
	public RecentChatLoader(Context context, ChatEngine c, String uid) {
		super(context);
		chatEngine= c;
		
		Uid= uid;
	}

	 @Override
	    public Cursor loadInBackground() {
	        // this is just a simple query, could be anything that gets a cursor
	        return chatEngine.getRecentChats(Uid);
	    }

}
