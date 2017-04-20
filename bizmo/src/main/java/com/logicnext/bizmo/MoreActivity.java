package com.logicnext.bizmo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class MoreActivity extends MainActivity implements OnClickListener
{

	TextView tvmyProfile, tvGoPremium, tvMyvisitors, tvVisitorNumber, tvGiftShop, tvGiftNumber, tvmain;
	TextView tvmyBusinessPage, tvCreateNewPage, tvWatchList, tvSettings, tvNotification, tvPrivacy,tvAbout, 
	                tvRules, tvReport, tvTellFriend, tvAddCompany; ;
	TextView tvLogout;
     BizmoEngine bEngine;
     
     LayoutInflater inflator;
  	View connectionView;
  	@Override
  	protected void onCreate(Bundle savedInstanceState) {
  		super.onCreate(savedInstanceState);
  		
  		inflator= getLayoutInflater();
  		connectionView = inflator.inflate(R.layout.more, null);
  		rlMain.addView(connectionView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  		bEngine= BizmoEngine.getInstance();
  		setUi(connectionView);
  	}
	
	 
	 
	 private void setUi(View view)
	 {
	        tvmyProfile= (TextView)view.findViewById(R.id.tvMyProfile);
	        tvVisitorNumber= (TextView)view.findViewById(R.id.tvMyVisitorNumber);
	        tvMyvisitors= (TextView)view.findViewById(R.id.tvMyVisitor);
	        tvGiftShop= (TextView)view.findViewById(R.id.tvGiftShop);
	        tvGiftNumber= (TextView)view.findViewById(R.id.tvGiftShopNumber);
	        tvGoPremium= (TextView)view.findViewById(R.id.tvGoPremium);
	        tvCreateNewPage= (TextView)view.findViewById(R.id.tvCreateNewPage);
	        tvmyBusinessPage= (TextView)view.findViewById(R.id.tvMyBusinessList);
	        tvmain= (TextView)view.findViewById(R.id.tvMain);
	        tvWatchList= (TextView)view.findViewById(R.id.tvWatchList);
	        tvAddCompany= (TextView)view.findViewById(R.id.tvCreateWatchList);
	        tvSettings= (TextView)view.findViewById(R.id.tvSettings);
	        tvNotification= (TextView)view.findViewById(R.id.tvNotification);
	        
	        tvPrivacy= (TextView)view.findViewById(R.id.tvPrivacy);
	        tvAbout= (TextView)view.findViewById(R.id.tvAbout);
	        tvRules= (TextView)view.findViewById(R.id.tvRules);
	        tvReport= (TextView)view.findViewById(R.id.tvReport);
	        tvTellFriend= (TextView)view.findViewById(R.id.tvtellFriend);
		    tvLogout= (TextView)view.findViewById(R.id.tvLogout);
		    
		    tvmyProfile.setOnClickListener(this);
		    tvLogout.setOnClickListener(this);
		    tvHeader.setText(R.string.more);
		    
		setFont();
	 }
	 
	 
	 private void setFont()
	 {
		 Typeface calBold= FontUtilitirs.calibriBold(this);
		 Typeface calibary= FontUtilitirs.calibriBlack(this);
		 tvmyProfile.setTypeface(calibary);
		 tvVisitorNumber.setTypeface(calibary);
		 tvGiftShop.setTypeface(calibary);
		 tvmain.setTypeface(calBold);
		 tvMyvisitors.setTypeface(calibary);
		 tvGoPremium.setTypeface(calibary);
		 tvmyBusinessPage.setTypeface(calBold);
		 tvCreateNewPage.setTypeface(calibary);
		 tvWatchList.setTypeface(calBold);
		 tvAddCompany.setTypeface(calibary);
		 tvSettings.setTypeface(calBold);
		 tvNotification.setTypeface(calibary);
		 tvPrivacy.setTypeface(calibary);
		 tvAbout.setTypeface(calibary);
		 tvRules.setTypeface(calibary);
		 tvReport.setTypeface(calibary);
		 tvTellFriend.setTypeface(calibary);
		 tvLogout.setTypeface(calibary);
		 ivOpenMenu.setOnClickListener(this);
		 ivInviteUser.setVisibility(View.GONE);
	 }
	 
	 
	 @Override
		public void onClick(View v) {
			if(v== tvmyProfile)
			{
				Intent intent= new Intent(this, MyProfileActivity.class);
				startActivity(intent);
			}
			else if(v== ivOpenMenu)
			{
				toggleDrawer();
			}
			else if(v== tvLogout)
			{
				showLogOutAlert();
			}
		}
	 
	 private class Logout extends AsyncTask<String, Integer, String> 
	 {
			 @Override
		     protected String doInBackground(String... aurl) 
		     {
				 String uid= myUserPrefs.getString(Constants.USERID, "");
				 String auth= myUserPrefs.getString(Constants.AUTH_TOKEN, "");
				 String result = bEngine.logOut(uid, auth);
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
		    	JSONObject object;
				try
				{
					object = new JSONObject(result);
					if(object.getString("errorcode").equals(HttpStatus.SUCCESS))
			    	{
			    		//Success
						myUserEdit.clear();
						myUserEdit.commit();
						chatEngine.logout();
						Intent in= new Intent(MoreActivity.this, RegistrationActivity.class);
						in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(in);
			    	}
			    	else
			    	{
			    		Toast.makeText(MoreActivity.this, "Logout failed", Toast.LENGTH_LONG).show();
			    	}
				} 
				catch (JSONException e) {
					e.printStackTrace();
				}
				//Send to main Screen
		     }
		 }
	 
	 private void showLogOutAlert()
	 {
			AlertDialog.Builder builder= new  AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.logout));
			
			builder.setMessage(R.string.logout_message);
			
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
					Logout logout= new Logout();
					logout.execute("");
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
