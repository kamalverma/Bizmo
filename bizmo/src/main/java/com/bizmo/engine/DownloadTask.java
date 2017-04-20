package com.bizmo.engine;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ui.common.Constants;

public class DownloadTask extends IntentService 
{
	BizmoEngine b;
	String uid; 
	String authToken;
	public SharedPreferences myUserPrefs;
	public SharedPreferences.Editor myUserEdit;

	String imagepath= ChatEngine.FolderPath+Constants.ImagePath; 
	
	public DownloadTask(String name) {
		super(name);
	}
	
	public DownloadTask() {
		super("TaskManager");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.d("TAG", "Service started New Task");
		String tid= intent.getExtras().getString("tid");
		String path= intent.getExtras().getString("path");
		String type= intent.getExtras().getString("type");
	}
	
	

}
