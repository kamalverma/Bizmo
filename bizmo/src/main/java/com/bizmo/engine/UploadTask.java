package com.bizmo.engine;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.bizmo.engine.ChatEngine.UploadCompletionReceiver;
import com.ui.common.Constants;

public class UploadTask extends IntentService 
{
	BizmoEngine bEngine;;
	String uid; 
	String authToken;
	public SharedPreferences myUserPrefs;
	public SharedPreferences.Editor myUserEdit;

	public UploadTask(String name) {
		super(name);
	}
	
	public UploadTask() {
		super("TaskManager");
	}
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		Log.d("TAG", "Service started New Task");
		
		Bundle b= intent.getExtras().getBundle("bundle");
		String path= b.getString("path");
		String type= b.getString("type");
		
		bEngine= BizmoEngine.getInstance();
		 myUserPrefs = getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		 myUserEdit =  myUserPrefs.edit();
		
		 uid= myUserPrefs.getString(Constants.USERID, "");
		 authToken= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
		 
		String response= bEngine.uploadImage(uid, authToken, path, type);
		Log.i("response", response);
		
		try 
		{
			JSONObject result= new JSONObject(response);
			if(result.getString("errorcode").equals("200"))
			{
				String gpath= result.getString("file");
				Log.i("Succes in task ", response);
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(UploadCompletionReceiver.ACTION_RESP);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				b.putString("gpath", gpath);
				broadcastIntent.putExtra("bundle", b);
				sendBroadcast(broadcastIntent);
			}
			else
			{
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	//	{"user_id":"5370a4f8bdd094f92f7675b9","file":"http:\/\/dev1.logicnext.com\/bizmo\/application\/uploads\/chat\/5370a4f8bdd094f92f7675b9_1400569999_img.png","version":1,"success":true,"errorcode":200,"message":"Success"}

	}
}
