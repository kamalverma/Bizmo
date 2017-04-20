package com.logicnext.bizmo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class RegistrationActivity extends Activity implements OnClickListener
{
	TextView tvVerifyPhoneNumber, tvHeader, tvRegistration;
	
	BizmoEngine bEngine;
	
	RelativeLayout rlMain;
	
	Spinner spnCountry;
	EditText edtPhoneNumber,edtCountryCode;
	
	ProgressBar prgRegistration;
	LinkedHashMap<String, String> hashCountries= new LinkedHashMap<String, String>();
	ArrayList<String> arrCountries= new ArrayList<String>();
	
	 private SharedPreferences myUserPrefs;
     private SharedPreferences.Editor myUserEdit;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	              WindowManager.LayoutParams.FLAG_FULLSCREEN);
	      
	      setContentView(R.layout.registration);
	      
	      bEngine= BizmoEngine.getInstance();
	      myUserPrefs = this.getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
	    
	      rlMain= (RelativeLayout)findViewById(R.id.rlMain);
	      tvVerifyPhoneNumber= (TextView)findViewById(R.id.tvVerifyPhoneNumber);
	      tvHeader= (TextView)findViewById(R.id.tvHeader);
	      tvRegistration= (TextView)findViewById(R.id.tvRegistration);
	      edtPhoneNumber= (EditText)findViewById(R.id.edtPhoneNumber);
	      edtCountryCode= (EditText)findViewById(R.id.edtCountrycode);
	      spnCountry= (Spinner)findViewById(R.id.spnCountry);
	      prgRegistration= (ProgressBar)findViewById(R.id.prgRegistration);
	      
	      parseCountries();
	      setFont();
	      
	      spnCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
	      {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				try {
					JSONObject jobject= new JSONObject(hashCountries.get(arrCountries.get(arg2)));
					edtCountryCode.setText(jobject.getString("country_code")+"");
					myUserEdit.putString(Constants.MYINTERNATION, jobject.getString("international_prefix"));
					myUserEdit.putString(Constants.MYNATIONAL, jobject.getString("national_prefix"));
					myUserEdit.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			//	
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	      tvVerifyPhoneNumber.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v== tvVerifyPhoneNumber)
		{
			if(edtPhoneNumber.getText().toString().trim().length()<=0)
			{
				edtPhoneNumber.setError(getResources().getString(R.string.err_empty_phone));
			}
			else
			{
				showConfirmMobile();
			}
			
		}
		else if(v== tvOk)
		{
			if(confirmPopup!= null && confirmPopup.isShowing())
				confirmPopup.dismiss();
				RegisterUser userTask= new RegisterUser();
				userTask.execute("");
		}
		else if(v== tvCancel)
		{
			if(confirmPopup!= null && confirmPopup.isShowing())
				confirmPopup.dismiss();
		}
		
	}
	
	
	private void setFont()
	{
		tvHeader.setTypeface(FontUtilitirs.calibriBold(this));
		edtCountryCode.setTypeface(FontUtilitirs.calibriBlack(this));
		edtPhoneNumber.setTypeface(FontUtilitirs.calibriBlack(this));
		tvRegistration.setTypeface(FontUtilitirs.calibriBlack(this));
		tvVerifyPhoneNumber.setTypeface(FontUtilitirs.calibriBold(this));
	}
	
	/**
	 * Read the file from string & retrun 
	 * in String from.
	 * @param fileName
	 * @return content of file
	 */
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
	
	
	/**
	 * Parse list of countries from assets
	 * & from the hash map for spinner adapter
	 */
	private void parseCountries()
	{
		Thread th= new Thread(new Runnable() {
			
			@Override
			public void run() 
			{
				String json=ReadFromfile("country_details.json");
				try 
				{
					JSONArray array= new JSONArray(json);
					
					for(int i=0; i<array.length(); i++)
					{
						JSONObject country= array.getJSONObject(i);
						hashCountries.put(country.getString("country_name"), country.toString());
					}
					// set array of country
					setCountryArray();
					runOnUiThread(new Runnable()
					{
						@Override
						public void run() {
							ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegistrationActivity.this, R.layout.spin, arrCountries);
							spnCountry.setAdapter(adapter);
						}
					});
				}
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
			}
		});
		th.start();
	}
	
	private void setCountryArray()
	{
		arrCountries= new ArrayList<String>();
		for(String keys : hashCountries.keySet()){
			arrCountries.add(keys);
		}
	}

	
	/**
	 * Async task to GetCinyance list  
	 * @author kamal
	 *
	 */
	private class RegisterUser extends AsyncTask<String, Integer, String> 
	{
		 @Override
	     protected String doInBackground(String... aurl) 
	     {
			String result =bEngine.registerUSer(spnCountry.getSelectedItem().toString(), edtCountryCode.getText().toString().trim().replaceAll(" ", ""), edtPhoneNumber.getText().toString().trim().replaceAll(" ", ""));
			Log.i("result", result);
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
				if(object.getString("errorcode").equals(HttpStatus.SUCCESS) ||object.getString("errorcode").equals(HttpStatus.USER_ALREADY_REGISTERED))
		    	{
		    		//Success
					String acode= object.getString("activation_code");
					showAlert(acode);
		    	}
		    	else
		    	{
		    		
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
				Intent in= new Intent(RegistrationActivity.this, VarificationCodeActivity.class);
				in.putExtra("phone", edtPhoneNumber.getText().toString());
				in.putExtra("country", spnCountry.getSelectedItem().toString());
				in.putExtra("ccode", edtCountryCode.getText().toString());
				startActivity(in);
			}
		});
		
		builder.create().show();
		
	}
	
	private void showConfirmMobile()
	{
		if(confirmPopup!= null && confirmPopup.isShowing())
			return;
		View v= new View(this);
		v= getLayoutInflater().inflate(R.layout.confirm_mobile_number, null);
		
		tvCancel= (TextView)v.findViewById(R.id.tvCancel);
		tvOk= (TextView)v.findViewById(R.id.tvOk);
		edtConfirmMobile= (TextView)v.findViewById(R.id.tvConfirmMobile);
		tvConfirmText= (TextView)v.findViewById(R.id.tvChangeMessage);
		
		tvCancel.setOnClickListener(this);
		tvOk.setOnClickListener(this);
		confirmPopup= new PopupWindow(v,  android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, false);
		confirmPopup.setBackgroundDrawable(new BitmapDrawable());
		confirmPopup.setOutsideTouchable(true);
		
		edtConfirmMobile.setText(edtCountryCode.getText().toString()+ edtPhoneNumber.getText().toString());
		
		//Set Font Type
		tvCancel.setTypeface(FontUtilitirs.calibriBold(this));
		tvOk.setTypeface(FontUtilitirs.calibriBold(this));
		edtConfirmMobile.setTypeface(FontUtilitirs.calibriBold(this));
		tvConfirmText.setTypeface(FontUtilitirs.calibriBlack(this));
		
		confirmPopup.showAtLocation(rlMain, Gravity.CENTER, 0, 0);
	}
	
	PopupWindow confirmPopup;
	TextView tvOk, tvCancel, tvConfirmText;
	TextView edtConfirmMobile;
	
	
	
	
	
	
	
	
	
	
	
	
	
}