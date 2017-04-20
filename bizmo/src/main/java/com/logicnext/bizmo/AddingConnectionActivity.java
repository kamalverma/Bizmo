package com.logicnext.bizmo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bizmo.communication.HttpStatus;
import com.bizmo.engine.BizmoEngine;
import com.ui.common.Constants;
import com.ui.common.FontUtilitirs;

public class AddingConnectionActivity extends Activity implements OnClickListener
{
	 private SharedPreferences myUserPrefs;
     private SharedPreferences.Editor myUserEdit;

	
     String countryCode;
	ProgressBar prgAddContects;
	ToggleButton toggleAutoAddConnection, toggleAllowotherstoAdd;
	TextView tvHeader, tvAutoAdd, tvAutoAddText, tvByUsing, tvAllowOther, tvAllowOtherText, tvYouWill, tvNext;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	      
	      setContentView(R.layout.adding_contacts);
	      
	      myUserPrefs = getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
	      tvHeader= (TextView)findViewById(R.id.tvHeader);
	      tvAutoAddText= (TextView)findViewById(R.id.auto_add_text);
	      tvAutoAdd= (TextView)findViewById(R.id.tvAddConnection);
	      tvByUsing= (TextView)findViewById(R.id.byUsing);
	      tvAllowOther= (TextView)findViewById(R.id.allowOther);
	      tvAllowOtherText= (TextView)findViewById(R.id.allowOthersText);
	      tvYouWill= (TextView)findViewById(R.id.youWill);
	      tvNext= (TextView)findViewById(R.id.tvNext);
	      prgAddContects= (ProgressBar)findViewById(R.id.prgAddConnection);
	      toggleAllowotherstoAdd= (ToggleButton)findViewById(R.id.toggleAllowOtherstoAdd);
	      toggleAutoAddConnection= (ToggleButton)findViewById(R.id.toggleAutoAddConnection);
	      tvNext.setOnClickListener(this);
	   
	      setFont();
	   
	      JSONObject object;
		try {
			object = new JSONObject(myUserPrefs.getString(Constants.USER_DATA, ""));
			JSONObject phone= object.getJSONObject("phone");
			countryCode= (phone.getString("code")+"");
		} catch (JSONException e) {
			e.printStackTrace();
			countryCode="";
		}
	      
	      myUserPrefs = this.getSharedPreferences("bizmo", Context.MODE_PRIVATE);
		  myUserEdit =  myUserPrefs.edit();
		  
		  if(myUserPrefs.getBoolean(Constants.AUTO_ADD_CONNECTION, true))
			{
				toggleAutoAddConnection.setChecked(true);
			}
			else
			{
				toggleAutoAddConnection.setChecked(false);
			}
		  if(myUserPrefs.getBoolean(Constants.ALLOW_OTHERS_TO_ADD, true))
			{
				toggleAllowotherstoAdd.setChecked(true);
			}
			else
			{
				toggleAllowotherstoAdd.setChecked(false);
			}
		  
		  toggleAllowotherstoAdd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				myUserEdit.putBoolean(Constants.ALLOW_OTHERS_TO_ADD, isChecked);
				myUserEdit.commit();
			}
		});
		  toggleAutoAddConnection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
				{
					myUserEdit.putBoolean(Constants.AUTO_ADD_CONNECTION, isChecked);
					myUserEdit.commit();
				}
			});
	}
	
	private void setFont()
	{
		tvHeader.setTypeface(FontUtilitirs.calibriBold(this));
		tvAutoAddText.setTypeface(FontUtilitirs.calibriBlack(this));
		tvAutoAdd.setTypeface(FontUtilitirs.calibriBlack(this));
		tvByUsing.setTypeface(FontUtilitirs.calibriBlack(this));
		tvAllowOther.setTypeface(FontUtilitirs.calibriBlack(this));
		tvAllowOtherText.setTypeface(FontUtilitirs.calibriBlack(this));
		tvYouWill.setTypeface(FontUtilitirs.calibriBlack(this));
		tvNext.setTypeface(FontUtilitirs.calibriBold(this));
	}

	@Override
	public void onClick(View v) {
		
		if(v== tvNext)
		{
			AddConnection adTask= new AddConnection();
			adTask.execute("");
		}
	}
	
	/**
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
			  String arrUsers= getPhoneBook();
			 String result = BizmoEngine.getInstance().addConnections(uid, auth, arrUsers, Autoadd, allowAll);
			 return result;
	     }

	     @Override
	    protected void onPreExecute() 
	    {
	    	super.onPreExecute();
	    	prgAddContects.setVisibility(View.VISIBLE);
	    	
	    	tvNext.setEnabled(false);
	    	toggleAllowotherstoAdd.setEnabled(false);
	    	toggleAutoAddConnection.setEnabled(false);
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
				
		    	}
		    	else
		    	{
		    		Toast.makeText(AddingConnectionActivity.this, "Adding connection failed", Toast.LENGTH_LONG).show();
		    	}
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
			//Send to main Screen
			tvNext.setEnabled(true);
	    	toggleAllowotherstoAdd.setEnabled(true);
	    	toggleAutoAddConnection.setEnabled(true);
			myUserEdit.putBoolean(Constants.REG_COMPLETE, true);
			myUserEdit.commit();
			Intent intent= new Intent(AddingConnectionActivity.this, ConnectionsActivity.class);
			
			startActivity(intent);
			finish();
			prgAddContects.setVisibility(View.GONE);
	     }
	 }
	
	
	
	/***Get Device Contects
	 * 
	 */
	private String getPhoneBook()
	{
		JSONArray arrConArray= new JSONArray();
		 ContentResolver cr = AddingConnectionActivity.this.getContentResolver();
	     Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null);
			if (cur.getCount() > 0) 
			{
				while (cur.moveToNext()) 
				{
				   String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				   
					Cursor emails = AddingConnectionActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
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
						Cursor phones = AddingConnectionActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + id, null, null);
						while (phones.moveToNext()) 
						{
							JSONObject objectJson= new JSONObject();
							
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
							phoneNumber=phoneNumber.replaceAll(" ", "");
							try {
								objectJson.put("phone", phoneNumber);
								objectJson.put("name", name);
								objectJson.put("email", emailAddress);
								arrConArray.put(objectJson);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						phones.close();
					}
				}
			 Log.i("the vector contacts Size is", arrConArray.length() + " abcd");
				cur.close();
			}
			
			return arrConArray.toString();
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


}