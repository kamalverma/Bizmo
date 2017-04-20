package com.bizmo.communication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.bizmo.object.User;



/*
 * Copyright (c) 2013, Logicnext Softwares and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

/**
* This class consists of all methods to access the web API. Only this class can create 
* a connection to server. All methods defined in this class will return the response in 
* form of string 
* 
* methods of this class can only be accessed from Engine.
* @author kamal
*
*/
public class HttpCalls {
	
	private static HttpCalls singeltoneObject;
	
	private final String  BASEURL="http://dev1.logicnext.com/bizmo/";
	private final String  REGISTERUSER="user/register";
	private final String  REGISTERUSERRESEND="user/register/resend";
	private final String  VERIFYUSER="/user/verification";
	private final String  Profile_image_update="/user/profile/image/update";
	private final String  Profile_update="/user/profile/update";
	private final String  Profile_get="/user/profile/get";
	private final String  Profile_delete="user/profile/delete";
	private final String  Logout="/user/logout";
	
	private final String  CONNECTION_ADD="/connection/addConnections";
	private final String  CONNECTION_GET="/connection/get";
	private final String  CONNECTION_INVITE="/connection/invite";
	private final String  CONNECTION_EXCHANGE="/connection/exchange";
	private final String  CONNECTION_ACCEPT="/connection/request/accept";
	
	private final String  CHAT_UPLOAD="/chat/upload";
	
	private HostnameVerifier hostnameVerifier ;
    private DefaultHttpClient client;
    private SchemeRegistry registry ;
    		
    private SSLSocketFactory socketFactory;
    private ThreadSafeClientConnManager mgr;
    private DefaultHttpClient httpclient ;
	
    
    /**
     * this method will return a singleton 
     * instance of this class.
     * @return
     */
	public static HttpCalls getInstance()
	{
		if(singeltoneObject== null)
			singeltoneObject= new HttpCalls();
		return singeltoneObject;
	}

	
	/**
	 * This method will setup a https connection
	 * (needed only in secure environment)
	 */
	private void setHtppsClient()
	{
		hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		client = new DefaultHttpClient();
		registry = new SchemeRegistry();
		socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		registry.register (new Scheme ("http", PlainSocketFactory.getSocketFactory (), 80));
		mgr = new ThreadSafeClientConnManager(client.getParams(), registry);
		httpclient = new DefaultHttpClient(mgr, client.getParams());
		httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,30000);
		// Set verifier    
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
	}
	

	/**
	 * Call for register a new user
	 * @param country
	 * @param countryCode
	 * @param phoneNumber
	 * @return  Response of API 
	 */
	public String registerUser(String country, String countryCode, String phoneNumber)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+REGISTERUSER);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("country", country));
			nameValuePairs.add(new BasicNameValuePair("country_code", countryCode));
			nameValuePairs.add(new BasicNameValuePair("phone",phoneNumber));
			nameValuePairs.add(new BasicNameValuePair("sendsms","YES"));

			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	
	/**
	 * call for resenting verification code
	 * @param country
	 * @param countryCode
	 * @param phoneNumber
	 * @return
	 */
	public String resendVerification(String country, String countryCode, String phoneNumber)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+REGISTERUSERRESEND);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("country", country));
			nameValuePairs.add(new BasicNameValuePair("country_code", countryCode));
			nameValuePairs.add(new BasicNameValuePair("phone",phoneNumber));


			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	
	/**
	 * call for verification of code
	 * @param countryCode
	 * @param phoneNumber
	 * @param ActivatuionCode
	 * @return
	 */
	public String verifyUser(String countryCode, String phoneNumber, String ActivatuionCode)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+VERIFYUSER);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("verification_code", ActivatuionCode));
			nameValuePairs.add(new BasicNameValuePair("country_code", countryCode));
			nameValuePairs.add(new BasicNameValuePair("phone",phoneNumber));


			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	/*************************User Profile Calls************************************
	 * 
	 */
	
	
	public String updateUserProfile(String uid, String auth, User user)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+Profile_update);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth));
			nameValuePairs.add(new BasicNameValuePair("username", user.userName));
			nameValuePairs.add(new BasicNameValuePair("email", user.email));
			nameValuePairs.add(new BasicNameValuePair("region", user.region));
			nameValuePairs.add(new BasicNameValuePair("status", user.status));
			nameValuePairs.add(new BasicNameValuePair("social_id", user.socialId));
			nameValuePairs.add(new BasicNameValuePair("social_type", user.social_type));
			nameValuePairs.add(new BasicNameValuePair("profile_image_url", user.profile_image_url));


			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String updateUserProfilePicture(String uid, String auth_token, String path)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+Profile_image_update);
		try 
		{
			// Add your data
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
			multipartEntity.addPart("user_id", new StringBody(uid));
			multipartEntity.addPart("auth_token", new StringBody(auth_token));
			multipartEntity.addPart("profile_image", new FileBody(new File(path)));

			httppost.setEntity(multipartEntity);
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Updating profile pic ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Updating profile pic ", e.toString());
			return null;
		}
	}
	
	public String  getUserProfile(String uid, String auth_token)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+Profile_get);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String deleteUserProfile(String uid, String auth_token)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+Profile_delete);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String logout(String uid, String auth_token)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+Logout);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	
	/*************************************************************Connection API CALLS**********************************************
	 * 
	 */
	
	public String addConnection(String uid, String auth_token, String arrContacts, String autoAddConnections, String alloOthers)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CONNECTION_ADD);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			nameValuePairs.add(new BasicNameValuePair("address_book", arrContacts));
			nameValuePairs.add(new BasicNameValuePair("auto_add_connections", autoAddConnections));
			nameValuePairs.add(new BasicNameValuePair("allow_other_to_add", alloOthers));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String getConnection(String uid, String auth_token)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CONNECTION_GET);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String acceptRequest(String rid, String accept, String auth_token, String uid)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CONNECTION_ACCEPT);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("request_id", rid));
			nameValuePairs.add(new BasicNameValuePair("accept", accept));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String inviteUsers(String phones, String auth_token, String udi)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CONNECTION_INVITE);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("phones", phones));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));
			nameValuePairs.add(new BasicNameValuePair("user_id", udi));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	public String sendrequest(String uid, String tUID,  String auth_token)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CONNECTION_EXCHANGE);
		try 
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user_id", uid));
			nameValuePairs.add(new BasicNameValuePair("acceptor_user_id", tUID));
			nameValuePairs.add(new BasicNameValuePair("auth_token", auth_token));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Register User ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Register User  ", e.toString());
			return null;
		}
	}
	
	
	/******************************************************CHAT API CALLS*************************************
	 * 
	 */
	
	public String uploadFile(String uid, String auth_token, String path, String file_type)
	{
		httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BASEURL+CHAT_UPLOAD);
		try 
		{
			// Add your data
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
			multipartEntity.addPart("user_id", new StringBody(uid));
			multipartEntity.addPart("auth_token", new StringBody(auth_token));
			multipartEntity.addPart("file_type", new StringBody(file_type));
			multipartEntity.addPart("filename", new FileBody(new File(path)));

			httppost.setEntity(multipartEntity);
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntityGet = response.getEntity();
			InputStream istream = resEntityGet.getContent();
			
			String res= convertInputStreamtoString(istream);
			response.getEntity().consumeContent();
			return res;
		}
		catch (UnsupportedEncodingException e)
        {
			Log.i(" exception in Updating profile pic ", e.toString());
			return null;
		} 
		catch (Exception e)
		{
			Log.i(" exception in Updating profile pic ", e.toString());
			return null;
		}
	}
	
	/**
	 * This method will convert the given input-stream
	 * into  string
	 * @param istream
	 * @return String
	 * @throws IOException
	 * @author kamal
	 */
	public String convertInputStreamtoString(InputStream istream) throws IOException
	{
		InputStreamReader reader = new InputStreamReader(istream);
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[2048];
		int num;
		while (-1 != (num = reader.read(cbuf))) {
			buf.append(cbuf, 0, num);
		}
		String result = buf.toString();
		reader.close();
		
		Log.i("Response", result);
		return result;
	}

}
