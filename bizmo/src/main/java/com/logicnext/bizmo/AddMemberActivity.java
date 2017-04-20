package com.logicnext.bizmo;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.bizmo.object.User;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKWallPostResult;

public class AddMemberActivity extends MainActivity implements OnClickListener
{
	SearchView srchSocial;
	ListView lvSocial;
	TextView tvCancel, tvSendInvite;
	LayoutInflater inflator;
	View addMemberView;
	private String countryCode;
	ProgressBar prg;
	
	
	String userName;
	
	UserList uAdapter;
	int mode=0;
	
	Vector<User> vecUsers= new Vector<User>();
	Vector<User> vecAdapter= new Vector<User>();
	Vector<User> vecSelected= new Vector<User>();
	
	
	private static final String[] sMyScope = new String[] {
        VKScope.FRIENDS,
        VKScope.WALL,
        VKScope.PHOTOS,
 };
	
	  protected ImageLoader imageLoader = ImageLoader.getInstance();
	 	DisplayImageOptions options;
		
		
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		inflator= getLayoutInflater();
		
		VKUIHelper.onCreate(this);
 		VKSdk.initialize(sdkListener, "4306235");
 		
		addMemberView = inflator.inflate(R.layout.add_member, null);
		rlMain.addView(addMemberView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		tvHeader.setText(R.string.add_mem);
		ivInviteUser.setVisibility(View.GONE);
		ivOpenMenu.setOnClickListener(this);
		
		setImageLoader();
		setUi();
		
		 JSONObject object;
			try {
				object = new JSONObject(myUserPrefs.getString(Constants.USER_DATA, ""));
				
				JSONObject phone= object.getJSONObject("phone");
				countryCode= (phone.getString("code")+"");
				
				userName= (object.getString("username")+"");
				
			} 
			catch (JSONException e) {
				e.printStackTrace();
				countryCode="";
				userName="";
			}
		
		mode= getIntent().getExtras().getInt("mode");
		
		prg.setVisibility(View.VISIBLE);
		switch (mode) {
		case 0:
			sendFacebookRequest(0);
			break;
		case 1:
			VKSdk.authorize(sMyScope, true, false);
			break;
		case 2:
			
			ReadPhoneBook task= new ReadPhoneBook();
			task.execute("");
		
			break;
		case 3:
			tvSendInvite.setText(R.string.send_email);
			ReadEmails etask= new ReadEmails();
			etask.execute("");
			break;

		default:
			break;
		}
		
	}
	
	
	private void setUi()
	{
		tvCancel= (TextView)addMemberView.findViewById(R.id.tvcancelPost);
		tvSendInvite= (TextView)addMemberView.findViewById(R.id.tvSendInvite);
		srchSocial= (SearchView)addMemberView.findViewById(R.id.srchSocial);
		lvSocial= (ListView)addMemberView.findViewById(R.id.lvSocialConnection);
		prg= (ProgressBar)addMemberView.findViewById(R.id.prgSocial);
		
		tvCancel.setOnClickListener(this);
		tvSendInvite.setOnClickListener(this);
		lvSocial.setTextFilterEnabled(true);
		
		srchSocial.setOnQueryTextListener(new SearchView.OnQueryTextListener() 
		 {
	            @Override
	            public boolean onQueryTextSubmit(String newText) {
	            	
	            	if(uAdapter!= null)
	            	{
	            		uAdapter.filter(newText);
	            	}
	            	
	                return true;
	            }

	            @Override
	            public boolean onQueryTextChange(String newText)
	            {
	            	if(uAdapter!= null)
	            	{
	            		uAdapter.filter(newText);
	            	}
	                return true;
	            }
	        });
	}
	
	
	  private  VKSdkListener sdkListener = new VKSdkListener() {
	        @Override
	        public void onCaptchaError(VKError captchaError) {
	        	Log.i("came here working", "Error");
	            new VKCaptchaDialog(captchaError).show();
	        }

	 
	        @Override
	        public void onAccessDenied(VKError authorizationError) {
	            new AlertDialog.Builder(AddMemberActivity.this)
	                    .setMessage(authorizationError.errorMessage)
	                    .show();
	        }

	        @Override
	        public void onReceiveNewToken(VKAccessToken newToken) {
	        	Log.i("came here working", newToken.toString()+"");
	        	
	        	getVKUserList();
	        }

	        @Override
	        public void onAcceptUserToken(VKAccessToken token) {
	        	Log.i("came here working", token.toString()+"");
	        	
	        	getVKUserList();
	        }

			@Override
			public void onTokenExpired(VKAccessToken arg0) {

				Log.i("came here working", "Expired");
	            VKSdk.authorize(sMyScope);
			}
	  };

	    
	    private class UserList extends BaseAdapter 
		{
			@Override
			public int getCount() 
			{
				return vecAdapter.size();
			}

			@Override
			public Object getItem(int arg0) 
			{
				return null;
			}

			@Override
			public long getItemId(int arg0) 
			{
				return 0;
			}

			@Override
			public View getView(int positioin, View convertView, ViewGroup arg2)
			{
				ViewHolder holder;
				if(convertView== null)
				{
					convertView= (LinearLayout)inflator.inflate(R.layout.add_member_strip, null);
					holder= new ViewHolder();
					holder.ivProfile= (ImageView)convertView.findViewById(R.id.ivConnectionImage);
					holder.tvName= (TextView)convertView.findViewById(R.id.tvMemberName);
					holder.chk= (CheckBox)convertView.findViewById(R.id.chlSelectContact);
					
					holder.chk.setOnCheckedChangeListener(new OnCheckedChangeListener() 
					{
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
						{
							int chkPos= (Integer) buttonView.getTag();
							if(isChecked)
							{
								if(!vecSelected.contains(vecAdapter.get(chkPos)))
								    vecSelected.add(vecAdapter.get(chkPos));
							}
							else
							{
								if(vecSelected.contains(vecAdapter.get(chkPos)))
									vecSelected.remove(vecAdapter.get(chkPos));
							}
						}
					});
					
					convertView.setTag(holder);
				}
				else
				{
					holder=(ViewHolder)convertView.getTag();
				}
				
				holder.tvName.setText(vecAdapter.get(positioin).userName+"");
				imageLoader.displayImage( vecAdapter.get(positioin).profile_image_url, holder.ivProfile, options);
				
				holder.chk.setTag(positioin);
				
				if(vecSelected.contains(vecAdapter.get(positioin)))
				{
					holder.chk.setChecked(true);
				}
				else
				{
					holder.chk.setChecked(false);
				}
				return convertView;
			}
			
			private class ViewHolder
			{
				ImageView ivProfile;
				TextView tvName;
				CheckBox chk;
			}
			
			
		    public void filter(String charText) {
		        charText = charText.toLowerCase(Locale.getDefault());
		       vecAdapter.clear();
		        if (charText.length() == 0) {
		        	vecAdapter.addAll(vecUsers);
		        }
		        else
		        {
		        	//Filter USer List
			            for (User wp : vecUsers)
			            {
			            	switch (mode) {
							case 0:
								  if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
					              {
					                	vecAdapter.add(wp);
					              }
								break;

							case 1:
								  if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
					              {
					                	vecAdapter.add(wp);
					              }
								break;
							case 2:
								  if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
					              {
					                	vecAdapter.add(wp);
					              }
								  else if (wp.phone.toLowerCase(Locale.getDefault()).contains(charText))
					              {
									  vecAdapter.add(wp);
					              }
								  break;
							case 3:
								  if (wp.userName.toLowerCase(Locale.getDefault()).contains(charText))
					              {
					                	vecAdapter.add(wp);
					              }
								  else if (wp.email.toLowerCase(Locale.getDefault()).contains(charText))
					              {
									  vecAdapter.add(wp);
					              }
								break;
							default:
								break;
							}
			            }
		        }
		            
		      
		        notifyDataSetChanged();
		    }
			
		}
	    
	    
	    /*************************************************************************************VK********************************************
	     * 
	     */
	    /**
	     * Post on Vk
	     * @param uid
	     */
	    private void postOnVK(String uid)
	    {
	    	VKRequest post;
	    	if(userName!= null && userName.length()>0)
	    		 post = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, uid, VKApiConst.ATTACHMENTS, null, VKApiConst.MESSAGE, userName+ " " +getResources().getString(R.string.invite_message)));
	    	else
	    		 post = VKApi.wall().post(VKParameters.from(VKApiConst.OWNER_ID, uid, VKApiConst.ATTACHMENTS, null, VKApiConst.MESSAGE, getResources().getString(R.string.invite_message_without_name)));
	    		
	        post.setModelClass(VKWallPostResult.class);
	        post.executeWithListener(new VKRequestListener() {
	            @Override
	            public void onComplete(VKResponse response) 
	            {
	                super.onComplete(response);
	                
	                finish();
	            }

	            @Override
	            public void onError(VKError error) {
	            }
	        });
	    }
	    
	    private void getVKUserList()
	    {
	    	VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,photo"));
	    	
	    	request.executeWithListener(new VKRequestListener() {
	            @Override
	            public void onComplete(VKResponse response) {
	            	Log.i("response", response.json.toString());
	            	
	            	parseVKResponse( response.json.toString());
	            	if(vecUsers!= null && vecUsers.size()>0)
	            	{
	            		vecAdapter.addAll(vecUsers);
	            		uAdapter= new UserList();
	            		
	            		lvSocial.setAdapter(uAdapter);
	            	}
	            	prg.setVisibility(View.GONE);
	            }
	            @Override
	            public void onError(VKError error) {
	                if (error.apiError != null)
	                	Log.i("response", error.apiError.errorMessage);
	                else
	                	Log.i("response", error.errorMessage);
	                prg.setVisibility(View.GONE);
	            }
	            @Override
	            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
	                                   long bytesTotal) {
	            }
	            @Override
	            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
	            }
	        });
	    }
	    private void parseVKResponse(String response)
	    {
	    	vecUsers= new Vector<User>();
	    
	    	try {
				JSONObject result= new JSONObject(response).getJSONObject("response");
				
				JSONArray arr= result.getJSONArray("items");
				
				for(int i=0; i<arr.length(); i++)
				{
					JSONObject friend= arr.getJSONObject(i);
					User u= new User();
					u.userName= friend.getString("first_name")+" "+friend.getString("last_name");
					u.profile_image_url= friend.getString("photo");
					u.userId= friend.getString("id");
					
					vecUsers.add(u);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    	//{"response":{"count":1,"items":[{"last_name":"Singh","id":250746315,"first_name":"Nitin","photo":"https:\/\/pp.vk.me\/c614628\/v614628315\/956d\/T8nLhq4Qixc.jpg","online":0}]}}
	    }
	    
	    
	    /***********************************END of Vk*************************************************
	     * 
	     */
	    
	    /***************************************************************facebook**************************************************************************
	     * 
	     */
	    
	    //mode0== friend list
	    //mode1== Post
	    private void sendFacebookRequest(final int requestTypr)
	    {
	    	
	    	 Session.openActiveSession(this, true, new Session.StatusCallback() 
			 {
				    // callback when session changes state
				    @Override
				    public void call(Session session, SessionState state, Exception exception) 
				    {
				    	
				    	if (session.isOpened())
				    	{
				    		if(requestTypr==0)
				    		{
				    			makeFriendsListRequest(session);
				    		}
				    		else
				    		{
				    			
				    		}
				    	}
				    }
				  });
	    }
	    
	    
	    private void makeFriendsListRequest(Session session)
	    {
	    	Request.newMyFriendsRequest(session, new GraphUserListCallback() 
	    	{
				@Override
				public void onCompleted(List<GraphUser> users, Response response)
				{
					parseFbUserList(users);
					vecAdapter.addAll(vecUsers);
					uAdapter= new UserList();
            		lvSocial.setAdapter(uAdapter);
					
					prg.setVisibility(View.GONE);
				}
			}).executeAsync();
	    }
	    private void parseFbUserList(List<GraphUser> users)
	    {
	    	for(int i=0; i<users.size(); i++)
	    	{
	    		User u= new User();
	    		
	    		u.userName= users.get(i).getName();
	    		u.userId= users.get(i).getId();
	    		String url= "http://graph.facebook.com/"+users.get(i).getId()+"/picture?type=normal";
	    		u.profile_image_url= url;
	    		
	    		vecUsers.add(u);
	    	}
	    }
	    
	    private void postOnFacebook(String uid)
	    {
	    	Bundle params = new Bundle();
	    	params.putString("name", "Bizmo");
	    	params.putString("link", "https://www.logicnext.com/");
	    	params.putString("to", uid);
	    	if(userName!= null && userName.length()>0)
	    		params.putString("description", userName+ " " +getResources().getString(R.string.invite_message));
	    	else
	    		params.putString("description",getResources().getString(R.string.invite_message_without_name));

	    	WebDialog feedDialog = (
	    	        new WebDialog.FeedDialogBuilder(this,
	    	            Session.getActiveSession(),
	    	            params))
	    	        .setOnCompleteListener(new OnCompleteListener()
	    	        {
						@Override
						public void onComplete(Bundle values,
								FacebookException error) {
							vecSelected.remove(0);
							if(vecSelected.size()>0)
								postOnFacebook(vecSelected.get(0).userId);
							else
							{
								finish();
							}
								
						}})
	    	        .build();
	    	    feedDialog.show();
	    }
	    
	    
	    /*********************************************************Invite throught SMS***************************************
	     * 
	     */
	    
	    private class ReadPhoneBook  extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 getContacts();
				 return "";
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
		    	 vecAdapter.addAll(vecUsers);
		    	 uAdapter= new UserList();
         		lvSocial.setAdapter(uAdapter);
					prg.setVisibility(View.GONE);
		     }
		 }
	    
	    private class ReadEmails  extends AsyncTask<String, Integer, String> 
			{
				 @Override
			     protected String doInBackground(String... aurl) 
			     {
					 getEmails();
					 return "";
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
			    	 vecAdapter.addAll(vecUsers);
			    	    uAdapter= new UserList();
	            		lvSocial.setAdapter(uAdapter);
						prg.setVisibility(View.GONE);
			     }
			 }
	    private String getJsonOfSelewcted()
	    {
	    	JSONArray arr= new JSONArray();
	    	for(int i=0 ; i<vecSelected.size(); i++)
	    	{
	    		User u= vecSelected.get(i);
	    		JSONObject objectJson= new JSONObject();
				try {
					objectJson.put("phone", u.phone);
					objectJson.put("name", u.userName);
					arr.put(objectJson);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	}
			return arr.toString();
	    }
	    
	    private class InviteBySMS extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String phonearr= getJsonOfSelewcted();
				 String uid= myUserPrefs.getString(Constants.USERID, "");
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				String result =BizmoEngine.getInstance().inviteUser(phonearr, auth, uid);
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
						Toast.makeText(AddMemberActivity.this, "Successfully sent invitation", Toast.LENGTH_LONG).show();
					
						finish();
			    	}
					else if(object.getString("errorcode").equals(HttpStatus.INVALID_CODE))
			    	{
			    		//Fail
						showAlert("Invalid Activation Code");
			    	}
			    	else
			    	{
			    		Toast.makeText(AddMemberActivity.this, "Failed. Try again later", Toast.LENGTH_LONG).show();
			    	}
				} 
				catch (JSONException e) {
					e.printStackTrace();
					//Faliure
				}
		    	 prg.setVisibility(View.GONE);
		     }
		 }
	    
	    
		private void getContacts()
		{
			vecUsers= new Vector<User>();
			if(ConnectionsActivity.hashPhoneContacts== null || ConnectionsActivity.hashPhoneContacts.size()==0)
			{
				ContentResolver cr = getContentResolver();
			     Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
					if (cur.getCount() > 0) 
					{
						while (cur.moveToNext()) 
						{
						   String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
							String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						
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
									user.userName= name;
									vecUsers.add(user);
								}
								phones.close();
							}
						}
						cur.close();
					}
			}
			else
			{
				vecUsers.addAll(ConnectionsActivity.hashPhoneContacts.values());
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
		
		private void getEmails()
		{
			vecUsers= new Vector<User>();
			if(ConnectionsActivity.hashPhoneContacts== null || ConnectionsActivity.hashPhoneContacts.size()==0)
			{
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
								 emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
								     User user= new User();
									user.email= emailAddress;
									user.userName= name;
									
									vecUsers.add(user);
								break;
							}
							emails.close();
						}
						cur.close();
					}
			}
			else
			{
				    Iterator it = ConnectionsActivity.hashPhoneContacts.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pairs = (Map.Entry)it.next();
				        User u= (User) pairs.getValue();
				        if(u.email!= null && u.email.length()>0)
				        {
				        	Log.i("adding email", u.email);
				        	vecUsers.add(u);
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				}
		}
		  
		
		private void sendEmail()
		{
			String[] emailArray= new String[vecSelected.size()];
			for(int i=0 ; i<vecSelected.size(); i++)
			{
				emailArray[i]= vecSelected.get(i).email;
			}
			Intent email = new Intent(android.content.Intent.ACTION_SEND);
			email.putExtra(android.content.Intent.EXTRA_EMAIL,emailArray);
			email.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
			if(userName== null || userName.length()==0)
				email.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.invite_message_without_name));
			else
				email.putExtra(android.content.Intent.EXTRA_TEXT,userName+ " " + getResources().getString(R.string.invite_message));
			email.setType("text/plain");
			startActivity(email);
		}
	
	 @Override
		public void onResume() {
		     super.onResume();
		     VKUIHelper.onResume(this);
		 }
		 @Override
			public void onDestroy() {
			     super.onDestroy();
			     VKUIHelper.onDestroy(this);
			 }
		 
		 @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			VKUIHelper.onActivityResult(requestCode, resultCode, data);
			try
			{
				Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
			}
			catch(NullPointerException ne)
			{
				
			}
		}
		 
	
	@Override
	public void onClick(View v) {
		
		if(v== ivOpenMenu)
		{
			toggleDrawer();
		}
		else if(v== tvCancel)
		{
			finish();
		}
		else if(v== tvSendInvite)
		{
			if(vecSelected== null || vecSelected.size()==0)
			{
				showAlert(getResources().getString(R.string.no_user_selected));
			}
			else
			{
				switch (mode) {
				case 0:
					
					postOnFacebook(vecSelected.get(0).userId);
					break;
					
				case 1:
					for(int i=0; i<vecSelected.size(); i++)
					{
						postOnVK(vecSelected.get(i).userId);
					}
					break;
					
				case 2:
					InviteBySMS invite= new InviteBySMS();
					invite.execute("");
					break;
				case 3:
					sendEmail();
					break;
	
				default:
					break;
				}
			}
		}
	}
	
	private void showAlert(String message)
	{
		AlertDialog.Builder builder= new  AlertDialog.Builder(this);
		builder.setMessage(message);
		
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.create().show();
		
	}

}
