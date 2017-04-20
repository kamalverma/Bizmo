package com.logicnext.bizmo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.android.databaseaccess.DatabaseHelper;
import com.ui.common.Constants;

public class SplashActivity extends Activity
{
	private SharedPreferences myUserPrefs;
	private SharedPreferences.Editor myUserEdit;
	 private final int SPLASH_DISPLAY_LENGHT = 1000;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	              WindowManager.LayoutParams.FLAG_FULLSCREEN);
	      
	      setContentView(R.layout.splash);
	      
	      myUserPrefs = this.getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
		  
		  myUserEdit.commit();
		  
		 
	      new Handler().postDelayed(new Runnable()
	      {
	          @Override
	          public void run() 
	          {
	        	  if(myUserPrefs.getBoolean(Constants.REG_COMPLETE, false))
	  			{
					 createDataBase();
	        		  Intent intent= new Intent(SplashActivity.this, RecentChatList.class);
		        	  startActivity(intent);
		        	  finish();
	  			}
	  			else
	  			{
	  				 Intent intent= new Intent(SplashActivity.this, RegistrationActivity.class);
		        	  startActivity(intent);
		        	  finish();
	  			}
	          }
	      }, SPLASH_DISPLAY_LENGHT);
		
	}
	private void createDataBase() 
    {
    	DatabaseHelper dbHelper = new DatabaseHelper(this, myUserPrefs.getString(Constants.USERID, ""));
		try
		{
			dbHelper.createDataBase();
		} 
		catch (Exception e) 
		{
			Log.e("Database Error MSG", e + "");
		}
	}

}
