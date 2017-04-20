package com.logicnext.bizmo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bizmo.engine.ChatEngine;
import com.bizmo.interfaces.OnCurrentUserMessageLitener;
import com.logicnext.loaders.ChatLoader;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import com.ui.common.Constants;

public class ChatActivity extends Activity implements OnClickListener, LoaderCallbacks<Cursor>  , OnCurrentUserMessageLitener
{
	 LayoutInflater inflator;
	 
	 ImageView ivAddMedia, ivSmilies, ivSend, ivInviteUsers;
	 EditText edtMessage;
	 ListView lvChat;
	 private String UId, Uname, MyUid;
	 RelativeLayout rlFooter;
	 ChatAdapter mAdapter;
	 ChatEngine chatEngine;
	 
	 TextView tvHeader, tvStatus;
	 boolean isGroupChat;
	LinearLayout llMain;
	 
		public SharedPreferences myUserPrefs;
		public SharedPreferences.Editor myUserEdit;
		
	 private final int CAPTURE_IMAGE= 100;
	 private final int CAPTURE_VIDEO= 101;
	 private final int PICK_IMAGE= 102;
	 private final int PICK_VIDEO= 103;
	 private final int PICK_CONTACT= 104;
	 private final int CAPTURE_LOCATION= 105;
	 
	 File cameraFile;
	 
	  protected ImageLoader imageLoader = ImageLoader.getInstance();
	  DisplayImageOptions options;
		
	 	private void setImageLoader()
	 	{
	 		options = new DisplayImageOptions.Builder()
	         .cacheInMemory(true) // default
	         .cacheOnDisc(true) // default
	          .displayer(new RoundedBitmapDisplayer(6)) // default
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
		protected void onCreate(Bundle savedInstanceState) 
		{
			super.onCreate(savedInstanceState);
			
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			
			  setContentView(R.layout.chat);
			  myUserPrefs = getSharedPreferences("bizmo", Context.MODE_PRIVATE);
			  myUserEdit =  myUserPrefs.edit();
			inflator= getLayoutInflater();
			setUi();
			setImageLoader();
			
			chatEngine= (ChatEngine)getApplication();
			UId= getIntent().getExtras().getString("uid");
			Uname= getIntent().getExtras().getString("uname");
			MyUid= myUserPrefs.getString(Constants.USERID, "");
			getLoaderManager().initLoader(0, null, this);
			mAdapter= new ChatAdapter(this,  null,true);
			lvChat.setAdapter(mAdapter);
			
			chatEngine.setCurrentUserListener(this, UId);
			tvHeader.setText(Uname);
		
			scrollMyListViewToBottom();
			
		
			chatEngine.getPresence(UId);
		}
		
		private void setUi()
		{
			llMain= (LinearLayout)findViewById(R.id.llMain);
			rlFooter= (RelativeLayout)findViewById(R.id.rlFooter);
			ivAddMedia= (ImageView)findViewById(R.id.ivAddItemToChat);
			ivSmilies= (ImageView)findViewById(R.id.ivSmilieys);
			ivInviteUsers= (ImageView)findViewById(R.id.ivInviteUser);
			ivSend= (ImageView)findViewById(R.id.ivSendMessage);
			edtMessage= (EditText)findViewById(R.id.edtMessage);
			lvChat= (ListView)findViewById(R.id.lvChat);
			tvHeader= (TextView)findViewById(R.id.tvHeader);
			tvStatus= (TextView)findViewById(R.id.tvUserStatus);
			smileysCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

			smileyPopupView = inflator.inflate(R.layout.smiley_popup_view, null);
			
			ivSend.setOnClickListener(this);
			ivAddMedia.setOnClickListener(this);
			ivSmilies.setOnClickListener(this);
			ivInviteUsers.setOnClickListener(this);
			
			
			edtMessage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (smileyPopup.isShowing()) {
						smileyPopup.dismiss();
					}
				}
			});
			
			edtMessage.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					
					if(s.toString().length()>0)
					{
						ivSend.setImageResource(R.drawable.chat_send_icon);
					}
					else
					{
						ivSend.setImageResource(R.drawable.chat_call_icon);
					}
				}
			});
			
			
			setSmileyUi();
		}
		
		
		@Override
		public void onClick(View v) {
			
			if(v== ivSend)
			{
				if(edtMessage.getText().toString().trim().length()>0)
				{
					if(isGroupChat)
					{
						
					}
					else
					{
						chatEngine.sendTextMessage(edtMessage.getText().toString().trim(), UId+"@"+ChatEngine.ServerAddress,  MyUid+"@"+ChatEngine.ServerAddress, "0");
					}
					
					edtMessage.setText("");
					getLoaderManager().restartLoader(0, null, this);
					scrollMyListViewToBottom();
				}
			}
			else if(v== ivAddMedia)
			{
				if (!smileyPopup.isShowing()) 
				{
					smileyPopup.setHeight((int) (keyboardHeight));
					if (isKeyBoardVisible) {
						smileysCover.setVisibility(LinearLayout.GONE);
					} else {
						smileysCover.setVisibility(LinearLayout.VISIBLE);
					}
					smileyPopup.showAtLocation(llMain, Gravity.BOTTOM, 0, 0);
					ivAddMedia.setImageResource(R.drawable.chat_keyboard_icon);
				} else {
					smileyPopup.dismiss();
				}
			}
			else if(v== ivInviteUsers)
			{
				Intent in= new Intent(this, ChooseMemberActivity.class);
				in.putExtra("isGroup", true);
				startActivity(in);
			}
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new ChatLoader(this, chatEngine, UId);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
		}
		
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if(resultCode== RESULT_OK)
			{
				if(requestCode== CAPTURE_IMAGE)
				{
					sendMediaMessage(cameraFile.getAbsolutePath(), 0, "", "");
				}
				else if(requestCode== CAPTURE_VIDEO || requestCode== PICK_VIDEO)
				{
					Uri selectedImage = data.getData();
					String[] filePathColumn = {
							MediaStore.Video.Media.DISPLAY_NAME,
							MediaStore.Video.Media.DATA,MediaStore.Video.Media.DURATION };
					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null,null, null);
					cursor.moveToFirst();

					int address = cursor.getColumnIndex(filePathColumn[1]);
					int dur = cursor.getColumnIndex(filePathColumn[2]);

					String filePath = cursor.getString(address);
					String duration = convertToTimeFormat(cursor.getLong(dur));
					cursor.close();

					File f= new File(filePath);
					long size=f.length();
					
					
					
					sendMediaMessage(filePath, 2, duration,size+"" );
				}
				else if(requestCode== PICK_IMAGE)
				{
					Uri selectedImage = data.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };
					Cursor cursor = this.getContentResolver().query(selectedImage,
							filePathColumn, null, null, null);
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					final String filePath = cursor.getString(columnIndex);
					cursor.close();
					
					sendMediaMessage(filePath, 0, "", "");
				}
				else if(requestCode== PICK_CONTACT)
				{
					Uri contactUri = data.getData();
					Cursor phones = getContentResolver().query(
				            contactUri, null, null,
				            null, null);
					phones.moveToFirst();
					
					int column = phones.getColumnIndex(Phone.NUMBER);
		            String number = phones.getString(column);
		            
		            
		            int columnNames = phones.getColumnIndex(Phone.DISPLAY_NAME);
		            String name = phones.getString(columnNames);
					
					String lookupKey = phones.getString(phones
			                .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

			        Uri uri = Uri.withAppendedPath(
			                ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
			        AssetFileDescriptor fd;
			        try {
			            fd = getContentResolver().openAssetFileDescriptor(uri,
			                    "r");
			            FileInputStream fis = fd.createInputStream();
			            byte[] buf = new byte[(int) fd.getDeclaredLength()];
			            fis.read(buf);
			            String VCard = new String(buf);
			            fis.close();
			            String path = ChatEngine.FolderPath+ Constants.VCFPath+chatEngine.getTime()+".vcf";
			            
			            File f= new File(ChatEngine.FolderPath+ Constants.VCFPath);
			            
			            if(!f.exists())
			            	f.mkdirs();
			            
			            FileOutputStream mFileOutputStream = new FileOutputStream(path,
			                    true);
			            mFileOutputStream.write(VCard.toString().getBytes());
			            mFileOutputStream.close();
			            phones.close();
			            
			            
			            //Send To chat Enginr for uploading
			            Log.d("Vcard", VCard);
			            sendVCard(path, name, number);
			            
			        } catch (Exception e1) {
			            e1.printStackTrace();
			        }
				}
				else if(requestCode== CAPTURE_LOCATION)
				{
					String address= data.getExtras().getString("address");
					String lat= data.getExtras().getDouble("lat")+"";
					String lng= data.getExtras().getDouble("lng")+"";
					
					if(isGroupChat)
					{
						
					}
					else
					{
						chatEngine.sendLocation(lat, lng, address, UId+"@"+ChatEngine.ServerAddress,  MyUid+"@"+ChatEngine.ServerAddress, "0");
					}
					
					getLoaderManager().restartLoader(0, null, this);
					scrollMyListViewToBottom();
				}
			}
		}
		
		private void sendMediaMessage(String path, int type, String duration , String size)
		{
			if(isGroupChat)
			{
				
			}
			else
			{
				chatEngine.addMultiMediaMessage(UId+"@"+ChatEngine.ServerAddress,  MyUid+"@"+ChatEngine.ServerAddress, path, type, duration, size, "0");
			}
			
			getLoaderManager().restartLoader(0, null, this);
			scrollMyListViewToBottom();
		}
		
		private void sendVCard(String path, String name, String phone)
		{
			if(isGroupChat)
			{
				
			}
			else
			{
				chatEngine.addVCard(UId+"@"+ChatEngine.ServerAddress,  MyUid+"@"+ChatEngine.ServerAddress, path, name, phone , "0");
			}
			
			getLoaderManager().restartLoader(0, null, this);
			scrollMyListViewToBottom();
		}
		
		private String convertToTimeFormat(long millis)
		{
			String hms = "00";
			hms = String.format("%02d:%02d:%02d", 
					TimeUnit.MILLISECONDS.toHours(millis),
					TimeUnit.MILLISECONDS.toMinutes(millis) -  
					TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
					TimeUnit.MILLISECONDS.toSeconds(millis) - 
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
			return hms;
		}
		
		private class ChatAdapter extends CursorAdapter
		{
			public class ViewHolder {
				TextView tvLeft, tvRight;
				TextView tvDate;
				ImageView ivleft, ivRight, ivDelieverystatus, ivRelodLeft, ivReloadRight;
				ProgressBar prgLeft, prgRight;
				RelativeLayout rlLeft, rlRight;
			}
			public ChatAdapter(Context context, Cursor c, Boolean flags) {
				super(context, c, flags);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) 
			{
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				String msg  = cursor.getString(cursor.getColumnIndex("message_text"));
				String msg_id  = cursor.getString(cursor.getColumnIndex("_id"));
				String msg_time  = cursor.getString(cursor.getColumnIndex("message_time"));
				String msg_owner  = cursor.getString(cursor.getColumnIndex("message_owner"));
				String msg_to  = cursor.getString(cursor.getColumnIndex("message_to"));
				String msg_mode  = cursor.getString(cursor.getColumnIndex("messgae_mode"));
				
				try
				{
					JSONObject msg_obj = new JSONObject(msg);
					String type= msg_obj.getString("type");
					if(msg_owner.equals(MyUid))
					{
						//Show Right Layout
						viewHolder.rlLeft.setVisibility(View.GONE);
						viewHolder.rlRight.setVisibility(View.VISIBLE);
						viewHolder.ivDelieverystatus.setVisibility(View.VISIBLE);
						
						viewHolder.rlRight.setTag(msg_id);
						if(type.equals(Constants.MSG_TYPE_TEXT))
						{
							viewHolder.tvRight.setVisibility(View.VISIBLE);
							viewHolder.ivRight.setVisibility(View.GONE);
							viewHolder.ivReloadRight.setVisibility(View.GONE);
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.tvRight.setText(msg_obj.getString("message_text"));
							viewHolder.tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
							viewHolder.tvRight.setCompoundDrawablePadding(10);
						}
						else if(type.equals(Constants.MSG_TYPE_IMAGE))
						{
							viewHolder.tvRight.setVisibility(View.GONE);
							viewHolder.ivRight.setVisibility(View.VISIBLE);
							viewHolder.ivReloadRight.setVisibility(View.GONE);
							viewHolder.prgRight.setVisibility(View.GONE);
							String mid  = cursor.getString(cursor.getColumnIndex("mid"));
							String mpath=chatEngine.getMediapath(mid);
							imageLoader.displayImage("file://"+mpath, viewHolder.ivRight, options);
							//Image Message
						}
						else if(type.equals(Constants.MSG_TYPE_AUDIO))
						{
							//Audio message
							viewHolder.tvRight.setVisibility(View.VISIBLE);
							viewHolder.ivRight.setVisibility(View.GONE);
							viewHolder.ivReloadRight.setVisibility(View.GONE);
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.tvRight.setText(msg_obj.getString("message_text")+"\n"+ msg_obj.getString("message_media_duration"));
							viewHolder.tvRight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.chat_audio, 0, 0, 0);
							viewHolder.tvRight.setCompoundDrawablePadding(10);
						}
						else if(type.equals(Constants.MSG_TYPE_VCARD))
						{
							//VCard message
							viewHolder.tvRight.setVisibility(View.VISIBLE);
							viewHolder.ivRight.setVisibility(View.GONE);
							viewHolder.ivReloadRight.setVisibility(View.GONE);
							viewHolder.prgRight.setVisibility(View.GONE);
							
							String mid  = cursor.getString(cursor.getColumnIndex("mid"));
							//String mpath=chatEngine.getMediapath(mid);
							viewHolder.tvRight.setText(msg_obj.getString("name"));
							viewHolder.tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_vc, 0);
							viewHolder.tvRight.setCompoundDrawablePadding(10);
						}
						else if(type.equals(Constants.MSG_TYPE_VIDEO))
						{
							//Video message
							viewHolder.tvRight.setVisibility(View.GONE);
							viewHolder.ivRight.setVisibility(View.VISIBLE);
							viewHolder.ivReloadRight.setVisibility(View.VISIBLE);
							
							viewHolder.prgRight.setVisibility(View.GONE);
							
							viewHolder.ivReloadRight.setImageResource(R.drawable.chat_video);
							String mid  = cursor.getString(cursor.getColumnIndex("mid"));
							String mpath=chatEngine.getMediapath(mid);
							String thumbpath= getVideoThumb(mpath);
							Log.i("media path", mpath);
							imageLoader.displayImage("file://"+thumbpath, viewHolder.ivRight, options);
						}
						else if(type.equals(Constants.MSG_TYPE_MAP))
						{
							//Video message
							viewHolder.tvRight.setVisibility(View.VISIBLE);
							viewHolder.ivRight.setVisibility(View.GONE);
							viewHolder.ivReloadRight.setVisibility(View.GONE);
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.tvRight.setText(msg_obj.getString("loc"));
							viewHolder.tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_location, 0);
						}
						String msg_status  = cursor.getString(cursor.getColumnIndex("message_status"));
						
						if(msg_status.equals("0"))
						{
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.ivDelieverystatus.setImageResource(R.drawable.single_tick);
						}
						else if(msg_status.equals("1"))
						{
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.ivDelieverystatus.setImageResource(R.drawable.double_tick);
						}
						else if(msg_status.equals("-1"))
						{
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.ivDelieverystatus.setVisibility(View.GONE);
						}
						else if(msg_status.equals("-5"))
						{
							viewHolder.prgRight.setVisibility(View.VISIBLE);
							viewHolder.ivDelieverystatus.setImageResource(0);
						}
						else if(msg_status.equals("-10"))
						{
							viewHolder.prgRight.setVisibility(View.GONE);
							viewHolder.ivReloadRight.setVisibility(View.VISIBLE);
							viewHolder.ivReloadRight.setImageResource(R.drawable.reload_button);
							viewHolder.ivDelieverystatus.setImageResource(0);
						}
					}
					else
					{
						viewHolder.rlLeft.setVisibility(View.VISIBLE);
						viewHolder.rlRight.setVisibility(View.GONE);
						viewHolder.ivDelieverystatus.setVisibility(View.GONE);
						viewHolder.rlLeft.setTag(msg_id);
						if(type.equals(Constants.MSG_TYPE_TEXT))
						{
							viewHolder.tvLeft.setVisibility(View.VISIBLE);
							viewHolder.ivleft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.GONE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							viewHolder.tvLeft.setText(msg_obj.getString("message_text"));
							viewHolder.tvLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
						}
						else if(type.equals(Constants.MSG_TYPE_IMAGE))
						{
							//Image Message
							
							viewHolder.tvLeft.setVisibility(View.GONE);
							viewHolder.ivleft.setVisibility(View.VISIBLE);
							viewHolder.ivRelodLeft.setVisibility(View.GONE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							String mid  = cursor.getString(cursor.getColumnIndex("mid"));
							
							String mpath=chatEngine.getMediapath(mid);
							Log.i("media path", mpath);
							imageLoader.displayImage("file://"+mpath, viewHolder.ivleft, options);
						}
						else if(type.equals(Constants.MSG_TYPE_AUDIO))
						{
							viewHolder.tvLeft.setVisibility(View.VISIBLE);
							viewHolder.ivleft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.GONE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							
							viewHolder.tvLeft.setText(msg_obj.getString("message_text")+"\n"+ msg_obj.getString("message_media_duration"));
							viewHolder.tvLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.chat_audio, 0, 0, 0);
							viewHolder.tvLeft.setCompoundDrawablePadding(10);
							//Audio message
						}
						else if(type.equals(Constants.MSG_TYPE_VCARD))
						{
							//VCard message
							viewHolder.tvLeft.setVisibility(View.VISIBLE);
							viewHolder.ivleft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.GONE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							viewHolder.tvLeft.setText(msg_obj.getString("name"));
							viewHolder.tvLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.chat_vc , 0, 0, 0);
							viewHolder.tvLeft.setCompoundDrawablePadding(10);
						}
						else if(type.equals(Constants.MSG_TYPE_VIDEO))
						{
							viewHolder.tvLeft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.VISIBLE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setImageResource(R.drawable.chat_video);
							String mid  = cursor.getString(cursor.getColumnIndex("mid"));
							viewHolder.ivleft.setVisibility(View.VISIBLE);
							String mpath=chatEngine.getMediapath(mid);
							String thumbpath= getVideoThumb(mpath);
							Log.i("media path", mpath);
							imageLoader.displayImage("file://"+thumbpath, viewHolder.ivleft, options);
							//Video message
						}
						else if(type.equals(Constants.MSG_TYPE_MAP))
						{
							//Video message
							viewHolder.tvLeft.setVisibility(View.VISIBLE);
							viewHolder.ivleft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.GONE);
							viewHolder.prgLeft.setVisibility(View.GONE);
							viewHolder.tvLeft.setText(msg_obj.getString("loc"));
							viewHolder.tvLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.chat_location , 0, 0, 0);
							viewHolder.tvLeft.setCompoundDrawablePadding(10);
						}
						
						String msg_status  = cursor.getString(cursor.getColumnIndex("message_status"));
						
				       if(msg_status.equals("-5"))
						{
							viewHolder.prgLeft.setVisibility(View.VISIBLE);
						}
						else if(msg_status.equals("-10"))
						{
							viewHolder.prgLeft.setVisibility(View.GONE);
							viewHolder.ivRelodLeft.setVisibility(View.VISIBLE);
							viewHolder.ivRelodLeft.setImageResource(R.drawable.reload_button);
						}
						else
						{
							viewHolder.prgLeft.setVisibility(View.GONE);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				viewHolder.tvDate.setText(chatEngine.getDateFromLong(msg_time));
				
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) 
			{
				View view= (RelativeLayout)inflator.inflate(R.layout.chat_strip, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.tvLeft = (TextView) view
						.findViewById(R.id.tvLeftChatMessage);
				viewHolder.tvRight = (TextView) view
						.findViewById(R.id.tvRightChatMessage);
				viewHolder.tvDate = (TextView) view
						.findViewById(R.id.tvChatTime);
				viewHolder.ivleft = (ImageView) view
						.findViewById(R.id.ivLeft);
				viewHolder.ivRight = (ImageView) view
						.findViewById(R.id.ivRight);
				viewHolder.rlLeft = (RelativeLayout) view	.findViewById(R.id.rlLeft);
				viewHolder.rlRight = (RelativeLayout) view.findViewById(R.id.rlRight);
				viewHolder.ivRelodLeft = (ImageView) view
						.findViewById(R.id.ivRetryLeft);
				viewHolder.ivReloadRight = (ImageView) view
						.findViewById(R.id.ivRetryRight);
				viewHolder.prgLeft = (ProgressBar) view	.findViewById(R.id.prgLeft);
				viewHolder.prgRight = (ProgressBar) view.findViewById(R.id.prgRight);
				viewHolder.ivDelieverystatus = (ImageView) view
						.findViewById(R.id.ivDelievery);
				view.setTag(viewHolder);
				
				viewHolder.rlLeft.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View v) {
						String tag= v.getTag().toString();
						handleMessageClick(tag);
					}
				});
				
				viewHolder.rlLeft.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						String tag= v.getTag().toString();
						Toast.makeText(getApplicationContext(), "longpress   "+tag, Toast.LENGTH_LONG).show();
						return true;
					}
				});
				
				viewHolder.rlRight.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						String tag= v.getTag().toString();
						Toast.makeText(getApplicationContext(), "Long press "+tag, Toast.LENGTH_LONG).show();
						return true;
					}
				});
				viewHolder.rlRight.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						String tag= v.getTag().toString();
						handleMessageClick(tag);
					}
				});
				return view;
			}
			
		}
		
		@Override
		public void onMessageReceived() 
		{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					getLoaderManager().restartLoader(0, null, ChatActivity.this);
					scrollMyListViewToBottom();
				}
			});
		}
		
		
		@Override
		public void userTyping() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					tvStatus.setText("Typing");
				}
			});
			
		}
		
		@Override
		public void pausedTyping() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					tvStatus.setText("OnLine");
				}
			});
		}
		
		@Override
		public void onPresenseReceived(final String presence) {
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tvStatus.setText(presence);
				}
			});
		}
		
		
		private void handleMessageClick(String msgId)
		{
			
			String message = chatEngine.getMessage(msgId);
			if(msgId!= null)
			{
				JSONObject onj;
				try {
					onj = new JSONObject(message);
					String type= onj.getString("type");
					if(type.equalsIgnoreCase(Constants.MSG_TYPE_IMAGE))
					{
						String mid= chatEngine.getMidOfMssage(msgId);
						String mpath=chatEngine.getMediapath(mid);
						Intent intent = new Intent();  
						intent.setAction(android.content.Intent.ACTION_VIEW);  
						File file = new File(mpath);  
						intent.setDataAndType(Uri.fromFile(file), "image/*");  
						startActivity(intent);
					}
					else if(type.equalsIgnoreCase(Constants.MSG_TYPE_AUDIO))
					{
						String mid= chatEngine.getMidOfMssage(msgId);
						String mpath=chatEngine.getMediapath(mid);
						Intent intent = new Intent();  
						intent.setAction(android.content.Intent.ACTION_VIEW);  
						File file = new File(mpath);  
						intent.setDataAndType(Uri.fromFile(file), "audio/*");  
						startActivity(intent);
					}
					else if(type.equalsIgnoreCase(Constants.MSG_TYPE_VIDEO))
					{
						String mid= chatEngine.getMidOfMssage(msgId);
						String mpath=chatEngine.getMediapath(mid);
						Intent intent = new Intent();  
						intent.setAction(android.content.Intent.ACTION_VIEW);  
						File file = new File(mpath);  
						intent.setDataAndType(Uri.fromFile(file), "video/*");  
						startActivity(intent);
						
					}else if(type.equalsIgnoreCase(Constants.MSG_TYPE_VCARD))
					{
						String mid= chatEngine.getMidOfMssage(msgId);
						String mpath=chatEngine.getMediapath(mid);
						Intent i = new Intent();
						i.setAction(android.content.Intent.ACTION_VIEW);
						i.setDataAndType(Uri.parse(mpath), "text/x-vcard");
						startActivity(i);
					}
					else if(type.equalsIgnoreCase(Constants.MSG_TYPE_MAP))
					{
						double lat= Double.parseDouble(onj.getString("lat"));
						double lng= Double.parseDouble(onj.getString("lng"));
						Intent in= new Intent(this, MapActivity.class);
						in.putExtra("isView", true);
						in.putExtra("lat", lat);
						in.putExtra("lng", lng);
						startActivity(in);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}
		
		private void scrollMyListViewToBottom() {
		    lvChat.post(new Runnable() {
		        @Override
		        public void run() {
		            // Select the last row so it will scroll into view...
		            lvChat.setSelection(mAdapter.getCount() - 1);
		        }
		    });
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			
		//	chatEngine.setLastSeen(MyUid);
			getLoaderManager().destroyLoader(0);
			chatEngine.setCurrentUserListener(null, null);
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
		
		@Override
		protected void onPause() {
			super.onPause();
			chatEngine.setCurrentUserListener(null, null);
		}
		
		@Override
		protected void onResume() {
			super.onResume();
			if(UId== null)
				finish();
			else
			{
					if(chatEngine.connection != null &&chatEngine.connection.isConnected() && chatEngine.connection.isAuthenticated())
					{
						Presence presence = new Presence(Presence.Type.available);
						chatEngine.connection.sendPacket(presence);
						chatEngine.setCurrentUserListener(this, UId);
					}
					
			}
				
		}
		
		
		/*********************************************************************************************************************************************************
		 * Smiley Code
		 */
		
		private PopupWindow smileyPopup;
		private GridView gvAttachmnet;
		RelativeLayout rlRecord;
		ImageView ivRecordAudio;
		TextView tvTimer;
		private int keyboardHeight;	
		private View smileyPopupView;
		private LinearLayout smileysCover;
		private boolean isKeyBoardVisible;

		private void setSmileyUi()
		{
			lvChat.setOnTouchListener(new OnTouchListener() 
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (smileyPopup.isShowing())
						smileyPopup.dismiss();	
					return false;
				}
			});
			// Defining default height of keyboard which is equal to 230 dip
			final float popUpheight = getResources().getDimension(
					R.dimen.keyboard_height);
			changeKeyboardHeight((int) popUpheight);
			enablePopUpView();
			checkKeyboardHeight(llMain);
		}
		
		
		/**
		 * Checking keyboard height and keyboard visibility
		 */
		int previousHeightDiffrence = 0;
		private com.logicnext.bizmo.AudioRecorder recorder;
		private void checkKeyboardHeight(final View parentLayout) {

			parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							
							Rect r = new Rect();
							parentLayout.getWindowVisibleDisplayFrame(r);
							
							int screenHeight = parentLayout.getRootView()
									.getHeight();
							int heightDifference = screenHeight - (r.bottom);
							
							if (previousHeightDiffrence - heightDifference > 50) {							
								smileyPopup.dismiss();
							}
							
							previousHeightDiffrence = heightDifference;
							if (heightDifference > 100) {

								isKeyBoardVisible = true;
								changeKeyboardHeight(heightDifference);

							} else {

								isKeyBoardVisible = false;
								
							}

						}
					});

		}

		/**
		 * change height of emoticons keyboard according to height of actual
		 * keyboard
		 * 
		 * @param height
		 *            minimum height by which we can make sure actual keyboard is
		 *            open or not
		 */
		private void changeKeyboardHeight(int height) {

			if (height > 100) {
				keyboardHeight = height;
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, keyboardHeight);
				smileysCover.setLayoutParams(params);
			}
		}
		/**
		 * Defining all components of emoticons keyboard
		 */
		private void enablePopUpView() {
			// Creating a pop window for emoticons keyboard
			smileyPopup = new PopupWindow(smileyPopupView, LinearLayout.LayoutParams.MATCH_PARENT,
					(int) keyboardHeight, false);
			
			gvAttachmnet= (GridView)smileyPopupView.findViewById(R.id.GvAttachment);
			ivRecordAudio= (ImageView)smileyPopupView.findViewById(R.id.ivRecoedAudio);
			tvTimer= (TextView)smileyPopupView.findViewById(R.id.tvRecorcTime);
			rlRecord= (RelativeLayout)smileyPopupView.findViewById(R.id.rlAudioRecorder);
			gvAttachmnet.setAdapter(new AttachMentAdapter());
			
			
			ivRecordAudio.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch (event.getAction()) {
					case  MotionEvent.ACTION_DOWN:
						
						ivRecordAudio.setImageResource(R.drawable.voice_rec_button_pressed);
						startRecord();
						startTimer();
						break;

					case  MotionEvent.ACTION_UP:
						
						if(inViewBounds(v,(int) event.getRawX() , (int) event.getRawY()))
						{
							stopRecord();
							ivRecordAudio.setImageResource(R.drawable.voice_rec_button);
						}
						else
						{
							cancelRecord();
							ivRecordAudio.setImageResource(R.drawable.voice_rec_button);
						}
						break;
					default:
						break;
					}
					return true;
				}
			});
			
			//TextView backSpace = (TextView) smileyPopupView.findViewById(R.id.back);
//			backSpace.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
//					edtMessage.dispatchKeyEvent(event);	
//				}
//			});
			smileyPopup.setOnDismissListener(new OnDismissListener() 
			{
				@Override
				public void onDismiss() {
					
					ivAddMedia.setImageResource(R.drawable.chat_plus_icon);
					smileysCover.setVisibility(LinearLayout.GONE);
				}
			});
		}
		
		  
		  private boolean inViewBounds(View view, int x, int y){
			  
			    Rect outRect = new Rect();
			    int[] location = new int[2];
		        view.getDrawingRect(outRect);
		        view.getLocationOnScreen(location);
		        outRect.offset(location[0], location[1]);
		        return outRect.contains(x, y);
		    }
		
		private class AttachMentAdapter extends BaseAdapter
		{
			@Override
			public int getCount() {
				return 7;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				if(convertView== null)
				{
					convertView= (LinearLayout)inflator.inflate(R.layout.attach_item_grid, null);
				}
				
				TextView tv= (TextView)convertView.findViewById(R.id.tvattachment);
				
				tv.setTag(position);
				
				switch (position) {
				case 0:
					tv.setText(R.string.choose_photo);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_image, 0,0);
					
					break;
				case 1:
					tv.setText(R.string.camera);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_camera, 0,0);
					break;
				case 2:
					tv.setText(R.string.choose_video);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_videos, 0,0);
					break;
				case 3:
					tv.setText(R.string.video);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_video, 0,0);
					break;
				case 4:
					tv.setText(R.string.voice_message);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_audio, 0,0);
					break;
				case 5:
					tv.setText(R.string.contacts);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_contacts, 0,0);
					break;
				case 6:
					tv.setText(R.string.my_location);
					tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.attachment_location, 0,0);
					break;

				default:
					break;
				}
				
				tv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						int pos= Integer.parseInt(v.getTag().toString());
						Toast.makeText(ChatActivity.this, "working " + pos, Toast.LENGTH_SHORT).show();
			            
			        	Intent intent = new Intent();
						switch (pos) 
						{
						case 0:
							intent.setAction(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							startActivityForResult(intent, PICK_IMAGE);
							break;
						case 1:
							Intent cameraIntent = new Intent(
									android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
							
							String path= ChatEngine.FolderPath+Constants.ImagePath;
							File f= new File(path);
							if(!f.exists())
								f.mkdirs();
							
							cameraFile = new File(ChatEngine.FolderPath+Constants.ImagePath + "IMG_"
									+ chatEngine.getTime() + ".jpeg");
							
							cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(cameraFile));
							cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,549152L); //.5*1048*1048=12MB
							startActivityForResult(cameraIntent, CAPTURE_IMAGE);
							break;
						case 2:
							intent.setAction(Intent.ACTION_GET_CONTENT);
							intent.setType("video/*");
							startActivityForResult(intent, PICK_VIDEO);
							break;
						case 3:
							
							Intent videoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
							videoIntent.putExtra("android.intent.extra.durationLimit", 30);
							videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
							startActivityForResult(videoIntent, CAPTURE_VIDEO);
							break;
						case 4:
							rlRecord.setVisibility(View.VISIBLE);
							break;
						case 5:
							 Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
							    pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
							    startActivityForResult(pickContactIntent, PICK_CONTACT);
							break;					
						case 6:
							Intent in= new Intent(ChatActivity.this, MapActivity.class);
							in.putExtra("isView", false);
							startActivityForResult(in, CAPTURE_LOCATION);
								break;
						default:
							break;
						}
					}
				});
				return convertView;
			}
		}
		public void startRecord() 
		{
			Long tsLong = System.currentTimeMillis() / 1000;
			String ts = tsLong.toString();
			recorder = new AudioRecorder( ChatEngine.FolderPath+ Constants.AudioPath+ ts);
			try 
			{
				recorder.start();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		public void stopRecord() 
		{
			try {
				recorder.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			t.cancel();
			File f= new File(recorder.path);
			sendMediaMessage(recorder.path, 1, tvTimer.getText().toString(), f.length()+"");
			tvTimer.setText("00:00");
		}
		
		public void cancelRecord() 
		{
			
			try {
				recorder.stop();
				t.cancel();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tvTimer.setText("00:00");
		}
		
		public String getVideoThumb(String vidpath)
		{
			File f= new  File(vidpath);
			String title= f.getName();
			
			File dir= new File(ChatEngine.FolderPath+Constants.VideoThumbPath);
			if(!dir.exists())
				dir.mkdirs();
			File file= new File(ChatEngine.FolderPath+Constants.VideoThumbPath, title+".jpeg");
			if(file.exists())
			{
				return file.getAbsolutePath();
			}
			Bitmap thumb = null;
	        try
            {
            MediaMetadataRetriever meta= new MediaMetadataRetriever();
            meta.setDataSource(vidpath);
          //  thumb=meta.getFrameAtTime(-1);
            thumb= meta.getFrameAtTime();
            thumb=Bitmap.createScaledBitmap(thumb, 250, 250, false);
            meta.release();
            }
            catch(Exception ee)
            {
                    
            }
            if(thumb!= null)
            {
            	FileOutputStream outThumb = null;
				try {
					outThumb = new FileOutputStream(new File(
							ChatEngine.FolderPath+Constants.VideoThumbPath, title+".jpeg"));
					thumb.compress(Bitmap.CompressFormat.JPEG, 100, outThumb);
					outThumb.flush();
					outThumb.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				thumb.recycle();
				return file.getAbsolutePath();
            }
            else
            	return "";
		}
		
		private Timer t;
		private int TimeCounter = 0;
		
		private void startTimer()
		{
			TimeCounter=0;
			t = new Timer();
		    t.scheduleAtFixedRate(new TimerTask() {

		        @Override
		        public void run() {
		            runOnUiThread(new Runnable() {
		                public void run() {
		                	int minutes = TimeCounter / 60;
		                    int seconds     = TimeCounter % 60;
		                    tvTimer.setText(String.format("%02d:%02d", minutes, seconds)); // you can set it to a textView to show it to the user to see the time passing while he is writing.
		                    TimeCounter++;
		                }
		            });
		        }
		    }, 1000, 1000); 
		}

}
