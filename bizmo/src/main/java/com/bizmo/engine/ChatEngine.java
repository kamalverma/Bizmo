package com.bizmo.engine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;
import org.jivesoftware.smackx.ChatStateManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.LastActivityManager;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.databaseaccess.DatabaseHelper;
import com.android.databaseaccess.DictionaryEntry;
import com.bizmo.interfaces.OnCurrentUserMessageLitener;
import com.bizmo.object.User;
import com.logicnext.bizmo.ChatActivity;
import com.logicnext.bizmo.R;
import com.logicnext.bizmo.XmppSocketFactory;
import com.ui.common.Constants;

public class ChatEngine extends Application  implements PacketListener,  ChatStateListener, ChatManagerListener, ReceiptReceivedListener, RosterListener, InvitationListener, SubjectUpdatedListener
{
	public XMPPConnection connection;
	public static final String ServerAddress = "dev1.logicnext.com";
	public static final String FolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() ;
	private static final int PORT = 5280;
	private  SQLiteDatabase _database;
	
	OnCurrentUserMessageLitener messageListener;
	public String currentUserUid;
	UploadCompletionReceiver uploadComplete;
	DownloadCompletionReceiver downloadReceiver;
	private DownloadManager dm;
	
	HashMap<Long, String> hashDownload= new HashMap<Long, String>();
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("Init called", "Application should work");
		SmackAndroid.init(this);
		
		IntentFilter filter = new IntentFilter(UploadCompletionReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        
        
        uploadComplete = new UploadCompletionReceiver();
        
        try
        {
        	  unregisterReceiver(uploadComplete);
        }
        catch(IllegalArgumentException ie)
        {
        	
        }
        registerReceiver(uploadComplete, filter);
        
        downloadReceiver= new DownloadCompletionReceiver();
        
        try
        {
        	  unregisterReceiver(downloadReceiver);
        }
        catch(IllegalArgumentException ie)
        {
        	
        }
        registerReceiver(downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	public void setCurrentUserListener(OnCurrentUserMessageLitener cl, String currentUser)
	{
		messageListener=cl;
		currentUserUid=  currentUser;
	}
	public void createConnection()
	{
		if(connection== null || !connection.isConnected())
		{
				AndroidConnectionConfiguration conConfig= null;
				try 
				{
					conConfig = new AndroidConnectionConfiguration(ServerAddress, PORT);
					conConfig.setDebuggerEnabled(true);// Enable xmpp debugging at Logcat
				} catch (XMPPException e1) 
				{
					e1.printStackTrace();
				}
				conConfig.setSocketFactory(new XmppSocketFactory());
		        // set up cert location
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					conConfig.setTruststoreType("AndroidCAStore");
					conConfig.setTruststorePassword(null);
					conConfig.setTruststorePath(null);
		        } else {
		        	conConfig.setTruststoreType("BKS");
		            String path = System.getProperty("javax.net.ssl.trustStore");
		            if (path == null) {
		                path = System.getProperty("java.home") + File.separator + "etc"
		                    + File.separator + "security" + File.separator
		                    + "cacerts.bks";
		            }
		            conConfig.setTruststorePath(path);
		        }
				//Allow reconnection
				conConfig.setReconnectionAllowed(true);
				conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
				conConfig.setSendPresence(true);
			
				connection = new XMPPConnection(conConfig);
			if(connection!= null)
			{
				try {
					//try connecting 
					connection.connect();
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean login(String uname, String pwd)
	{
		//Check for connection
		createConnection();
		
		if(connection.isConnected())
		{
			//Check id already authenticated
			if(!connection.isAuthenticated())
			{
				  try {
			            XHTMLManager.setServiceEnabled(connection, false);
			        } catch (Exception e) {
			            Log.e("Failed to set ServiceEnabled flag for XHTMLManager", e.toString());
			            // Managing an issue with ServiceDiscoveryManager
			            if (e.getMessage() == null) {
			            }
			        }
				
				  ProviderManager.getInstance().addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
				  ProviderManager.getInstance().addExtensionProvider(DeliveryReceiptRequest.ELEMENT, new DeliveryReceiptRequest().getNamespace(), new DeliveryReceiptRequest.Provider());
				  SmackConfiguration.setPacketReplyTimeout(10000);
				try {
					connection.login(uname, pwd);
					Presence presence = new Presence(Presence.Type.available);
					connection.sendPacket(presence);
					connection.addPacketListener(this, null);
				//	connection.addConnectionListener(this);
					  ChatStateManager.getInstance(connection);
					connection.getChatManager().addChatListener(this);
					
					connection.getRoster().addRosterListener(this);
					// DeliveryReceiptManager.getInstanceFor(connection).enableAutoReceipts();
					 DeliveryReceiptManager.getInstanceFor(connection).addReceiptReceivedListener(this);
					 MultiUserChat.addInvitationListener(connection, this);
					return true;
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
			else 
				return true;
		}
		return false;
	}
	
	
	public void getPresence(String uid)
	{
        	String jid= uid+"@"+ServerAddress;//
        	Presence p= connection.getRoster().getPresence(jid);
        	 
            if(p.getType().equals(Presence.Type.available))
    		{
            	 
        				   if(messageListener!= null)
        					   messageListener.onPresenseReceived("Online");
    		}
            else if(p.getType().equals(Presence.Type.unavailable))
            {
            	String resource="";
            	String from= p.getFrom();
            	if (from != null && from.lastIndexOf("/") > 0) {
                    resource = from.substring(from.lastIndexOf("/") + 1);
                    // from here you can track all active resources
                }
            	if(resource.startsWith("Smack"))
            		getLastSeen(uid,resource);
            	else
            		getLastSeen(uid);
            }
	}
	
	public void getLastSeen(String useriD, String resource){
        try {
        	String jid= useriD+"@"+ServerAddress+resource;//
        	 LastActivity activity=LastActivityManager.getLastActivity(connection, jid);
        	 
        	  if(messageListener!= null)
				   messageListener.onPresenseReceived("Last Seen Before:"+activity.lastActivity+" Seconds Before");
            Log.d("LAST ACTIVITY",activity.lastActivity+"");
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
	
	public void getLastSeen(String useriD){
        try {
        	String jid= useriD+"@"+ServerAddress;//
        	 LastActivity activity=LastActivityManager.getLastActivity(connection, jid);
        	 
        	  if(messageListener!= null)
				   messageListener.onPresenseReceived("Last Seen Before:"+activity.lastActivity+" Seconds Before");
            Log.d("LAST ACTIVITY",activity.lastActivity+"");
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
	
	public boolean logout()
	{
		if(connection== null || !connection.isConnected())
		{
			//createConnection();
			return true;
		}
		else
		{
			Presence presence = new Presence(Presence.Type.unavailable);
			connection.disconnect(presence);
			
			return true;
		}
	}
	
	
	public boolean createGroupChat(String title, Vector<String> vec)
	{
		MultiUserChat muc= new MultiUserChat(connection, getTime()+"@conference."+ServerAddress);
		try {
			muc.create("I am");
			Form form = muc.getConfigurationForm();
		      // Create a new form to submit based on the original form
		      Form submitForm = form.createAnswerForm();
		      // Add default answers to the form to submit
		      for (Iterator fields = form.getFields(); fields.hasNext();) {
		          FormField field = (FormField) fields.next();
		          if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
		              // Sets the default value as the answer
		              submitForm.setDefaultAnswer(field.getVariable());
		          }
		      }
		      // Sets the new owner of the room
//		      List owners = new ArrayList();
//		      owners.add("johndoe@jabber.org");
//		      submitForm.setAnswer("muc#roomconfig_roomowners", owners);
		      // Send the completed form (with default values) to the server to configure the room
		      submitForm.setAnswer("muc#roomconfig_roomname", title);
		      muc.sendConfigurationForm(submitForm);
		      
		      for(int i=0; i<vec.size(); i++)
		      {
		    	  muc.invite(vec.get(i)+ServerAddress+"", "Meet me in this excellent room");
		      }
		      
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 
	 * @param message message body
	 * @param to receiver's ID (Group Id always in case of group chat)
	 * @param from owners ID
	 * @param mode chat type 0-chat, 1-group chat
	 */
	public void sendTextMessage(String message, String to, String from, String mode)
	{
		Message m= new Message();
		JSONObject mObj= new JSONObject();
		try {
			mObj.put("message_text", message);
			mObj.put("type", "message_type_text");
			mObj.put("message_time", getTime()+"");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
		
		if(mode.equalsIgnoreCase("0"))
			m.setType(Type.chat);
		else if(mode.equalsIgnoreCase("1"))
			m.setType(Type.groupchat);
		
		
		m.setTo(to);
		m.setFrom(from);
		
		insertIntoChatMessage(m, "", "0", mode);
		
		m.addExtension(new DeliveryReceiptRequest());
		connection.sendPacket(m);
	}
	
	
	//{"type":"message_type_map","message_time":"1400665322346","lat":"37.17818069458002","lng":"-96.05458068847656","message_text":"Location","loc":" Sedan KS"}
	public void sendLocation(String lat, String lng, String loc, String to, String from, String mode)
	{
		Message m= new Message();
		JSONObject mObj= new JSONObject();
		try {
			mObj.put("message_text", "Location");
			mObj.put("type", "message_type_map");
			mObj.put("message_time", getTime());
			mObj.put("lat", lat+"");
			mObj.put("lng", lng+"");
			mObj.put("loc", loc+"");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
	//	m.setPacketID(UUID.randomUUID().toString());
		if(mode.equalsIgnoreCase("0"))
			m.setType(Type.chat);
		else if(mode.equalsIgnoreCase("1"))
			m.setType(Type.groupchat);
		
		
		m.setTo(to);
		m.setFrom(from);
		insertIntoChatMessage(m, "", "0", mode);
		
		m.addExtension(new DeliveryReceiptRequest());
		connection.sendPacket(m);
		
	}
	
	public void sendMutiMediaMessage(String tid, String global_path, String duarion, String file_size)
	{
		DictionaryEntry [][] data= getTask(tid);
		int mType= Integer.parseInt((data[0][4].value.toString()));
		String to=data[0][2].value.toString()+"@"+ServerAddress;
		String from=data[0][3].value.toString()+"@"+ServerAddress;
		String chat_tpe=data[0][5].value.toString();
		String msg_id=data[0][1].value.toString();
		Message m= new Message();
		
		m.setPacketID(msg_id);
		JSONObject mObj= new JSONObject();
		try {
			switch (mType) {
			case 0://Image
				mObj.put("message_text", "image");
				mObj.put("type", Constants.MSG_TYPE_IMAGE );
				mObj.put("message_media_size", "" );
				mObj.put("message_time", getTime());
				mObj.put("message_media_duration", "" );
				mObj.put("ext", global_path.substring(global_path.lastIndexOf(".")));
				mObj.put("url", global_path+"" );
				break;
			case 1://Audio
				mObj.put("message_text", "audio");
				mObj.put("type", Constants.MSG_TYPE_AUDIO );
				mObj.put("message_media_size",file_size+ "" );
				mObj.put("message_time", getTime());
				mObj.put("message_media_duration", duarion+"" );
				mObj.put("ext", global_path.substring(global_path.lastIndexOf(".")));
				mObj.put("url", global_path+"" );
				break;
			case 2://Video
				mObj.put("message_text", "video");
				mObj.put("type", Constants.MSG_TYPE_VIDEO );
				mObj.put("ext", global_path.substring(global_path.lastIndexOf(".")));
				mObj.put("message_media_size",file_size+ "" );
				mObj.put("message_time", getTime());
				mObj.put("message_media_duration", duarion+"" );
				mObj.put("url", global_path+"" );
				break;

			default:
				mObj.put("type", Constants.MSG_TYPE_TEXT );
				mObj.put("message_time", getTime());
				break;
			}
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
		if(chat_tpe.equals("0"))
			m.setType(Type.chat);
		else if(chat_tpe.equals("1"))
			m.setType(Type.groupchat);
		
		m.setTo(to);
		m.setFrom(from);
		
		updateMessageStatus(msg_id, "0");
		
		m.addExtension(new DeliveryReceiptRequest());
		connection.sendPacket(m);
		
		
		updateGlobalPath(getMidOfMssage(msg_id), "global_path");
		
	  if(currentUserUid!= null && m.getTo().split("@")[0].equalsIgnoreCase(currentUserUid))
	   {
		   if(messageListener!= null)
			   messageListener.onMessageReceived();
	   }
	}
	
	
	
	public void sendVCard(String tid, String global_path, String name, String phone)
	{
		DictionaryEntry [][] data= getTask(tid);
		int mType= Integer.parseInt((data[0][4].value.toString()));
		String to=data[0][2].value.toString()+"@"+ServerAddress;
		String from=data[0][3].value.toString()+"@"+ServerAddress;
		String chat_tpe=data[0][5].value.toString();
		String msg_id=data[0][1].value.toString();
		Message m= new Message();
		
		m.setPacketID(msg_id);
		JSONObject mObj= new JSONObject();
		try {
			mObj.put("message_text", "Contact");
			mObj.put("message_time", getTime());
			mObj.put("ext", ".vcf");
			mObj.put("name", name+"");
			mObj.put("phone", phone+"");
			mObj.put("url", global_path+"");
			mObj.put("type", Constants.MSG_TYPE_VCARD );
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
		if(chat_tpe.equals("0"))
			m.setType(Type.chat);
		else if(chat_tpe.equals("1"))
			m.setType(Type.groupchat);
		
		m.setTo(to);
		m.setFrom(from);
		m.addExtension(new DeliveryReceiptRequest());
		connection.sendPacket(m);
		updateMessageStatus(msg_id, "0");
		updateGlobalPath(getMidOfMssage(msg_id), "global_path");
		
	  if(currentUserUid!= null && m.getTo().split("@")[0].equalsIgnoreCase(currentUserUid))
	   {
		   if(messageListener!= null)
			   messageListener.onMessageReceived();
	   }
	}
	
	public void addVCard(String to, String from, String file_path, String name, String phone, String mode)
	{
		String mid= getTime();
		//InsertInto Media
		insertIntoMedia(mid, file_path, "", to.split("@")[0]);
		
		//InsertIntoChat
	   Message m= new Message();
	   
		JSONObject mObj= new JSONObject();
		try {
				mObj.put("message_text", "Contact");
				mObj.put("message_time", getTime());
				mObj.put("ext", ".vcf");
				mObj.put("name", name+"");
				mObj.put("phone", phone+"");
				mObj.put("type", Constants.MSG_TYPE_VCARD );
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
		
		if(mode.equalsIgnoreCase("0"))
			m.setType(Type.chat);
		else if(mode.equalsIgnoreCase("1"))
			m.setType(Type.groupchat);
		
		
		m.setTo(to);
		m.setFrom(from);
		insertIntoChatMessage(m, mid, "-5", mode);
		//insert into task
		addUploadVCFTask(to.split("@")[0], from.split("@")[0],  "0", file_path, m.getPacketID(), name, phone);
	}
	
	public void addMultiMediaMessage(String to, String from, String file_path, int mType, String duration, String size, String mode)
	{
		String mid= getTime();
		//InsertInto Media
		insertIntoMedia(mid, file_path, "", to.split("@")[0]);
		//InsertIntoChat
	   Message m= new Message();
	   
		JSONObject mObj= new JSONObject();
		try {
			switch (mType) {
			case 0://Image
				mObj.put("message_text", "image");
				mObj.put("message_time", getTime());
				mObj.put("ext", file_path.substring(file_path.lastIndexOf(".")));
				mObj.put("type", Constants.MSG_TYPE_IMAGE );
				break;
			case 1://Audio
				mObj.put("message_text", "audio");
				mObj.put("message_time", getTime());
				mObj.put("ext", file_path.substring(file_path.lastIndexOf(".")));
				mObj.put("message_media_duration", duration+"");
				mObj.put("type", Constants.MSG_TYPE_AUDIO );
				break;
			case 2://Video
				mObj.put("message_text", "video");
				mObj.put("message_time", getTime());
				mObj.put("message_media_duration", duration+"");
				mObj.put("ext", file_path.substring(file_path.lastIndexOf(".")));
				mObj.put("type", Constants.MSG_TYPE_VIDEO );
				break;
				
			case 3://VCF
				mObj.put("message_text", "Contact");
				mObj.put("message_time", getTime());
				mObj.put("ext", ".vcf");
				mObj.put("name", ".vcf");
				mObj.put("phone", ".vcf");
				mObj.put("type", Constants.MSG_TYPE_VCARD );
				break;

			default:
				mObj.put("type", Constants.MSG_TYPE_TEXT );
				mObj.put("message_time", getTime());
				break;
			}
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		m.setBody(mObj.toString());
		
		if(mode.equalsIgnoreCase("0"))
			m.setType(Type.chat);
		else if(mode.equalsIgnoreCase("1"))
			m.setType(Type.groupchat);
		
		
		m.setTo(to);
		m.setFrom(from);
		insertIntoChatMessage(m, mid, "-5", mode);
		
		//insert into task
		addUploadMediaTask(to.split("@")[0], from.split("@")[0], mType+"", "0", file_path, m.getPacketID(), duration, size);
	}
	
	public void getMultiMediaMessage(Message m)
	{
		String mid= getTime();
		String mType="";
		String file_path="";
		//insert into chat
		if(m.getType()== Type.chat)
			insertIntoChatMessage(m, mid, "-5", "0");
		else if(m.getType()== Type.groupchat)
			insertIntoChatMessage(m, mid, "-5", "1");
		
		
		try {
			
			JSONObject obj= new JSONObject(m.getBody());
			String type= obj.getString("type");
			String path="";
			if(type.equalsIgnoreCase(Constants.MSG_TYPE_IMAGE))
			{
				mType="0";
				  path =  Constants.ImagePath+getTime()+obj.getString("ext");
		          File f= new File(ChatEngine.FolderPath+ Constants.ImagePath);
		          if(!f.exists())
		          	f.mkdirs();
			}
			else if(type.equalsIgnoreCase(Constants.MSG_TYPE_AUDIO))
			{
				mType="1";
				 path = Constants.AudioPath+getTime()+obj.getString("ext");
		          File f= new File(ChatEngine.FolderPath+ Constants.AudioPath);
		          if(!f.exists())
		          	f.mkdirs();
			}
			else if(type.equalsIgnoreCase(Constants.MSG_TYPE_VIDEO))
			{
				mType="2";
				 path = Constants.VideoPath+getTime()+obj.getString("ext");
		          File f= new File(ChatEngine.FolderPath+ Constants.VideoPath);
		          if(!f.exists())
		          	f.mkdirs();
			}
			else if(type.equalsIgnoreCase(Constants.MSG_TYPE_VCARD))
			{
				mType="3";
				 path = Constants.VCFPath+getTime()+obj.getString("ext");
		          File f= new File(ChatEngine.FolderPath+ Constants.VCFPath);
		          if(!f.exists())
		          	f.mkdirs();
			}
			
			file_path= obj.getString("url");
			//InsertInto Media
			 insertIntoMedia(mid, FolderPath+path, file_path+"", m.getFrom().split("@")[0]);
			
			
			if(m.getType().equals(Type.chat))
				addDownloadMediaTask(m.getTo().split("@")[0], m.getFrom().split("@")[0], mType+"", "0", file_path, path, m.getPacketID());
			else if(m.getType().equals(Type.groupchat))
				addDownloadMediaTask(m.getTo().split("@")[0], m.getFrom().split("@")[0], mType+"", "1", file_path,path, m.getPacketID());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		
		Log.i("trim memory called", "should work");
		if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) 
		{
            // We're in the Background
			if(connection!= null && connection.isConnected() && connection.isAuthenticated())
			{
				Message m= new Message();
				m.setBody("ABCD");
			//	m.setPacketID(UUID.randomUUID().toString());
				m.setType(Type.chat);
				connection.sendPacket(m);
				Presence presence = new Presence(Presence.Type.unavailable);
				connection.sendPacket(presence);
			}
        }
	}

	@Override
	public void processPacket(Packet packet) 
	{
		
		Log.i("something is recceived", "Preces Packet");
	}

	@Override
	public void processMessage(Chat chat, Message msg) {
		
		
		if(msg.getBody().equalsIgnoreCase("ABCD"))
		{
			return;
		}
		if(msg.getType()== Type.chat  || msg.getType()== Type.groupchat)
		{
			Packet received = new Message();
			received.addExtension(new DeliveryReceipt(msg.getPacketID()));
			
			received.setTo(msg.getFrom());
			connection.sendPacket(received);
			try {
				JSONObject obj= new JSONObject(msg.getBody());
				String type= obj.getString("type");
				if(type.equals(Constants.MSG_TYPE_TEXT))
				{
					//Text Message
					if(msg.getType()== Type.chat)
						 insertIntoChatMessage(msg, "", "", "0");
					else if(msg.getType()== Type.groupchat)
						 insertIntoChatMessage(msg, "", "", "1");
					
				}
				else if(type.equals(Constants.MSG_TYPE_IMAGE))
				{
					//Image Message
					getMultiMediaMessage(msg);
				}
				else if(type.equals(Constants.MSG_TYPE_AUDIO))
				{
					//Text Message
					getMultiMediaMessage(msg);
				}
				else if(type.equals(Constants.MSG_TYPE_VIDEO))
				{
					//Text Message
					getMultiMediaMessage(msg);
				}
				else if(type.equals(Constants.MSG_TYPE_VCARD))
				{
					//Text Message
					getMultiMediaMessage(msg);
				}
				else if(type.equals(Constants.MSG_TYPE_MAP))
				{
					//Location Message
					if(msg.getType()== Type.chat)
						 insertIntoChatMessage(msg, "", "", "0");
					else if(msg.getType()== Type.groupchat)
						 insertIntoChatMessage(msg, "", "", "1");
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		 //  53625551bdd094012c8b4569@dev1.logicnext.com/Smack
		  // String from= m
		   if(currentUserUid!= null && msg.getFrom().split("@")[0].equalsIgnoreCase(currentUserUid))
		   {
			   if(messageListener!= null)
				   messageListener.onMessageReceived();
		   }
		   else
		   {
			   if(msg.getType()== Type.chat)
				   addToUnread(msg.getFrom().split("@")[0] );
			   else if(msg.getType()== Type.groupchat)
				   addToUnread(msg.getTo().split("@")[0] );
			   
			   
			   showMessageInNotification(msg);
		   }
		}
		else
		{
			Log.i(msg.getType()+" is recceived", msg.getSubject()+"  Precess message");
		}
	}
	
	@Override
	public void onReceiptReceived(String arg0, String arg1, String arg2) {
		addReceiptToMessage(arg2);
	}

	private void addReceiptToMessage(String msg_id)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		ContentValues values= new ContentValues();
		values.put("message_status","1");
		long l=_database.update("Chat_History", values, "message_id='"+msg_id+"'", null);
		
		//Log.i("rows updates by receipt", "gdsfgdhgf   "+l );
		//
		
		if(messageListener!= null)
			messageListener.onMessageReceived();
	}
	
	private void updateMessageStatus(String msg_id, String status)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		ContentValues values= new ContentValues();
		values.put("message_status", status);
		long l=_database.update("Chat_History", values, "message_id='"+msg_id+"'", null);
		
		Log.i("rows updates by status", "gdsfgdhgf   "+l );
		
		if(messageListener!= null)
			messageListener.onMessageReceived();
	}
	
	private void updateGlobalPath(String mid, String gPath)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		ContentValues values= new ContentValues();
		values.put("media_global_path",gPath+"");
		
		long l=_database.update("media", values, "mid='"+mid+"'", null);
		
		Log.i("rows updates by Media", "gdsfgdhgf   "+l );
		
		if(messageListener!= null)
			messageListener.onMessageReceived();
	}
	
	private void showMessageInNotification(Message msg)
	{
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(""+msg.getFrom().split("@")[0])
		        .setAutoCancel(true);
		
		mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE |Notification.DEFAULT_SOUND);
		        
		    	try {
					JSONObject msg_obj = new JSONObject(msg.getBody());
					mBuilder.setContentText(msg_obj.getString("message_text"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ChatActivity.class);
		
		resultIntent.putExtra("uid", msg.getFrom().split("@")[0]);
		resultIntent.putExtra("uname",msg.getFrom().split("@")[0]);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ChatActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build());
		
		
	}
	
	
	
	
	private void insertIntoChatMessage(Message msg, String mid, String msg_status, String mode)
	{
		ContentValues values= new ContentValues();
		values.put("message_text", msg.getBody()+"");
		values.put("message_id", msg.getPacketID()+"");
		values.put("message_owner", msg.getFrom().split("@")[0]+"");
		values.put("message_to", msg.getTo().split("@")[0]+"");
		values.put("message_mode", mode+"");
		
		
		// Retrieve Message type from body & insert
		try {
			JSONObject obj= new JSONObject(msg.getBody());
			values.put("message_time", obj.getString("message_time")+"");
			values.put("message_type", obj.getString("message_type")+"");
		} catch (JSONException e) {
			e.printStackTrace();
			values.put("message_time",getTime()+"");
			values.put("message_type", Constants.MSG_TYPE_TEXT+"");
		}
		values.put("message_status", msg_status);
		
		values.put("mid", mid+"");
		
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		long l=_database.insert("chat_history", null, values);
		
		Log.i("inserted in chat", l+"  z");
	}
	
	private void insertIntoMedia(String mid, String lpath, String gPath, String user)
	{
		ContentValues values= new ContentValues();
		values.put("mid", mid+"");
		values.put("media_user", user+"");
		values.put("media_local_path", lpath+"");
		values.put("media_global_path",gPath+"");
		
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		long l=_database.insert("media", null, values);
		Log.i("inserted in media", l+"  z");
	}
	
	public  String getMediapath(String mid)
	{
		String  path="";
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		DictionaryEntry [][] data = null;
		try
		{
			data= DatabaseHelper.get("select media_local_path from media where mid='"+mid +"'" );
			
			if (data != null) 
			{
					Log.i("local path",data[0][0].value.toString());
					path= data[0][0].value.toString();
			}
		}
		catch (Exception e) 
		{
			Log.i("getFilePathLocal()", "error in getting path");
			path = "";
		}
		return  path;
	}
	
	private void addUploadMediaTask( String user_to, String user_owner, String mtype, String chat_type, String local_path, String msgId, String dur, String size)
	{
		String tid=getTime();
		
		ContentValues values= new ContentValues();
		values.put("type", "1");
		values.put("msg_id", msgId);
		values.put("user_to", user_to+"");
		values.put("user_owner", user_owner+"");
		values.put("m_type",mtype+"");
		values.put("chat_type",chat_type+"");
		values.put("local_path",local_path+"");
		values.put("tid",tid+"");
		
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		long l=_database.insert("task", null, values);
		Log.i("inserted in task", l+"  z");
		
		//Add uploading task in queue
		Intent intent =new Intent(this, UploadTask.class);
		Bundle b= new Bundle();
		b.putString("tid", tid);
		b.putString("path", local_path );
		b.putString("type", mtype );
		b.putString("duration", dur );
		b.putString("size", size );
		intent.putExtra("bundle", b);
		startService(intent);
	}
	
	private void addUploadVCFTask( String user_to, String user_owner, String chat_type, String local_path, String msgId, String name, String phone)
	{
		String tid=getTime();
		
		ContentValues values= new ContentValues();
		values.put("type", "1");
		values.put("msg_id", msgId);
		values.put("user_to", user_to+"");
		values.put("user_owner", user_owner+"");
		values.put("m_type","3");
		values.put("chat_type",chat_type+"");
		values.put("local_path",local_path+"");
		values.put("tid",tid+"");
		
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		long l=_database.insert("task", null, values);
		Log.i("inserted in task", l+"  z");
		
		//Add uploading task in queue
		Intent intent =new Intent(this, UploadTask.class);
		Bundle b= new Bundle();
		b.putString("tid", tid);
		b.putString("path", local_path );
		b.putString("type", "3" );
		b.putString("name", name+"");
		b.putString("phone", phone+"" );
		intent.putExtra("bundle", b);
		startService(intent);
	}
	
	private void addDownloadMediaTask( String user_to, String user_owner, String mtype, String chat_type, String g_path, String fileName,  String msgId)
	{
		String tid=getTime();
		
		ContentValues values= new ContentValues();
		values.put("type", "0");
		values.put("msg_id", msgId);
		values.put("user_to", user_to+"");
		values.put("user_owner", user_owner+"");
		values.put("m_type",mtype+"");
		values.put("chat_type",chat_type+"");
		values.put("global_path",g_path+"");
		values.put("local_path",FolderPath+fileName+"");
		values.put("tid",tid+"");
		
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		long l=_database.insert("task", null, values);
		Log.i("inserted in task", l+"  z");
		
		//Add Downloading task in queue
		
		 dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
	      Request request = new Request(
	                Uri.parse(g_path));
	     request .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                  DownloadManager.Request.NETWORK_MOBILE)
		.setAllowedOverRoaming(true)
		.setTitle("Bizmo")
		.setDescription("Downloading Bizmo Media")
		.setDestinationInExternalPublicDir("", fileName);
	      long  enqueue = dm.enqueue(request);
	       hashDownload.put(enqueue, tid);
	       
	       
//		Intent intent =new Intent(this, DownloadTask.class);
//		Bundle b= new Bundle();
//		b.putString("tid", tid);
//		b.putString("path", g_path );
//		b.putString("type", mtype );
//		intent.putExtra("bundle", b);
//		startService(intent);
	}
	
	public  DictionaryEntry [][]   getTask(String tid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		DictionaryEntry [][] data = null;
		try
		{
			data= DatabaseHelper.get("SELECT * from task where tid='"+tid+"'" );
		}
		catch (Exception e) 
		{
		}
		return  data;
	}

	private void removeFromTask(String tid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		_database.delete("task", "tid='"+tid+"'", null);
	}
	
	public  String   getMidOfMssage(String msg_id)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		DictionaryEntry [][] data = null;
		try
		{
			data= DatabaseHelper.get("SELECT mid from CHAT_HISTORY where message_id='"+msg_id+"'" );
		}
		catch (Exception e) 
		{
		}
		return  data[0][0].value.toString();
	}
	
	public  String   getMessage(String msg_id)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		DictionaryEntry [][] data = null;
		try
		{
			data= DatabaseHelper.get("SELECT message_text from CHAT_HISTORY where message_id='"+msg_id+"'" );
		}
		catch (Exception e) 
		{
		}
		return  data[0][0].value.toString();
	}
	
	public  Cursor getChats(String buddyUid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		String query = "SELECT *, message_id as _id from CHAT_HISTORY where message_to='"+buddyUid +"' or message_owner='"+buddyUid+"'";
		Cursor c;
		c = DatabaseHelper.getData(query);
		return c;
	}
	
	public  Cursor getMembers(String constraint)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		if (constraint == null  ||  constraint.length () == 0)  {
			String query = "SELECT *, uid as _id from connections where utype='0'" ;
			Cursor c;
			c = DatabaseHelper.getData(query);
			return c;
	    }  else  {
	    	
	    	String value = "%"+constraint+"%";
	    	String query = "SELECT *, uid as _id from connections where utype='0' and uname LIKE  '"+value+"'" ;
			Cursor c;
			c = DatabaseHelper.getData(query);
			return c;
	    }
	
	}
	
	
	public  Cursor getRecentChats(String uid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		String query = "select *, message_id as _id from CHAT_HISTORY where message_time in (select max(message_time) from (select *  from CHAT_HISTORY )  " +
				"as hist1 ,  (SELECT  distinct message_owner  FROM CHAT_HISTORY  union SELECT  distinct message_to FROM CHAT_HISTORY ) as user " +
				"where hist1.message_to= user.message_owner or hist1.message_owner=user.message_owner GROUP BY user.message_owner )  ORDER BY  CHAT_HISTORY.message_time DESC";
		Cursor c;
		c = DatabaseHelper.getData(query);
		return c;
	}
	
	public  Vector<User> getConnections()
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		String query = "SELECT * from connections where utype='0'";
		Cursor c;
		c = DatabaseHelper.getData(query);
		
		Vector<User> vecConn= new Vector<User>();
		if(c== null)
			return vecConn;
		
		if(c.moveToFirst())
		{
			do {
				try
				{
					User cab= new User();
					cab.userId = c.getString(0);
					cab.userName = c.getString(1);
					cab.phone = c.getString(2);
					cab.email = c.getString(3);
					cab.profile_image_url = c.getString(4);
					
					vecConn.add(cab);
				} 
				catch (Exception e)
				{
					Log.i("Error in getConnections() ", e.toString());
				}
			} while (c.moveToNext());
		}
		c.close();
		
		return vecConn;
	}
	public   Vector<User> getRequests()
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		String query = "SELECT * from  connections where utype='1' ";
		Cursor c;
		c = DatabaseHelper.getData(query);
		Vector<User> vecReq= new Vector<User>();
		
		if(c== null)
			return vecReq;
		if(c.moveToFirst())
		{
			do {
				try
				{
					User cab= new User();
					cab.userId = c.getString(0);
					cab.userName = c.getString(1);
					cab.phone = c.getString(2);
					cab.email = c.getString(3);
					cab.profile_image_url = c.getString(4);
					cab.reqId = c.getString(6);
				
					vecReq.add(cab);
				} 
				catch (Exception e)
				{
					Log.i("Error in getConnections() ", e.toString());
				}
			} while (c.moveToNext());
		}
		c.close();
		
		return vecReq;
	}
	
	public   Vector<User> getPhoneBook()
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		String query = "SELECT * from connections where utype='2'";
		Cursor c;
		c = DatabaseHelper.getData(query);
		
		
		Vector<User> vecPhone= new Vector<User>();
		
		if(c== null)
			return vecPhone;
		
		if(c.moveToFirst())
		{
			do {
				try
				{
					User cab= new User();
					cab.userId = c.getString(0);
					cab.userName = c.getString(1);
					cab.phone = c.getString(2);
					cab.email = c.getString(3);
					cab.profile_image_url = c.getString(4);
					String req= c.getString(7);
					if(req.equalsIgnoreCase("YES"))
						cab.isRequestRequired= true;
					else
						cab.isRequestRequired= false;
					vecPhone.add(cab);
				} 
				catch (Exception e)
				{
					Log.i("Error in getConnections() ", e.toString());
				}
			} while (c.moveToNext());
		}
		c.close();
		return vecPhone;
	}
	
	public User getUserDetails(String uid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		String query = "SELECT * from connections where uid='"+uid+"'";
		Cursor c;
		c = DatabaseHelper.getData(query);
		
		if(c== null)
		{
			return null;
		}
		else
		{
			if(c.moveToFirst())
			{
					try
					{
						User cab= new User();
						cab.userId = c.getString(0);
						cab.userName = c.getString(1);
						cab.phone = c.getString(2);
						cab.email = c.getString(3);
						cab.profile_image_url = c.getString(4);
						return cab; 
					} 
					catch (Exception e)
					{
						Log.i("Error in getConnections() ", e.toString());
						return null;
					}
			}
		}
		c.close();
		return null;
		
	}
	public User getNewUserDetails(String uid)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		
		String query = "SELECT * from new_users where uid='"+uid+"'";
		Cursor c;
		c = DatabaseHelper.getData(query);
		
		if(c== null)
		{
			return null;
		}
		else
		{
			if(c.moveToFirst())
			{
					try
					{
						User cab= new User();
						cab.userId = c.getString(0);
						cab.userName = c.getString(1);
						cab.profile_image_url = c.getString(2);
						cab.phone = c.getString(3);
						return cab; 
					} 
					catch (Exception e)
					{
						Log.i("Error in getConnections() ", e.toString());
						return null;
					}
			}
		}
		c.close();
		return null;
		
	}
	
	public  void insertIntoConnections(Vector<User> vecRequests, Vector<User> vecConnections, Vector<User> vecPhoneBook)
	{
		if(_database== null || !_database.isOpen())
			  _database= DatabaseHelper.openDataBase();
		//_database.delete("requests", null, null);
		_database.delete("connections", null, null);
	//	_database.delete("phone_book", null, null);
		
		for(int i=0; i<vecRequests.size(); i++)
		{
			User u= vecRequests.get(i);
			ContentValues values= new ContentValues();
			values.put("uid", u.userId+"");
			values.put("rid", u.reqId+"");
			values.put("utype", "1");
			values.put("uname", u.userName+"");
			values.put("phone", u.phone+"");
			values.put("profile_image",u.profile_image_url+"");
			values.put("email",u.email+"");
			long l=_database.insert("connections", null, values);
			Log.i("inserted in chat", l+"  z");
		}
		
		for(int i=0; i<vecConnections.size(); i++)
		{
			User u= vecConnections.get(i);
			ContentValues values= new ContentValues();
			values.put("uid", u.userId+"");
			values.put("uname", u.userName+"");
			values.put("utype", "0");
			values.put("phone", u.phone+"");
			values.put("email", u.email+"");
			values.put("profile_image", u.profile_image_url+"");
			long l=_database.insert("connections", null, values);
			Log.i("inserted in chat", l+"  z");
		}
		
		for(int i=0; i<vecPhoneBook.size(); i++)
		{
			User u= vecPhoneBook.get(i);
			ContentValues values= new ContentValues();
			values.put("phone", u.phone+"");
			values.put("uid", u.userId+"");
			values.put("utype", "2");
			values.put("profile_image", u.profile_image_url+"");
			values.put("uname", u.userName+"");
			values.put("email", u.email+"");
			if(u.isRequestRequired)
				values.put("isRequestReq", "YES");
			else
				values.put("isRequestReq", "NO");
			long l=_database.insert("connections", null, values);
			Log.i("inserted in chat", l+"  z");
		}
	}

	@Override
	public void stateChanged(Chat chat, ChatState state) {
		Log.i("Chat state is recceived", "State changed");
	
		if(state.equals(ChatState.composing))
		{
			Log.i("User is Typing","Received");
				   if(messageListener!= null)
					   messageListener.userTyping();
		}
		else if(state.equals(ChatState.paused))
		{
			Log.i("User is paused","Received");
				   if(messageListener!= null)
					   messageListener.pausedTyping();
		}
		
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		Log.i("Somebody created Chat", "caht created");
		chat.addMessageListener(this);
	}
	
	
	public String getTime()
	{
		 Long tsLong = System.currentTimeMillis();
         return tsLong.toString();
	}

	public String getDateFromLong(String longV)
	{
		if(longV!= null && longV.length()>0)
		{
		try
		{
		 SimpleDateFormat formatter = new SimpleDateFormat(" hh:mm");
		 long millisecond = Long.valueOf(longV);
		    // Create a calendar object that will convert the date and time value in milliseconds to date. 
		     Calendar calendar = Calendar.getInstance();
		     calendar.setTimeInMillis(millisecond);
		     return formatter.format(calendar.getTime());
		}
		catch(NumberFormatException ee)
		{
			return"not available";
		}
		}
		else
		{
			return "not available";
		}
	}
	
	public class UploadCompletionReceiver extends BroadcastReceiver
	{
		public static final String ACTION_RESP =    
			      "com.mamlambo.intent.action.MESSAGE_UPlOAD";
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Receiver called", "Callback of upload completion");
		    
			Bundle b=  intent.getExtras().getBundle("bundle");
			String tid= b.getString("tid");
			String gpath= b.getString("gpath");
			String type= b.getString("type");
			
			String duration= b.getString("duration");
			String size= b.getString("size");
			//Update global path in media
			//Send chat message
			if(type.equalsIgnoreCase("3"))
			{
				String name= b.getString("name");
				String phone= b.getString("phone");
				sendVCard(tid, gpath, name, phone);
			}
			else
			sendMutiMediaMessage(tid, gpath, duration, size);
			//Update chat history status
			//Remove from taskManager
			removeFromTask(tid);
		}
	}
	
	public class DownloadCompletionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Receiver called", "Callback of download completion");
			String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
            {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Query query = new Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
	                if (c.moveToFirst())
	                {
	                    int columnIndex = c
	                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
	                    int status= c.getInt(columnIndex);
	                    
	                    DictionaryEntry [][] data= getTask(hashDownload.get(downloadId));
	                    if(data!= null)
	                    {
		            		String msg_id=data[0][1].value.toString();
		                    if (DownloadManager.STATUS_SUCCESSFUL == status) {
		                    	
		                    	updateMessageStatus(msg_id, "0");
		                    	removeFromTask(hashDownload.get(downloadId));
		                    	hashDownload.remove(downloadId);
		                 		   if(messageListener!= null)
		                 			   messageListener.onMessageReceived();
		                    }
		                    else
		                    {
		                    	//Fail  COLUMN_REASON
		  	                    int Reason= c
		  	                            .getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
		  	                    Log.i("error", Reason+"");
		  	                  updateMessageStatus(msg_id, "-10");
		                    }
	                    }
	                }
            	}
			}
		}

	@Override
	public void entriesAdded(Collection<String> arg0) {
		
	}

	@Override
	public void entriesDeleted(Collection<String> arg0) {
		
	}

	@Override
	public void entriesUpdated(Collection<String> arg0) {
		
	}

	@Override
	public void presenceChanged(Presence arg0) 
	{
		  if(currentUserUid!= null && arg0.getFrom().split("@")[0].equalsIgnoreCase(currentUserUid))
		   {
			  if(arg0.getType().equals(Presence.Type.unavailable))
			  {
				  String resource="";
	            	String from= arg0.getFrom();
	            	if (from != null && from.lastIndexOf("/") > 0) {
	                    resource = from.substring(from.lastIndexOf("/") + 1);
	                    // from here you can track all active resources
	                }
	            	if(resource.startsWith("Smack"))
	            		getLastSeen(arg0.getFrom().split("@")[0],resource);
	            	else
	            		getLastSeen(arg0.getFrom().split("@")[0]);
			  }
			  else
			  {
			   if(messageListener!= null)
				   messageListener.onPresenseReceived(arg0.getType().name());
			  }
		   }
	}

	@Override
	public void invitationReceived(Connection arg0, String room, String inviter, String reason, String password, Message arg5) {
		Log.i("MUC invitation received", "Good to go with this");
		MultiUserChat muc= new MultiUserChat(connection, room);
		 try {
			muc.join("testbot2");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		 muc.addMessageListener(this);
	}

	@Override
	public void subjectUpdated(String arg0, String arg1) {
		
	}

	
	private void addTOGroup(MultiUserChat muc)
	{
		String name= muc.getRoom();
		String status= muc.getSubject();
		String a= muc.getNickname();
		try {
			RoomInfo b= muc.getRoomInfo(connection, muc.getRoom());
			
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addToUnread(String from)
	{
		
	}


	
}
