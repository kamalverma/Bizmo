package com.logicnext.bizmo;

import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizmo.engine.ChatEngine;
import com.ui.common.Constants;

public class MainActivity extends Activity implements OnClickListener
{
	public SharedPreferences myUserPrefs;
	public SharedPreferences.Editor myUserEdit;
	
	DrawerLayout leftDrawer;
	RelativeLayout rlMain;
	ListView lvDrawer;
	String[] arrOptions={"Business", "Connections", "Chats", "More"};
	int[] arrImages={R.drawable.business_tap, R.drawable.connections_tap, R.drawable.chat_tap, R.drawable.more_tap};
	
	LayoutInflater inflator;
	DrawerAdapter adapter;
	TextView tvHeader;
	ImageView ivOpenMenu, ivInviteUser;
	boolean isFromRegistrtion;
	private boolean isDrawerOPened;
	ChatEngine chatEngine;
	String phoneNumber;
	String serverString="@dev1.logicnext.com";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		  setContentView(R.layout.main);
		  myUserPrefs = getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
		  inflator= getLayoutInflater();
		  rlMain= (RelativeLayout)findViewById(R.id.content_frame);
		  leftDrawer= (DrawerLayout)findViewById(R.id.drawer_layout);
		  lvDrawer= (ListView)findViewById(R.id.left_drawer);
		  ivInviteUser= (ImageView)findViewById(R.id.ivInviteUser);
		  ivOpenMenu= (ImageView)findViewById(R.id.ivOpenNavigation);
		  tvHeader= (TextView)findViewById(R.id.tvHeader);

		  ivInviteUser.setOnClickListener(this);
		  ivOpenMenu.setOnClickListener(this);
		  adapter= new DrawerAdapter();
		  lvDrawer.setAdapter(adapter);
		  
		  chatEngine= (ChatEngine)getApplication();
		  
		  //Login to Chat Server
		  if(chatEngine.connection== null || !chatEngine.connection.isConnected() || !chatEngine.connection.isAuthenticated())
		  {
			LoginToChat loging= new LoginToChat();
			loging.execute("");
		  }
			
		
		  
		  lvDrawer.setOnItemClickListener(new DrawerItemClickListener());
		  
		   leftDrawer.setDrawerListener(new DrawerListener() {
				@Override
				public void onDrawerStateChanged(int arg0) {
					
				}
				
				@Override
				public void onDrawerSlide(View arg0, float arg1) {
				}
				
				@Override
				public void onDrawerOpened(View arg0) {
					//
					isDrawerOPened= true;
				}
				
				@Override
				public void onDrawerClosed(View arg0) {
					isDrawerOPened= false;
				}
			});
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		
		if(chatEngine.connection != null && chatEngine.connection.isConnected() && chatEngine.connection.isAuthenticated())
		{
			Presence presence = new Presence(Presence.Type.available);
			chatEngine.connection.sendPacket(presence);
		}
	}
	
	private class LoginToChat extends AsyncTask<String, Integer, Boolean> 
	{
		 @Override
	     protected Boolean doInBackground(String... aurl) 
	     {
			 String uid= myUserPrefs.getString(Constants.USERID, "");
			  
			   JSONObject object;
				try {
					object = new JSONObject(myUserPrefs.getString(Constants.USER_DATA, ""));
					
					JSONObject phone= object.getJSONObject("phone");
					phoneNumber= (phone.getString("fullnumber")+"");
				} 
				catch (JSONException e) {
					e.printStackTrace();
					phoneNumber= "";
				}
				
			boolean isAuthenticated= chatEngine.login(uid, phoneNumber);
			 return isAuthenticated;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	
	    }
	     @Override
	     protected void onProgressUpdate(Integer... progress) 
	     {
	    	 
	     }

	     @Override
	     protected void onPostExecute(Boolean result) 
	     {
	    	if(result)
			{
				Log.i("Successfully SignedIn", "Carry On");
			}
			else
			{
				Log.i("Failed tp SignedIn", "start Again");
			}
	     }
	 }
	private class DrawerAdapter extends BaseAdapter
	{
		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			if(convertView== null)
				convertView= (LinearLayout)inflator.inflate(R.layout.option_strip, null);
			
			TextView tv= (TextView)convertView.findViewById(R.id.tvOption);
			
			tv.setText(arrOptions[position]);
			tv.setCompoundDrawablesWithIntrinsicBounds(arrImages[position],0, 0, 0);
			return convertView;
		}
		
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) 
	{
	    // Create a new fragment and specify the planet to show based on position
	if(position==0)
	{
		 Intent intent= new Intent(this, BusinessActivity.class);
	     startActivity(intent);
	}
	else if(position==1)
	{
		 Intent intent= new Intent(this, ConnectionsActivity.class);
	     startActivity(intent);
	}
	else if(position==2)
	{
		 Intent intent= new Intent(this, RecentChatList.class);
	     startActivity(intent);
	}
	else if(position==3)
	{
		 Intent intent= new Intent(this, MoreActivity.class);
	     startActivity(intent);
	}
	
	    // Highlight the selected item, update the title, and close the drawer
	    lvDrawer.setItemChecked(position, true);
	    leftDrawer.closeDrawer(lvDrawer);
	}

	@Override
	public void setTitle(CharSequence title) {
	   // mTitle = title;
	  //  getActionBar().setTitle(mTitle);
	}

	@Override
	public void onClick(View v) {
		
		if(v== ivOpenMenu)
		{
			toggleDrawer();
		}
	}
	
	public void toggleDrawer()
	{
		if(!isDrawerOPened)
		{
			leftDrawer.openDrawer(lvDrawer);
		}
			
		else
			leftDrawer.closeDrawer(lvDrawer);
	}
	
	
	

}
