package com.bizmo.engine;

import com.bizmo.communication.HttpCalls;
import com.bizmo.object.User;

public class BizmoEngine 
{
	private static BizmoEngine bEngine;
	
	private HttpCalls http;
	
	public static BizmoEngine getInstance()
	{
		if(bEngine== null)
			bEngine= new BizmoEngine();
			
			return bEngine;
	}
	
	public BizmoEngine() 
	{
		http= HttpCalls.getInstance();
	}
	
	public String registerUSer(String country, String cCode, String phone )
	{
		String userResponse=http.registerUser(country, cCode, phone);
		
		return userResponse;
	}
	
	public String sendVerification( String cCode, String phone , String act_code)
	{
		String userResponse=http.verifyUser( cCode, phone, act_code);
		
		return userResponse;
	}
	
	public String resendVerificatonCode(String country, String cCode, String phone )
	{
		String userResponse=http.resendVerification(country, cCode, phone);
		
		return userResponse;
	}
	
	public String getUserProfile(String uid, String auth )
	{
		String userResponse=http.getUserProfile(uid, auth);
		
		return userResponse;
	}
	public String deleteUserProfile(String uid, String auth )
	{
		String userResponse=http.deleteUserProfile(uid, auth);
		
		return userResponse;
	}
	public String updateUserProfile(String uid, String authCode, User user)
	{
		String userResponse=http.updateUserProfile(uid, authCode, user);
		
		return userResponse;
	}
	public String updateProfilePic(String uid, String authCode, String path)
	{
		String userResponse=http.updateUserProfilePicture(uid, authCode, path);
		
		return userResponse;
	}
	public String addConnections(String uid, String authCode, String arrUsers, String autoAdd, String allowAll)
	{
		String userResponse=http.addConnection(uid, authCode, arrUsers, autoAdd, allowAll);
		
		return userResponse;
	}
	public String getConnection(String uid, String authCode)
	{
		String userResponse=http.getConnection(uid, authCode);
		
		return userResponse;
	}
	
	public String logOut(String uid, String authCode)
	{
		String userResponse=http.logout(uid, authCode);
		
		return userResponse;
	}
	
	public String sendRequest(String uid, String tUID,String auth_token)
	{
		String userResponse=http.sendrequest(uid, tUID, auth_token);
		
		return userResponse;
	}
	public String acceptConnection(String rid, String accept, String authCode, String uid)
	{
		String userResponse=http.acceptRequest(rid, accept, authCode, uid);
		
		return userResponse;
	}
	
	public String inviteUser(String phoneArr, String authCode, String uid)
	{
		String userResponse=http.inviteUsers(phoneArr, authCode, uid);
		
		return userResponse;
	}
	
	public String uploadImage(String uid, String authCode, String filePath, String file_type)
	{
		String file_typr= "image";
		if(file_type.equalsIgnoreCase("0"))
		{
			file_typr= "image";
		}
		else if(file_type.equalsIgnoreCase("2"))
		{
			file_typr= "video";
		}
		else
			file_typr= "other";
		String userResponse=http.uploadFile(uid, authCode, filePath, file_typr);
		return userResponse;
	}
	
	public static String getTimeStamp()
	{
		 Long tsLong = System.currentTimeMillis();
         return tsLong.toString();
	}

}
