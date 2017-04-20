package com.logicnext.bizmo;

import java.util.HashMap;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bizmo.object.User;
import com.logicnext.loaders.RecentChatLoader;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;



/*
 * Copyright (c) 2013, Logicnext Softwares and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

/**
* This Activity class is for displaying recent chat list
* @author kamal
*
*/

public class RecentChatList  extends MainActivity implements OnClickListener, LoaderCallbacks<Cursor> 
{
	 LayoutInflater inflator;
	 View businessView;
	 ChatAdapter mAdapter;
	 ListView lvChats;
	 static String muUid;//My UserID
	 ProgressBar prgDialog;
	 boolean isFromOncreate= false;;
	 
	 RecentChatLoader chatLoader;  //Database Loader
	 
	 //For loading images Async
	 protected ImageLoader imageLoader = ImageLoader.getInstance();
	 DisplayImageOptions options;
	
	 /**
	  * Set up Image loader for loading images Async
	  */
	 	private void setImageLoader()
	 	{
	 		options = new DisplayImageOptions.Builder()
	         .cacheInMemory(true) // default
	         .cacheOnDisc(true) // default
	         .bitmapConfig(Bitmap.Config.ARGB_8888) // default
	         .showImageForEmptyUri(R.drawable.connections_phone_default_user_image)//Default image
		         .showImageOnFail(R.drawable.connections_phone_default_user_image)
		         .showImageOnLoading(R.drawable.connections_phone_default_user_image)
	         .handler(new Handler()) // default
	         .build();
	 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
	 		.memoryCache(new WeakMemoryCache())
	 		.denyCacheImageMultipleSizesInMemory()
	 		
	 		.discCacheExtraOptions(250, 250, CompressFormat.JPEG, 100, null)//Size of cached images
	 		.discCache(new UnlimitedDiscCache(getCacheDir()))
	 		.imageDownloader(new BaseImageDownloader(this)) // defaut
	 		.build();
	 		L.disableLogging(); //Disable logging
	 		imageLoader.init(config);
	 	}
	 	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			inflator= getLayoutInflater();
			
			businessView = inflator.inflate(R.layout.recrent_cgat, null);
			rlMain.addView(businessView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			
			prgDialog= (ProgressBar)businessView.findViewById(R.id.prgRecentChats);
			
			isFromOncreate= true;
			//load all connections
		
			//set image loader
			setImageLoader();
			
			 chatLoader= new RecentChatLoader(RecentChatList.this, chatEngine, "");
			 getLoaderManager().initLoader(1, null, RecentChatList.this);
			 
			 //set Adapter
			 lvChats.setAdapter(mAdapter);
			
			//handle visiblity of Main UI
			ivInviteUser.setVisibility(View.VISIBLE);
			ivInviteUser.setImageResource(R.drawable.add_new_conversation);
			tvHeader.setText(R.string.chats);
			ivOpenMenu.setOnClickListener(this);
			ivInviteUser.setOnClickListener(this);
			
			//set value of My uid
			muUid= myUserPrefs.getString(Constants.USERID, "");
			
			lvChats= (ListView)businessView.findViewById(R.id.lvRecentChatList);
		
			//Create new Adapter
			mAdapter= new ChatAdapter(this,  null,true);
		  
			//handle click on chat list item
		  lvChats.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) 
	            {
	            	//Open chat screen
	            	TextView tv= (TextView)view.findViewById(R.id.tvRecentUserName);
	            	User u= (User)(tv.getTag());
	            	Intent in= new Intent(RecentChatList.this, ChatActivity.class);
	    			in.putExtra("uid", u.userId);
	    			in.putExtra("uname", u.userName);
	    	       startActivityForResult(in, 500);
	            }
	        });
		}
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			
			if(requestCode== 500)
			{
				//Refresh Chat list
				//getLoaderManager().restartLoader(1, null, RecentChatList.this);
				
				chatLoader.reset();
			}
		}
		
		@Override
		public void onClick(View v) {
			if(v== ivOpenMenu)
			{
				toggleDrawer();
			}
			else if(v== ivInviteUser)
			{
				Intent in= new Intent(this, ChooseMemberActivity.class);
				in.putExtra("isGroup", false);
				startActivity(in);
			}
		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return chatLoader;
		}


		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor(arg1);
		}


		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
		}
		
		
		
		private class ChatAdapter extends CursorAdapter
		{
			public class ViewHolder {
				TextView tvUserName;
				TextView tvDate;
				TextView tvMessage;
				ImageView ivUserImage;
				RelativeLayout rlParent;
			}
			public ChatAdapter(Context context, Cursor c, Boolean flags) {
				super(context, c, flags);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) 
			{
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				String msg  = cursor.getString(cursor.getColumnIndex("message_text"));
				String msg_id  = cursor.getString(cursor.getColumnIndex("_id"));
				String msg_time  = cursor.getString(cursor.getColumnIndex("message_time"));
				String msg_owner  = cursor.getString(cursor.getColumnIndex("message_owner"));
				String msg_to  = cursor.getString(cursor.getColumnIndex("message_to"));
				
				try {
					JSONObject msg_obj = new JSONObject(msg);
					viewHolder.tvMessage.setText(msg_obj.getString("message_text"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				viewHolder.tvDate.setText(chatEngine.getDateFromLong(msg_time));
				
				if(msg_owner.equals(muUid))
				{
						User u= chatEngine.getUserDetails(msg_to);
						if(u!= null)
						{
							viewHolder.tvUserName.setTag(u);
							imageLoader.displayImage(u.profile_image_url, viewHolder.ivUserImage, options);
							viewHolder.tvUserName.setText(u.userName);
						}
						else
						{
							//Download Details
							User uNew= chatEngine.getUserDetails(msg_owner);
							if(uNew!= null)
							{
								viewHolder.tvUserName.setTag(uNew);
								imageLoader.displayImage(uNew.profile_image_url, viewHolder.ivUserImage, options);
								viewHolder.tvUserName.setText(uNew.userName);
							}
						}
				}
				else if(msg_to.equals(muUid))
				{
					User u= chatEngine.getUserDetails(msg_owner);
					if(u!= null)
					{
						viewHolder.tvUserName.setTag(u);
						imageLoader.displayImage(u.profile_image_url, viewHolder.ivUserImage, options);
						viewHolder.tvUserName.setText(u.userName);
					}
					else
					{
						//Download Details
						User uNew= chatEngine.getUserDetails(msg_owner);
						if(uNew!= null)
						{
							viewHolder.tvUserName.setTag(uNew);
							imageLoader.displayImage(uNew.profile_image_url, viewHolder.ivUserImage, options);
							viewHolder.tvUserName.setText(uNew.userName);
						}
					}
				}
				//viewHolder.rlParent.setTag(msg_id);
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) 
			{
				View view= (RelativeLayout)inflator.inflate(R.layout.recent_chat_strip, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.rlParent = (RelativeLayout) view
						.findViewById(R.id.rlStrip);
				viewHolder.tvUserName = (TextView) view
						.findViewById(R.id.tvRecentUserName);
				viewHolder.tvDate = (TextView) view
						.findViewById(R.id.tvRecentChatDate);
				viewHolder.tvMessage = (TextView) view
						.findViewById(R.id.tvRecentChatMessage);
				viewHolder.ivUserImage = (ImageView) view
						.findViewById(R.id.ivRecentUserImage);
				view.setTag(viewHolder);
				return view;
			}
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			
			//Destroy loader for memory
			getLoaderManager().destroyLoader(1);
		}

		@Override
		protected void onResume() {
			super.onResume();
			
			if(!isFromOncreate)
			{
				
			}
			else
				isFromOncreate= false;
			
		}
	
}
