package com.logicnext.bizmo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.TextView;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.bizmo.object.User;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class ConnectionsActivity extends MainActivity  implements OnClickListener
{
	ExpandableListView lvConnections;
	SearchView srchConeection;
	ProgressBar prg;
	ExpandableListAdapter adapter;
	BizmoEngine bEngine;
	GetConnections gettask;
	HashMap< String, Vector<User>> hashUsers;
	ArrayList<String> arrHeader;
	Vector<User> vecRequests= new Vector<User>();
	Vector<User> vecConnections= new Vector<User>();
	Vector<User> vecPhoneBook= new Vector<User>();
     LayoutInflater inflator;
 	View connectionView;
 	ImageView ivRefresh;
 	
 	SQLiteDatabase _database; 
 	
 	public static HashMap< String, User> hashPhoneContacts;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		inflator= getLayoutInflater();
 		connectionView = inflator.inflate(R.layout.connection, null);

 		rlMain.addView(connectionView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		bEngine= BizmoEngine.getInstance();
 		setUi(connectionView);
 		
 		LoadConnectionFromLocal load= new LoadConnectionFromLocal();
 		load.execute("");
 	}
 	
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		gettask= new GetConnections();
 		gettask.execute("");
 	}
 	
 	 @Override
		public void onClick(View v) {
			if(v.getId()== R.id.tvSms)
			{
				invitePopup.dismiss();
				Intent intent= new Intent(this, AddMemberActivity.class);
				intent.putExtra("mode", 2);
				startActivity(intent);
			}
			else if(v.getId()== R.id.tvSocial)
			{
				invitePopup.dismiss();
				showSocial();
			}
			else if(v.getId()== R.id.tvEmail)
			{
				invitePopup.dismiss();
				Intent intent= new Intent(this, AddMemberActivity.class);
				intent.putExtra("mode", 3);
				startActivity(intent);
			}
			else if(v.getId()== R.id.tvCancel)
			{
				if(invitePopup!= null && invitePopup.isShowing())
				{
					invitePopup.dismiss();
				}
			}
			else if(v.getId()== R.id.ivRefresh)
			{
				AddConnection connection= new AddConnection();
				connection.execute("");
			}
			else if(v.getId()== R.id.tvfb)
			{
				socialPopup.dismiss();
				Intent intent= new Intent(this, AddMemberActivity.class);
				intent.putExtra("mode", 0);
				startActivity(intent);
			}
			else if(v.getId()== R.id.tvVK)
			{
				socialPopup.dismiss();
				Intent intent= new Intent(this, AddMemberActivity.class);
				intent.putExtra("mode", 1);
				startActivity(intent);
			}
			else if(v.getId()== R.id.tvSocialCancel)
			{
				socialPopup.dismiss();
			}
			else if(v== ivInviteUser)
			{
				showINvitations();
			}
			else if(v== ivOpenMenu)
			{
				toggleDrawer();
			}
		}
	
    protected ImageLoader imageLoader = ImageLoader.getInstance();
 	DisplayImageOptions options;
	private String countryCode;
	
	
	
 	private void setImageLoader()
 	{
 		options = new DisplayImageOptions.Builder()
         .cacheInMemory(true) // default
         .cacheOnDisc(true) // default
         .bitmapConfig(Bitmap.Config.ARGB_8888) // default
         .showImageForEmptyUri(R.drawable.connections_phone_default_user_image)
	         .showImageOnFail(R.drawable.connections_phone_default_user_image)
	         .showImageOnLoading(R.drawable.connections_phone_default_user_image)
         .handler(new Handler()) // default
         .build();
 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
 		.memoryCache(new WeakMemoryCache())
 		.denyCacheImageMultipleSizesInMemory()
 		
 		.discCacheExtraOptions(250, 250, CompressFormat.JPEG, 100, null)
 		.discCache(new UnlimitedDiscCache(getCacheDir()))
 		.imageDownloader(new BaseImageDownloader(this)) // defau
 		.build();
 		L.disableLogging();
 		imageLoader.init(config);
 	}
 	
 	
	 private void setUi(View v)
	 {
		 lvConnections= (ExpandableListView)v.findViewById(R.id.lvConnections);
		 srchConeection= (SearchView)v.findViewById(R.id.srchContacts);
		 ivRefresh= (ImageView)v.findViewById(R.id.ivRefresh);
		 ivInviteUser.setVisibility(View.VISIBLE);
		 ivInviteUser.setOnClickListener(this);
		 ivRefresh.setOnClickListener(this);
		 prg= (ProgressBar)v.findViewById(R.id.prgGetConnection); 
		 
		    JSONObject object;
			try {
				object = new JSONObject(myUserPrefs.getString(Constants.USER_DATA, ""));
				JSONObject phone= object.getJSONObject("phone");
				countryCode= (phone.getString("code")+"");
			} catch (JSONException e) {
				e.printStackTrace();
				countryCode="";
			}
		
		 tvHeader.setText(R.string.connection);
		 setImageLoader();
	
		 
		 srchConeection.setOnQueryTextListener(new SearchView.OnQueryTextListener() 
		 {
	            @Override
	            public boolean onQueryTextSubmit(String newText) {
	            	
	            	if(adapter!= null)
	            		adapter.filter(newText);
	                return true;
	            }

	            @Override
	            public boolean onQueryTextChange(String newText)
	            {
	            	if(adapter!= null)
	            		adapter.filter(newText);
	                return true;
	            }
	        });
		 
		 ivOpenMenu.setOnClickListener(this);
	 }
	 
	 
	 @Override
	public void onStop() {
		super.onStop();
	}
	 
		PopupWindow invitePopup;
		PopupWindow socialPopup;
		int cablePosition;
		
		/**
		 * Display the invitation popup
		 */
	 private void showINvitations()
	 {
			if(invitePopup!= null && invitePopup.isShowing())
				return;
			
			View v= new View(this);
			v= getLayoutInflater().inflate(R.layout.invite_popup, null);
			
			TextView tvSms, tvEmail, tvSocial, tvCanel, yvInvite;;
			ImageView ivDismiss;
			
			tvSms= (TextView)v.findViewById(R.id.tvSms);
			tvEmail= (TextView)v.findViewById(R.id.tvEmail);
			tvSocial= (TextView)v.findViewById(R.id.tvSocial);
			tvCanel= (TextView)v.findViewById(R.id.tvCancel);
			ivDismiss= (ImageView)v.findViewById(R.id.ivDismiss);
			
			
			yvInvite= (TextView)v.findViewById(R.id.tvInviteHeader);
			
			Typeface bold= FontUtilitirs.calibriBold(this);
			Typeface regular= FontUtilitirs.calibriBlack(this);
			
			tvSms.setOnClickListener(this);
			tvEmail.setOnClickListener(this);
			tvSocial.setOnClickListener(this);
			tvCanel.setOnClickListener(this);
			invitePopup= new PopupWindow(v,LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, false);
			invitePopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.overlay));
			invitePopup.setOutsideTouchable(true);
			
			invitePopup.showAtLocation(rlMain, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			
			tvCanel.setTypeface(bold);
			yvInvite.setTypeface(regular);
			tvSms.setTypeface(regular);
			tvSocial.setTypeface(regular);
			tvEmail.setTypeface(regular);
			
			ivDismiss.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
						if(invitePopup!= null)
							invitePopup.dismiss();
					return true;
				}
			});
	 }
	 
	 
	 
	 /**
	  * Display the social popup 
	  */
	 private void showSocial()
	 {
			if(socialPopup!= null && socialPopup.isShowing())
				return;
			
			View v= new View(this);
			v= getLayoutInflater().inflate(R.layout.social_poopup, null);
			
			TextView tvFb, tvVK,  tvCanel, tvSocial;
			ImageView ivDismiss;
			
			tvFb= (TextView)v.findViewById(R.id.tvfb);
			tvVK= (TextView)v.findViewById(R.id.tvVK);
			tvSocial= (TextView)v.findViewById(R.id.tvSocialHeader);
			tvCanel= (TextView)v.findViewById(R.id.tvSocialCancel);
			ivDismiss= (ImageView)v.findViewById(R.id.ivDismissSocial);
			
			
			
			Typeface bold= FontUtilitirs.calibriBold(this);
			Typeface regular= FontUtilitirs.calibriBlack(this);
			
			tvFb.setOnClickListener(this);
			tvVK.setOnClickListener(this);
			tvSocial.setOnClickListener(this);
			tvCanel.setOnClickListener(this);
			socialPopup= new PopupWindow(v,LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, false);
			socialPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.overlay));
			socialPopup.setOutsideTouchable(true);
			
			socialPopup.showAtLocation(rlMain, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			
			tvCanel.setTypeface(bold);
			tvFb.setTypeface(regular);
			tvVK.setTypeface(regular);
			tvSocial.setTypeface(regular);
			
			ivDismiss.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
						if(invitePopup!= null)
							invitePopup.dismiss();
					return true;
				}
			});
	 }
	 
	 
	 /**
	  * This Async task will load all connection & filter them with
	  * phoneBook 
	  * Also Load the UI in end of task
	  * @author kamal
	  *TODO Load contacts from local database
	  */
		private class GetConnections extends AsyncTask<String, Integer, String> 
		{
			
			String relod;
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 relod= aurl[0];
				 String uid= myUserPrefs.getString(Constants.USERID, "");
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				 String result = bEngine.getConnection(uid, auth);
				 getPhoneBook(false);
				 JSONObject object;
					try
					{
						object = new JSONObject(result);
						if(object.getString("errorcode").equals(HttpStatus.SUCCESS))
				    	{
				    		//Success
							parseAndInsertInDB(object);
				    	}
				    	else
				    	{
				    	}
					} 
					catch (JSONException e) {
						e.printStackTrace();
					}
				 return result;
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
		     protected void onPostExecute(String result) 
		     {
					if(relod.equals("yes"))
					{
						LoadConnectionFromLocal load= new LoadConnectionFromLocal();
						load.execute("");
					}
					prg.setVisibility(View.GONE);
		     }
		 }
		
		private class LoadConnectionFromLocal extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 Log.i("loading from local", "faster");
				vecConnections= chatEngine.getConnections();
				vecRequests= chatEngine.getRequests();
				vecPhoneBook= chatEngine.getPhoneBook();
				 return "";
		     }

		     @Override
		    protected void onPreExecute() 
		    {
		    	super.onPreExecute();
		    	prg.setVisibility(View.VISIBLE);
		    	hashUsers= new LinkedHashMap<String, Vector<User>>();
		    }
		     @Override
		     protected void onProgressUpdate(Integer... progress) 
		     {
		    	 
		     }
		     @Override
		     protected void onPostExecute(String result) 
		     {
		    	if(vecConnections.size()==0 && vecPhoneBook.size()==0 && vecRequests.size()==0)
		    	{
		    		// no entry in phonebook , Load 
			    	 gettask= new GetConnections();
		    		 gettask.execute("yes");
		    	}
		    	else 
		    	{
		    		 Log.i("Loading complete", "Creating UI");
					//array for showing header of contact list
					arrHeader= new ArrayList<String>();
					//Get All requests
						//if user has any pending requests
						if(vecRequests.size()>0)
						{
							arrHeader.add(getResources().getString(R.string.conn_req));
							Vector<User> vecRequest= new Vector<User>();
							vecRequest.addAll(vecRequests);
							//put in hashmap to display list
							hashUsers.put(getResources().getString(R.string.conn_req), vecRequest);
						}
					   //Get All Connections
						arrHeader.add(getResources().getString(R.string.bizmo_conn));
						
						Vector<User> vecConn= new Vector<User>();
						vecConn.addAll(vecConnections);
						
						hashUsers.put(getResources().getString(R.string.bizmo_conn), vecConn);
					//Get All Connections
			
						arrHeader.add(getResources().getString(R.string.phonebook));
						
						Vector<User> vecNotConnections= new Vector<User>();
						vecNotConnections.addAll(vecPhoneBook);
				
						hashUsers.put(getResources().getString(R.string.phonebook), vecNotConnections);
						if(adapter== null)
						{
							adapter= new ExpandableListAdapter();
							lvConnections.setAdapter(adapter);
						}
						else
						{
							adapter.notifyDataSetChanged();
						}
						prg.setVisibility(View.GONE);
		    	}
		     }
		 }
		
		/**
		 * Response to friend request
		 * parametrs are requestid & response
		 * @author kamal
		 *
		 */
		private class RespondRequest extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String rid= aurl[0];
				 String response= aurl[1];
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				 String uid= myUserPrefs.getString(Constants.USERID, "");
				 String result = bEngine.acceptConnection(rid, response, auth, uid);
				
				 return result;
		     }

		     @Override
		    protected void onPreExecute() 
		    {
		    	super.onPreExecute();
		    	prg.setVisibility(View.VISIBLE);
		    	lvConnections.setEnabled(false);
		    }
		     @Override
		     protected void onProgressUpdate(Integer... progress) 
		     {
		    	 
		     }
		     @Override
		     protected void onPostExecute(String result) 
		     {
				gettask= new GetConnections();
				gettask.execute("yes");
		     }
		 }
		
		
		/**
		 * Parse the response of getContact &
		 * sort them accordingly 
		 * @param objet response object
		 */
		private void parseAndInsertInDB(JSONObject objet)
		{
			//A new local hash to filter
			//we will not make any changes in global phone book hashmap
			HashMap< String , User> hashLocal= new HashMap<String, User>();
			hashLocal.putAll(hashPhoneContacts);
			Vector<User> vecRequest= new Vector<User>();
			Vector<User> vecCon= new Vector<User>();
			Vector<User> vecNotConnections= new Vector<User>();
			
			try 
			{
				JSONArray arrRequest= objet.getJSONArray("requests");
				//if user has any pending requests
				if(arrRequest.length()>0)
				{
					for(int i=0; i<arrRequest.length(); i++)
					{
						JSONObject reqobject= arrRequest.getJSONObject(i);
						User u= new User();
						u.userName= reqobject.getString("username");
						u.reqId= reqobject.getString("request_id");
						u.email= reqobject.getString("email");
						u.region= reqobject.getString("region");
						u.status= reqobject.getString("status");
						u.profile_image_url= reqobject.getString("profile_image_url");
						//u.reqType= reqobject.getString("request_type");
						JSONObject phone= reqobject.getJSONObject("phone");
						u.phone= phone.getString("code")+phone.getString("number");
						if(u.userName== null || u.userName.length()<=0)
						{
							u.userName= u.phone;
						}
						
						//if phone book has this contacts already
						//Replace the user name from phone book
						//and remove from local phone book hashmap
						if(hashLocal.containsKey(u.phone))
						{
							u.userName= hashLocal.get(u.phone).userName;
							hashLocal.remove(u.phone);
						}
						//Add user in list
						vecRequest.add(u);
					}
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			
			//Get All Connections
			try 
			{
				JSONArray arrConnection= objet.getJSONArray("connections");
				
				
				for(int i=0; i<arrConnection.length(); i++)
				{
					JSONObject conobject= arrConnection.getJSONObject(i);
					User u= new User();
					u.userName= conobject.getString("username");
					u.userId= conobject.getString("userid");
					u.email= conobject.getString("email");
					u.region= conobject.getString("region");
					u.status= conobject.getString("status");
					u.profile_image_url= conobject.getString("profile_image_url");
					JSONObject phone= conobject.getJSONObject("phone");
					u.phone= phone.getString("code")+phone.getString("number");
					
					if(u.userName== null || u.userName.length()<=0)
					{
						u.userName= u.phone;
					}
					
					if(hashLocal.containsKey(u.phone))
					{
						u.userName= hashLocal.get(u.phone).userName;
						hashLocal.remove(u.phone);
					}
					vecCon.add(u);
				}
			}
			catch (JSONException e){
				e.printStackTrace();
			}
			//Get All Connections
			try 
			{
				JSONArray arrNorConnection= objet.getJSONArray("notconnected");
				for(int i=0; i<arrNorConnection.length(); i++)
				{
					JSONObject notconobject= arrNorConnection.getJSONObject(i);
					User u= new User();
					u.userName= notconobject.getString("username");
					u.userId= notconobject.getString("userid");
					u.email= notconobject.getString("email");
					u.region= notconobject.getString("region");
					u.status= notconobject.getString("status");
					u.profile_image_url= notconobject.getString("profile_image_url");
					JSONObject phone= notconobject.getJSONObject("phone");
					
					u.phone= phone.getString("code")+phone.getString("number");
					if(u.userName== null || u.userName.length()<=0)
					{
						u.userName= u.phone;
					}
					if(hashLocal.containsKey(u.phone))
					{
						User u1= hashLocal.get(u.phone);
						u1.isRequestRequired= true;
						u1.userId= u.userId;
						u1.profile_image_url= u.profile_image_url;
					}
				}
				vecNotConnections.addAll(hashLocal.values());
				chatEngine.insertIntoConnections(vecRequest, vecCon, vecNotConnections);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Contact listr adapter
		 * @author kamal
		 *
		 */
		public class ExpandableListAdapter extends BaseExpandableListAdapter
		{
		    @Override
		    public Object getChild(int groupPosition, int childPosititon) {
		        return hashUsers.get(arrHeader.get(groupPosition))
		                .get(childPosititon);
		    }
		 
		    @Override
		    public long getChildId(int groupPosition, int childPosition) {
		        return childPosition;
		    }
		 
		    @Override
		    public View getChildView(int groupPosition, final int childPosition,
		            boolean isLastChild, View convertView, ViewGroup parent) {
		 
		        if (convertView == null) {
		   
		            convertView = inflator.inflate(R.layout.conn_strip, null);
		            
		            convertView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {

							User u= (User)v.getTag();
							if(u.userId!= null && u.userId.length()>0)
							{
								Bundle b= new Bundle();
								b.putString("uid", u.userId);
								b.putBoolean("isMe", false);
								b.putBoolean("isReq", u.isRequestRequired);
							
								Intent in= new Intent(ConnectionsActivity.this, ProfilePageActivity.class);
								in.putExtra("bundle", b);
								startActivity(in);
							}
						}
					});
		        }
		        TextView tvAccept= (TextView)convertView.findViewById(R.id.tvaccept);
		        TextView tvlater= (TextView)convertView.findViewById(R.id.tvacceptLater);
		        TextView tvUserName= (TextView)convertView.findViewById(R.id.tvUserName);
		        ImageView ivUser= (ImageView)convertView.findViewById(R.id.ivConnectionImage);
		        ImageView ivBizmpUser= (ImageView)convertView.findViewById(R.id.ivBizmoUser);
		        ivBizmpUser.setVisibility(View.GONE);
		        
		        if(!arrHeader.get(groupPosition).equalsIgnoreCase(getResources().getString(R.string.conn_req)))
		        {
		        	tvAccept.setVisibility(View.GONE);
		        	tvlater.setVisibility(View.GONE);
		        }
		        else
		        {
		        	tvAccept.setVisibility(View.VISIBLE);
		        	tvAccept.setTag(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition).reqId);
		        	tvlater.setTag(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition).reqId);
		        	tvlater.setVisibility(View.VISIBLE);
		        }
		        
		        if(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition).isRequestRequired)
		        {
		        	ivBizmpUser.setVisibility(View.VISIBLE);
		        }
		        
		        tvAccept.setOnClickListener(new OnClickListener() 
		        {
					@Override
					public void onClick(View v) 
					{
						RespondRequest task= new RespondRequest();
						
						String[] arr= {v.getTag().toString(), "YES"};
						task.execute(arr);
					}
				});
		        
             tvlater.setOnClickListener(new OnClickListener() 
             {
					@Override
					public void onClick(View v)
					{
						RespondRequest task= new RespondRequest();
						String[] arr= {v.getTag().toString(), "NO"};
						task.execute(arr);
					}
				});
		        
		        tvUserName.setText(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition).userName);
		        imageLoader.displayImage(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition).profile_image_url, ivUser, options);
		        convertView.setTag(hashUsers.get(arrHeader.get(groupPosition)).get(childPosition));
		 
		        return convertView;
		    }
		 
		    @Override
		    public int getChildrenCount(int groupPosition) {
		        return hashUsers.get(arrHeader.get(groupPosition)).size();
		    }
		 
		    @Override
		    public Object getGroup(int groupPosition) {
		        return arrHeader.get(groupPosition);
		    }
		 
		    @Override
		    public int getGroupCount() {
		        return arrHeader.size();
		    }
		 
		    @Override
		    public long getGroupId(int groupPosition) {
		        return groupPosition;
		    }
		 
		    @Override
		    public View getGroupView(int groupPosition, boolean isExpanded,
		            View convertView, ViewGroup parent) 
		    {
		        if (convertView == null) {
		            convertView = inflator.inflate(R.layout.connection_header, null);
		        }
		 
		        lvConnections.expandGroup(groupPosition);
		        TextView tvHead = (TextView) convertView
		                .findViewById(R.id.tvConnheader);
		        tvHead.setTypeface(FontUtilitirs.calibriBold(ConnectionsActivity.this));
		        
		        tvHead.setText(arrHeader.get(groupPosition));
		  //      lblListHeader.setText(headerTitle);
		 
		        return convertView;
		    }
		 
		    @Override
		    public boolean hasStableIds() {
		        return false;
		    }
		 
		    @Override
		    public boolean isChildSelectable(int groupPosition, int childPosition) {
		        return true;
		    }
		    
		    
		    
		    /**
		     * Filter the contacts list according to 
		     * search query
		     * @param charText  query
		     */
		    public void filter(String charText) {
		        charText = charText.toLowerCase(Locale.getDefault());
		        
		        
		        if(hashUsers.containsKey(getResources().getString(R.string.conn_req)))
		        {
		        	 hashUsers.get(getResources().getString(R.string.conn_req)).clear();
		        }
		        hashUsers.get(getResources().getString(R.string.bizmo_conn)).clear();
		        hashUsers.get(getResources().getString(R.string.phonebook)).clear();
		        
		        
		        if (charText.length() == 0) {
		        	 if(hashUsers.containsKey(getResources().getString(R.string.conn_req)))
		        		 hashUsers.get(arrHeader.get(0)).addAll(vecRequests);
		        	 
		        	hashUsers.get(getResources().getString(R.string.bizmo_conn)).addAll(vecConnections);
		        	hashUsers.get(getResources().getString(R.string.phonebook)).addAll(vecPhoneBook);
		        }
		        else
		        {
		        	//Filter requests
		            if(hashUsers.containsKey(getResources().getString(R.string.conn_req)))
		            {
			            for (User wp : vecRequests)
			            {
			                if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
			                {
			                	hashUsers.get(getResources().getString(R.string.conn_req)).add(wp);
			                }
			                else if (wp.phone.toLowerCase(Locale.getDefault()).contains(charText))
			                {
			                	hashUsers.get(getResources().getString(R.string.conn_req)).add(wp);
			                }
			            }
		            }
		            
		            //Filter connection
		            for (User wp : vecConnections)
		            {
		                if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
		                {
		                	hashUsers.get(getResources().getString(R.string.bizmo_conn)).add(wp);
		                }
		                else if (wp.phone.toLowerCase(Locale.getDefault()).contains(charText))
		                {
		                	hashUsers.get(getResources().getString(R.string.bizmo_conn)).add(wp);
		                }
		            }
		            
		            //Filter Phone book
		            for (User wp : vecPhoneBook)
		            {
		                if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
		                {
		                	hashUsers.get(getResources().getString(R.string.phonebook)).add(wp);
		                }
		                else if (wp.phone.toLowerCase(Locale.getDefault()).contains(charText))
		                {
		                	hashUsers.get(getResources().getString(R.string.phonebook)).add(wp);
		                }
		            }
		        }
		        notifyDataSetChanged();
		    }
		}
		
		
		
		/**
		 * read the contacts from phone book & put them in a hash map
		 * @author kamal 
		 */
		private void getPhoneBook(boolean isRefresh)
		{
			if(isRefresh || hashPhoneContacts==null || hashPhoneContacts.size()==0 )
			{
				hashPhoneContacts= new HashMap<String, User>();
				ContentResolver cr = getContentResolver();
			     Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
					if (cur.getCount() > 0) 
					{
						while (cur.moveToNext()) 
						{
						   String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
							String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						   
							Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
									null,ContactsContract.CommonDataKinds.Email.CONTACT_ID+ " = " + id, null, null);
							String emailAddress="";
							while (emails.moveToNext()) 
							{
								// This would allow you get several email addresses
								 emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
								break;
							}
							emails.close();
							if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
							{
								// You know it has a number so now query it like this
								Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + id, null, null);
								while (phones.moveToNext()) 
								{
									User user= new User();
									String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
									if(phoneNumber.startsWith("+"))
									{
										phoneNumber=phoneNumber.replace("+", "");
									}
									else 
									{
										String iCode=getInternational(phoneNumber);
										if(iCode.length()>0)
											phoneNumber=phoneNumber.replace(iCode, "");
										else
										{
											String nCode= getNational(phoneNumber);
											
											if(nCode.length()>0)
												phoneNumber=phoneNumber.replace(nCode, "");
											
											
											phoneNumber= countryCode+phoneNumber;
										}
									}
									phoneNumber=phoneNumber.replaceAll("[\\D]", "");
									user.phone= phoneNumber;
									user.email= emailAddress;
									user.userName= name;
									hashPhoneContacts.put(phoneNumber, user);
								}
								phones.close();
							}
						}
						cur.close();
					}
			}
		  }
		
		private String getInternational(String phoneNumber)
		{
			String isFound= "";
			try 
			{
				JSONArray arrInter= new JSONArray(myUserPrefs.getString(Constants.MYINTERNATION, "[]"));
				
				for(int i= 0; i<arrInter.length(); i++)
				{
					 if(phoneNumber.startsWith(arrInter.getString(i)))//Remove international code
					{
						isFound= arrInter.getString(i);
						break;
					}
				}
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
			return isFound;
		}
		
		private String getNational(String phoneNumber)
		{
			String isFound= "";
			JSONArray arrNational;
			try {
				arrNational = new JSONArray(myUserPrefs.getString(Constants.MYNATIONAL, "[]"));
				for(int i= 0; i<arrNational.length(); i++)
				{
					 if(phoneNumber.startsWith(arrNational.getString(i)))//Remove international code
					{
						isFound= arrNational.getString(i);
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return isFound;
		}
		
		/*
		 * Async task to GetCinyance list  
		 * @author kamal
		 *
		 */
		private class AddConnection extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String allowAll, Autoadd;
				 String uid= myUserPrefs.getString(Constants.USERID, "");
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				  if(myUserPrefs.getBoolean(Constants.AUTO_ADD_CONNECTION, true))
					{
						Autoadd= "YES";
					}
					else
					{
						Autoadd= "NO";
					}
				  if(myUserPrefs.getBoolean(Constants.ALLOW_OTHERS_TO_ADD, true))
					{
						allowAll= "YES";
					}
					else
					{
						allowAll= "NO";
					}
				 
				   getPhoneBook(true);
				  Vector<User> vec= new Vector<User>();
				  vec.addAll(hashPhoneContacts.values());
				  String arrUsers= getJsonArrayofUsers(vec);
				 String result = BizmoEngine.getInstance().addConnections(uid, auth, arrUsers, Autoadd, allowAll);
				 return result;
		     }

		     @Override
		    protected void onPreExecute() 
		    {
		    	super.onPreExecute();
		    	prg.setVisibility(View.VISIBLE);
		    }
		     @Override
		     protected void onProgressUpdate(Integer... progress) 
		     {
		    	 
		     }

		     @Override
		     protected void onPostExecute(String result) 
		     {
		    	JSONObject object;
				try
				{
					object = new JSONObject(result);
					if(object.getString("errorcode").equals(HttpStatus.SUCCESS))
			    	{
			    		//Success
						gettask= new GetConnections();
						gettask.execute("yes");
			    	}
			    	else
			    	{
			    		prg.setVisibility(View.GONE);
			    	}
				} 
				catch (JSONException e) {
					e.printStackTrace();
					prg.setVisibility(View.GONE);
				}
				//Send to main Screen
		     }
		 }
		
		
		private String getJsonArrayofUsers(Vector<User> vecUser)
		{
			JSONArray arrConArray= new JSONArray();
			for(User u:vecUser)
			{
				JSONObject obj= new JSONObject();
				try {
					obj.put("phone", u.phone);
					obj.put("name", u.userName);
					obj.put("email", u.email);
					arrConArray.put(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
	   return arrConArray.toString();
	}
		
		
		
}
