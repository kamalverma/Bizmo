package com.logicnext.bizmo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.bizmo.object.User;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;
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
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

public class MyProfileActivity extends MainActivity implements OnClickListener
{

	TextView tvMobile, tvMobileNumber, tvProfilepage,  tvfacebook, tvVkontakte, tvDeleteAcc, tvEditImage;;
	TextView tvSave;
	ImageView ivProfileImage;
	EditText edtName, edtStatus;
	Spinner spnCountry;
	ArrayList<String> arrCountries= new ArrayList<String>();
	private int Gallary_REQUEST=200;
	ProgressBar prgImage, prgProfile;
	
	
	String url="";
	
	BizmoEngine bengine;
	
	private static final String[] sMyScope = new String[] {
        VKScope.FRIENDS,
        VKScope.WALL,
        VKScope.PHOTOS,
        VKScope.NOHTTPS
};
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	private void setImageLoader()
	{
		options = new DisplayImageOptions.Builder()
        .cacheInMemory(true) // default
        .cacheOnDisc(true) // default
           .showImageForEmptyUri(R.drawable.my_profile_default_image)
	         .showImageOnFail(R.drawable.my_profile_default_image)
	         .showImageOnLoading(R.drawable.my_profile_default_image)
        .bitmapConfig(Bitmap.Config.ARGB_8888) // default
        .displayer(new RoundedBitmapDisplayer(4)) // default
        .handler(new Handler()) // default
        .build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
		.memoryCache(new WeakMemoryCache())
		.denyCacheImageMultipleSizesInMemory()
		.discCacheExtraOptions(250, 250, CompressFormat.JPEG, 100, null)
		.discCache(new UnlimitedDiscCache(this.getCacheDir()))
		.imageDownloader(new BaseImageDownloader(this)) // defau
		.build();
		L.disableLogging();
		imageLoader.init(config);
	}
	
	 LayoutInflater inflator;
	  	View myProfileView;
	  	@Override
	  	protected void onCreate(Bundle savedInstanceState) {
	  		super.onCreate(savedInstanceState);
	  		
	  		inflator= getLayoutInflater();
	  		myProfileView = inflator.inflate(R.layout.myprofile, null);

	  		rlMain.addView(myProfileView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	  		bengine= BizmoEngine.getInstance();
	  		setUi(myProfileView);
	  		setFont();
	  	    setImageLoader();
	  	   
	  	 VKUIHelper.onCreate(this);
		 parseCountries();
		 setProfileValues();
		 GetProfile getTask= new GetProfile();
		 getTask.execute("");
		 VKSdk.initialize(sdkListener, "4306235");
	    
	  	}
	 
	    private  VKSdkListener sdkListener = new VKSdkListener() {
	        @Override
	        public void onCaptchaError(VKError captchaError) {
	        	Log.i("came here working", "Error");
	            new VKCaptchaDialog(captchaError).show();
	        }

	        @Override
	        public void onTokenExpired(VKAccessToken expiredToken) {
	        	Log.i("came here working", "Expired");
	            VKSdk.authorize(sMyScope);
	        }

	        @Override
	        public void onAccessDenied(VKError authorizationError) {
	            new AlertDialog.Builder(MyProfileActivity.this)
	                    .setMessage(authorizationError.errorMessage)
	                    .show();
	        }

	        @Override
	        public void onReceiveNewToken(VKAccessToken newToken) {
	        	Log.i("came here working", newToken.toString()+"");
	        	getVKuserProfile();
	        }

	        @Override
	        public void onAcceptUserToken(VKAccessToken token) {
	        	Log.i("came here working", token.toString()+"");
	        	getVKuserProfile();
	        }
	    };
	    
	    
	    private void getVKuserProfile()
	    {
                  VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                  "id,first_name,last_name,sex,bdate,city,country,photo_50,photo_100," +
                  "photo_200_orig,photo_200,photo_400_orig,photo_max,photo_max_orig,online," +
                  "online_mobile,lists,domain,has_mobile,contacts,connections,site,education," +
                  "universities,schools,can_post,can_see_all_posts,can_see_audio,can_write_private_message," +
                  "status,last_seen,common_count,relation,relatives,counters"));
	    
	       request.executeWithListener(new VKRequestListener() {
	        @Override
	        public void onComplete(VKResponse response) {
	            //Do complete stuff
	        	 VKApiUser user = ((VKList<VKApiUser>)response.parsedModel).get(0);
	    	        Log.i("User name", user.first_name + " " + user.last_name);
	    	        
	    	        edtName.setText( user.first_name + " " + user.last_name);
	    	        
	    	        
	    	        url=user.photo_200;
                	 imageLoader.displayImage(url, ivProfileImage, options);
	    	        Log.i("Photo name", user.first_name + " " + user.photo_200);
	    	        
	    	        UpdateProfile updateTask= new UpdateProfile();
	    			updateTask.execute(url);
	        }
	        @Override
	        public void onError(VKError error) {
	            //Do error stuff
	        }
	        @Override
	        public void onProgress(VKRequest.VKProgressType progressType,
	                                         long bytesLoaded,
	                                         long bytesTotal)
	        {
	            //I don't really believe in progress
	        }
	        @Override
	        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
	            //More luck next time
	        }
	    });
	    }
	 
	 private void setUi(View view)
	 {
		  tvProfilepage= (TextView)view.findViewById(R.id.tvprofilePage);
		  tvMobile= (TextView)view.findViewById(R.id.tvMo);
		  tvMobileNumber= (TextView)view.findViewById(R.id.tvMobileNumber);
		  tvfacebook= (TextView)view.findViewById(R.id.tvFacebook);
		  tvVkontakte= (TextView)view.findViewById(R.id.tvVkontakte);
		  tvDeleteAcc= (TextView)view.findViewById(R.id.tvDeleteAccount);
		  tvSave= (TextView)view.findViewById(R.id.tvSave);
		  tvEditImage= (TextView)view.findViewById(R.id.tvEditImage);
		  prgImage= (ProgressBar)view.findViewById(R.id.prgProfilePic);
		  prgProfile= (ProgressBar)view.findViewById(R.id.prgProfile);
		  
		  edtName= (EditText)view.findViewById(R.id.edtName);
		  edtStatus= (EditText)view.findViewById(R.id.edtStatus);
		  spnCountry= (Spinner)view.findViewById(R.id.spCountry);
		  ivProfileImage= (ImageView)view.findViewById(R.id.ivProfile);
		  
		  tvProfilepage.setOnClickListener(this);
		  tvfacebook.setOnClickListener(this);
		  tvVkontakte.setOnClickListener(this);
		  tvDeleteAcc.setOnClickListener(this);
		  tvSave.setOnClickListener(this);
		  tvEditImage.setOnClickListener(this);
		  ivProfileImage.setOnClickListener(this);
		  ivOpenMenu.setOnClickListener(this);
		  tvHeader.setText(R.string.my_profile);
		  ivInviteUser.setVisibility(View.GONE);
	 }
	 
	 private String ReadFromfile(String fileName) {
		    StringBuilder ReturnString = new StringBuilder();
		    InputStream fIn = null;
		    InputStreamReader isr = null;
		    BufferedReader input = null;
		    try {
		        fIn = getResources().getAssets()
		                .open(fileName, Context.MODE_PRIVATE);
		        isr = new InputStreamReader(fIn);
		        input = new BufferedReader(isr);
		        String line = "";
		        while ((line = input.readLine()) != null) {
		            ReturnString.append(line);
		        }
		    } catch (Exception e) {
		        e.getMessage();
		    } finally {
		        try {
		            if (isr != null)
		                isr.close();
		            if (fIn != null)
		                fIn.close();
		            if (input != null)
		                input.close();
		        } catch (Exception e2) {
		            e2.getMessage();
		        }
		    }
		    return ReturnString.toString();
		}
	 
	 
		private void parseCountries()
		{
			arrCountries= new ArrayList<String>();
			String json=ReadFromfile("countries_list.json");
			try 
			{
				JSONArray array= new JSONArray(json);
				
				for(int i=0; i<array.length(); i++)
				{
					JSONObject country= array.getJSONObject(i);
					arrCountries.add(country.getString("name"));//)put(, country.getString("dial_code"));
				}
				// set array of country
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spin_profile, arrCountries);
				spnCountry.setAdapter(adapter);
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}

	 
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	 {
		super.onActivityResult(requestCode, resultCode, data);
		VKUIHelper.onActivityResult(requestCode, resultCode, data);
		
		try
		{
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
		catch(NullPointerException ne)
		{
			
		}
		
		
		
		if(resultCode== Activity.RESULT_OK  && requestCode== Gallary_REQUEST)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = this.getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			final String filePath = cursor.getString(columnIndex);
			cursor.close();
			
			imageLoader.displayImage("file://"+filePath, ivProfileImage, options);
			UpdateProfilePic updateTask= new UpdateProfilePic();
			updateTask.execute(filePath);
		}
	}
	 
	 private void startFacebbokAuth()
	 {
		 Session.openActiveSession(this, true, new Session.StatusCallback() 
		 {
			    // callback when session changes state
			    @Override
			    public void call(Session session, SessionState state, Exception exception) 
			    {
			    	
			    	if (session.isOpened())
			    	{
			    		makeMeRequest(session);
			    	}
			    }
			  });
	 }
	 
	 private void makeMeRequest(final Session session) {
		    // Make an API call to get user data and define a 
		    // new callback to handle the response.
		    Request request = Request.newMeRequest(session, 
		            new Request.GraphUserCallback() {
		        @Override
		        public void onCompleted(GraphUser user, Response response) {
		            // If the response is successful
		            if (session == Session.getActiveSession()) {
		                if (user != null) {
		                    // Set the id for the ProfilePictureView
		                    // view that in turn displays the profile picture.
		                	Log.i("facebook response", user.getInnerJSONObject().toString()+"");
		                	
		                 	edtName.setText(user.getName());
		                	url= "http://graph.facebook.com/"+user.getId()+"/picture?type=large";
		                	
		                	 imageLoader.displayImage(url, ivProfileImage, options);
		                	 
		                	   UpdateProfile updateTask= new UpdateProfile();
		   	    			   updateTask.execute(url);
		                }
		            }
		            if (response.getError() != null) {
		                // Handle errors, will do so later.
		            }
		        }
		    });
		    request.executeAsync();
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
	 private void setFont()
	 {
		 
		 Typeface bold= FontUtilitirs.calibriBold(this);
		 Typeface regular= FontUtilitirs.calibriBlack(this);
		    tvProfilepage.setTypeface(regular);
			tvMobile.setTypeface(regular);
			tvMobileNumber.setTypeface(bold);
			tvfacebook.setTypeface(bold);
			tvVkontakte.setTypeface(bold);
			tvDeleteAcc.setTypeface(bold);
			tvSave.setTypeface(bold);
			edtName.setTypeface(regular);
			edtStatus.setTypeface(regular);
			tvEditImage.setTypeface(regular);
	 }

	 
	 private void setProfileValues()
	 {
		 String userObject= myUserPrefs.getString(Constants.USER_DATA, "");
		 if(userObject!= null && userObject.length()>0)
		 {
			 JSONObject object;
			try
			{
				object = new JSONObject(userObject);
				
				JSONObject phone= object.getJSONObject("phone");
				 tvMobileNumber.setText(phone.getString("fullnumber")+"");
				 edtName.setText(object.getString("username")+"");
				 edtStatus.setText(object.getString("status")+"");
				 imageLoader.displayImage(myUserPrefs.getString(Constants.PROFILEIMAGE, ""), ivProfileImage, options);
				 
				 String reg=object.getString("region")+"";
				 
				 if(arrCountries.contains(reg))
				 {
					 spnCountry.setSelection(arrCountries.indexOf(reg));
				 }
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		 }
	 }
	@Override
	public void onClick(View v) 
	{
		if(v== tvProfilepage)
		{
			Bundle b= new Bundle();
			b.putString("uid", myUserPrefs.getString(Constants.USERID, ""));
			b.putBoolean("isMe", true);
			b.putBoolean("isReq", false);
			Intent in= new Intent(this, ProfilePageActivity.class);
			in.putExtra("bundle", b);
			startActivity(in);
		}
		else if(v== tvfacebook)
		{
			//
			showSocialLinking(true);
		}
		else if(v== ivOpenMenu)
		{
			//
			toggleDrawer();
			
		}
		else if(v== tvVkontakte)
		{
		
			showSocialLinking(false);
		}
		else if(v== tvDeleteAcc)
		{
			//
			showDeleteDialog();
		}
		else if(v== tvSave)
		{
			//
			UpdateProfile updateTask= new UpdateProfile();
			updateTask.execute(url);
		}
		else if(v== ivProfileImage || v== tvEditImage)
		{
			//
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, Gallary_REQUEST);
		}
	}
	
	
	private void showDeleteDialog()
	{
		AlertDialog.Builder alert= new AlertDialog.Builder(this);
		
		alert.setTitle("warning");
		alert.setMessage("Are you sure to delete this account?");
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
			}
		});
		
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				DeleteProfile deleteTask= new DeleteProfile();
				deleteTask.execute("");
				dialog.dismiss();
				
			}
		});
		
		alert.create().show();
	}
	
	
	/**
	 * Async task to GetCinyance list  
	 * @author kamal
	 *
	 */
	private class UpdateProfilePic extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			 String path= aurl[0];
			 String uid= myUserPrefs.getString(Constants.USERID, "");
			 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
			 
			String result =bengine.updateProfilePic(uid, auth, path);
			Log.i("result", result);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgImage.setVisibility(View.VISIBLE);
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
					String url=  object.getString("profile_image_url");
					myUserEdit.putString(Constants.PROFILEIMAGE, url);
					myUserEdit.commit();
		    	}
		    	else
		    	{
		    		
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
	    	 prgImage.setVisibility(View.GONE);
	     }
	 }
	
	private class UpdateProfile extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			 
			 String url= aurl[0];
			 
			 User user= new User();
			 
			 user.userName= edtName.getText().toString()+"";
			 user.status= edtStatus.getText().toString()+"";
			 user.region= spnCountry.getSelectedItem()+"";
			 if(url== null || url.length()==0 )
				 user.profile_image_url= myUserPrefs.getString(Constants.PROFILEIMAGE, "");
			 else
				 user.profile_image_url= url;
			 String uid= myUserPrefs.getString(Constants.USERID, "");
			 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
			 
			String result =bengine.updateUserProfile(uid, auth, user);
			Log.i("result", result);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgProfile.setVisibility(View.VISIBLE);
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
				if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
		    	{
		    		//Success
		    	}
		    	else
		    	{
		    		
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
			prgProfile.setVisibility(View.GONE);
	     }
	 }
	
	private class GetProfile extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			 String uid= myUserPrefs.getString(Constants.USERID, "");
			 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
			 
			String result =bengine.getUserProfile(uid, auth);
			Log.i("result", result);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgProfile.setVisibility(View.VISIBLE);
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
				if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
		    	{
		    		//Success
					parseAndSaveResult(object);
					setProfileValues();
		    	}
		    	else
		    	{
		    		
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
			
			
			prgProfile.setVisibility(View.GONE);
	     }
	 }
	
	private void parseAndSaveResult(JSONObject result)
	{

		try {
			JSONObject userOngect= result.getJSONObject("user");
			String uid= userOngect.getString("userid");
			String auth_token= userOngect.getString("auth_token");
			String profile_image= userOngect.getString("profile_image_url");
			
			myUserEdit.putString(Constants.USERID, uid);
			myUserEdit.putString(Constants.PROFILEIMAGE, profile_image);
			myUserEdit.putString(Constants.AUTH_TOKEN, auth_token);
			myUserEdit.putString(Constants.USER_DATA, userOngect.toString());
			myUserEdit.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	private class DeleteProfile extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			 String uid= myUserPrefs.getString(Constants.USERID, "");
			 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
			 
			String result =bengine.deleteUserProfile(uid, auth);
			Log.i("result", result);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgProfile.setVisibility(View.VISIBLE);
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
				if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
		    	{
		    		//Success
					myUserEdit.clear();
					myUserEdit.commit();
					Intent in= new Intent(MyProfileActivity.this, RegistrationActivity.class);
					in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(in);
		    	}
		    	else
		    	{
		    		
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
			prgProfile.setVisibility(View.GONE);
	     }
	 }
	
	 private void showSocialLinking(final boolean isFb)
	 {
			AlertDialog.Builder builder= new  AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.my_profile));
			
			if(isFb)
				builder.setMessage(R.string.linktofb);
			else
				builder.setMessage(R.string.linktovk);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				
					if(isFb)
					{
						url="";
					 startFacebbokAuth();
					}
					else
					{
						url="";
						VKSdk.authorize(sMyScope, true, false);
					}
				}
			});
			
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
			
			builder.create().show();
	 }
	
}
