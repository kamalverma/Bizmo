package com.logicnext.bizmo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class ProfilePageActivity extends MainActivity implements OnClickListener
{

	TextView  tvFreeCall, tvFreeMessage, tvStatusLabel, tvStatus, tvLocationLabel, tvLacation, tvMobileLabel, tvMobile;
	TextView tvSeeBusineeePage;
	
	ImageView ivSendRequest, ivProfile, ivSep;
	ProgressBar prg;
	String uid, Uname, Call;
	boolean isMe, isRequestRequired;
	
	BizmoEngine bEngine;
	
	  protected ImageLoader imageLoader = ImageLoader.getInstance();
	 	DisplayImageOptions options;
	 	private void setImageLoader()
	 	{
	 		options = new DisplayImageOptions.Builder()
	         .cacheInMemory(true) // default
	         .cacheOnDisc(true) // default
	         .showImageForEmptyUri(R.drawable.user_profile_default_image)
	         .showImageOnFail(R.drawable.user_profile_default_image)
	         .showImageOnLoading(R.drawable.user_profile_default_image)
	         .bitmapConfig(Bitmap.Config.ARGB_8888) // default
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
		  	View ProfileView;
		  	@Override
		  	protected void onCreate(Bundle savedInstanceState) {
		  		super.onCreate(savedInstanceState);
		  		
		  		inflator= getLayoutInflater();
		  		
		  		ProfileView = inflator.inflate(R.layout.profile_page, null);

		  		rlMain.addView(ProfileView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		  	
		  	    bEngine= BizmoEngine.getInstance();
			    Bundle bundle = getIntent().getExtras().getBundle("bundle");
			    uid = bundle.getString("uid");
			    isMe= bundle.getBoolean("isMe");
			    isRequestRequired= bundle.getBoolean("isReq");
			    
			    setImageLoader();
			    
			    setUi(ProfileView);
			    setFont();
			    GetProfile gtask= new GetProfile();
			    gtask.execute("");
			    
			 
		  	}
	 private void setUi(View view)
	 {
		 tvFreeCall= (TextView)view.findViewById(R.id.tvFreeCall);
		 tvFreeMessage= (TextView)view.findViewById(R.id.tvFreemessage);
		  tvStatus= (TextView)view.findViewById(R.id.tvStatus);
		  tvStatusLabel= (TextView)view.findViewById(R.id.lableStatus);
		  tvLocationLabel= (TextView)view.findViewById(R.id.lableLocation);
		  tvLacation= (TextView)view.findViewById(R.id.tvLocation);
		  tvMobile= (TextView)view.findViewById(R.id.tvMobile);
		  tvMobileLabel= (TextView)view.findViewById(R.id.lableMobile);
		  tvSeeBusineeePage= (TextView)view.findViewById(R.id.tvSeeBusinessPage);
		  tvHeader= (TextView)this.findViewById(R.id.tvHeader);
		  prg= (ProgressBar)view.findViewById(R.id.prgProfile);
		 ivProfile= (ImageView)view.findViewById(R.id.ivProfilePic);
		 ivSep= (ImageView)view.findViewById(R.id.sep);
		 ivSendRequest= (ImageView)view.findViewById(R.id.ivSendRequest);
		 ivInviteUser.setVisibility(View.GONE);
		 
		 ivOpenMenu.setOnClickListener(this);
		 if(isMe)
		 {
			 tvFreeCall.setVisibility(View.GONE);
			 tvFreeMessage.setVisibility(View.GONE);
			 ivSendRequest.setVisibility(View.GONE);
			 ivSep.setVisibility(View.GONE);
		 }
		 else
		 {
		   if(isRequestRequired)
		    {
		    	ivSendRequest.setVisibility(View.VISIBLE);
		    	tvFreeCall.setVisibility(View.GONE);
				 tvFreeMessage.setVisibility(View.GONE);
		    }
		    else
		    {
		    	ivSendRequest.setVisibility(View.INVISIBLE);
		    	tvFreeCall.setVisibility(View.VISIBLE);
				 tvFreeMessage.setVisibility(View.VISIBLE);
		    }
		 }
		 tvFreeCall.setOnClickListener(this);
		 tvFreeMessage.setOnClickListener(this);
		 ivSendRequest.setOnClickListener(this);
	 }
	 private void setFont()
	 {
		    tvFreeCall.setTypeface(FontUtilitirs.calibriBlack(this));
		    tvHeader.setTypeface(FontUtilitirs.calibriBlack(this));
			tvFreeMessage.setTypeface(FontUtilitirs.calibriBlack(this));
			tvHeader.setTypeface(FontUtilitirs.calibriBold(this));
			tvStatusLabel.setTypeface(FontUtilitirs.calibriBold(this));
			tvStatus.setTypeface(FontUtilitirs.calibriBlack(this));
			tvLocationLabel.setTypeface(FontUtilitirs.calibriBold(this));
			tvLacation.setTypeface(FontUtilitirs.calibriBlack(this));
			tvMobile.setTypeface(FontUtilitirs.calibriBlack(this));
			tvMobileLabel.setTypeface(FontUtilitirs.calibriBold(this));
			tvSeeBusineeePage.setTypeface(FontUtilitirs.calibriBlack(this));
	 }

		private class GetProfile extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				String result =bEngine.getUserProfile(uid, auth);
				Log.i("result", result);
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
					if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
			    	{
			    		//Success
						setProfileValues(object.getJSONObject("user").toString());
			    	}
			    	else
			    	{
			    		
			    	}
				} 
				catch (JSONException e) {
					e.printStackTrace();
					//Faliure
				}
		    	 prg.setVisibility(View.GONE);
		     }
		 }
		
		private class Sendrequest extends AsyncTask<String, Integer, String> 
		{
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				 String myuid= myUserPrefs.getString(Constants.USERID, "");
				String result =bEngine.sendRequest(myuid, uid, auth);
				Log.i("result", result);
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
					if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
			    	{
			    		//Success
						Toast.makeText(ProfilePageActivity.this, "Request sent", Toast.LENGTH_LONG).show();
			    	}
			    	else
			    	{
			    		
			    	}
				} 
				catch (JSONException e) {
					e.printStackTrace();
					//Faliure
				}
		    	 prg.setVisibility(View.GONE);
		     }
		 }
		
		 private void setProfileValues(String userObject)
		 {
			 if(userObject!= null && userObject.length()>0)
			 {
				 JSONObject object;
				try
				{
					object = new JSONObject(userObject);
					JSONObject phone= object.getJSONObject("phone");
					
					 String uname=object.getString("username")+"";
					 String phString=phone.getString("fullnumber")+"";
					 tvMobile.setText(phString);
					
					 if(uname==null || uname.length()<=0)
					 {
						 tvHeader.setText(phString);
						 uname= phString;
					 }
					 else
					 {
						 tvHeader.setText(uname);
						 Uname= uname;
					 }
					 
					 tvStatus.setText(object.getString("status")+"");
					 imageLoader.displayImage(object.getString("profile_image_url"), ivProfile, options);
					 tvLacation.setText(object.getString("region")+"");
				} 
				catch (JSONException e) {
					e.printStackTrace();
				}
			 }
		 }
		 
		 private void showMobielAlert()
		 {
				AlertDialog.Builder builder= new  AlertDialog.Builder(this);
				builder.setTitle("Warning");
				
				builder.setMessage(R.string.wifi_warning);
				
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						Intent in= new Intent(ProfilePageActivity.this, ChatActivity.class);
						in.putExtra("uid", uid);
						in.putExtra("uname", Uname);
				       startActivity(in);
					}
				});
				
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				builder.create().show();
				
		}
		 
		 
	private void getNetworkState()
	{
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		State mobile = conMan.getNetworkInfo(0).getState();
		//wifi
		State wifi = conMan.getNetworkInfo(1).getState();
		
		if (wifi == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
			
			showMobielAlert();
		    //mobile
		} else if (mobile == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
		    //wifi
		}
		else
		{
			
		}
	}
	 
	@Override
	public void onClick(View v) {
		
		if(v== ivOpenMenu)
		{
			toggleDrawer();
		}
		else if(v== ivSendRequest)
		{
			Sendrequest send= new Sendrequest();
			send.execute("");
		}
		else if(v== tvFreeMessage)
		{
			getNetworkState();
		}
		
		else if(v== tvFreeCall)
		{
			if(tvMobile.getText().toString().length()>0)
			{
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tvMobile.getText().toString()));
				startActivity(intent);
			}
				
			
		}
	}
}
