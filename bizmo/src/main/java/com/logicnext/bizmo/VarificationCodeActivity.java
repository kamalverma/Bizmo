package com.logicnext.bizmo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.databaseaccess.DatabaseHelper;
import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class VarificationCodeActivity extends Activity implements OnClickListener
{
	
	EditText edtCode;
	
	TextView tvNext, tvResend, tvHeader, tvHeaderText, tvDintRecieve;
	String phoneNumber, cCode, country;
	BizmoEngine bEngine;
	ProgressBar prgRegistration;
	
	
	 private SharedPreferences myUserPrefs;
		private SharedPreferences.Editor myUserEdit;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	      
	      setContentView(R.layout.verification);
	      
	      phoneNumber= getIntent().getExtras().getString("phone");
	      cCode= getIntent().getExtras().getString("ccode");
	      country= getIntent().getExtras().getString("country");
	      
	      
	      bEngine= BizmoEngine.getInstance();
	      tvNext= (TextView)findViewById(R.id.tvNext);
	      tvResend= (TextView)findViewById(R.id.tvResend);
	      tvHeader= (TextView)findViewById(R.id.tvHeader);
	      tvHeaderText= (TextView)findViewById(R.id.tvPleaseEnter);
	      tvDintRecieve= (TextView)findViewById(R.id.tvDintRecieved);
	      edtCode=  (EditText)findViewById(R.id.edtVerificationCode);
	      prgRegistration= (ProgressBar)findViewById(R.id.prgVerification);
	      tvNext.setOnClickListener(this);
	      tvResend.setOnClickListener(this);
	      
	      setFont();
	      
	      myUserPrefs = this.getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
	}
	
	
	private void setFont()
	{
		tvHeader.setTypeface(FontUtilitirs.calibriBold(this));
		edtCode.setTypeface(FontUtilitirs.calibriBold(this));
		tvNext.setTypeface(FontUtilitirs.calibriBold(this));
		tvResend.setTypeface(FontUtilitirs.calibriBold(this));
		tvDintRecieve.setTypeface(FontUtilitirs.calibriBlack(this));
		tvHeaderText.setTypeface(FontUtilitirs.calibriBold(this));
	}
	
	private void reSendCode()
	{
		ResendVarification resend= new ResendVarification();
		resend.execute("");
	}
	
	private void verifyCode()
	{
		VarifyCode verify= new VarifyCode();
		verify.execute("");
	}
	
	private void showErrorDialog(int error)
	{
		AlertDialog.Builder alerrt= new AlertDialog.Builder(this);
		
		alerrt.setMessage(error);
		
		//alerrt.setNeutralButton("OK", listener)
		
	}


	@Override
	public void onClick(View v) {
		
		if(v== tvNext)
		{
			if(edtCode.getText().toString().trim().length()<=0)
			{
				edtCode.setError(getResources().getString(R.string.err_empty_code));
			}
			else
			{
				verifyCode();
			}
		}
		else if(v== tvResend)
		{
			reSendCode();
		}
		
	}
	
	/**
	 * Async task to GetCinyance list  
	 * @author kamal
	 *
	 */
	private class ResendVarification extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			String result =bEngine.resendVerificatonCode(country, cCode,phoneNumber);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgRegistration.setVisibility(View.VISIBLE);
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
					Toast.makeText(VarificationCodeActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
					String acode= object.getString("activation_code");
					showAlert(acode);

		    	}
		    	else
		    	{
		    		Toast.makeText(VarificationCodeActivity.this, "Failed. Try again later", Toast.LENGTH_LONG).show();
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
	    	 prgRegistration.setVisibility(View.GONE);
	     }
	 }
	
	private void showAlert(String acode)
	{
		AlertDialog.Builder builder= new  AlertDialog.Builder(this);
		builder.setTitle("Activation Code");
		
		builder.setMessage(acode);
		
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
			}
		});
		
		builder.create().show();
		
	}
	
	/**
	 * Async task to GetCinyance list  
	 * @author kamal
	 *
	 */
	private class VarifyCode extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			String result =bEngine.sendVerification(cCode, phoneNumber, edtCode.getText().toString());
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgRegistration.setVisibility(View.VISIBLE);
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
					Toast.makeText(VarificationCodeActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
					parseAndSaveResult(object);
					
					 createDataBase();
					Intent intent= new Intent(VarificationCodeActivity.this, AddingConnectionActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					finish();
		    	}
				else if(object.getString("errorcode").equals(HttpStatus.INVALID_CODE))
		    	{
		    		//Fail
					showAlert("Invalid Activation Code");
		    	}
		    	else
		    	{
		    		Toast.makeText(VarificationCodeActivity.this, "Failed. Try again later", Toast.LENGTH_LONG).show();
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				//Faliure
			}
	    	 prgRegistration.setVisibility(View.GONE);
	     }
	 }
	
	
	private void parseAndSaveResult(JSONObject result)
	{
		try {
			JSONObject userOngect= result.getJSONObject("user");
			String uid= userOngect.getString("userid");
			String auth_token= userOngect.getString("auth_token");
			
			myUserEdit.putString(Constants.USERID, uid);
			myUserEdit.putString(Constants.AUTH_TOKEN, auth_token);
			myUserEdit.putString(Constants.USER_DATA, userOngect.toString());
			myUserEdit.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	private void createDataBase() 
    {
    	DatabaseHelper dbHelper = new DatabaseHelper(this,myUserPrefs.getString(Constants.USERID, ""));
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