package com.logicnext.bizmo;

import java.util.Vector;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.TextView;

import com.bizmo.engine.BizmoEngine;
import com.bizmo.object.User;
import com.logicnext.loaders.MemberLoader;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;

public class ChooseMemberActivity extends MainActivity implements  LoaderCallbacks<Cursor> 
{
	
	  LayoutInflater inflator;
	 View connectionView;
	 BizmoEngine bEngine;
	 
	 ListView lvMembers;
	 SearchView srch;
	MemberLoader memberloader;
	
	MamberAdapter mAdapter;
	
	 protected ImageLoader imageLoader = ImageLoader.getInstance();
	 DisplayImageOptions options;
	 ProgressBar prg;
	 boolean isGroup;
	 
	 LinearLayout llBottom;
	 TextView tvCancel, tvSelect;
	 
	 Vector<String> vecSelected= new Vector<String>();
	 
	 /**
	  * Set up Image loader for loading images Async
	  */
	 	private void setImageLoader()
	 	{
	 		options = new DisplayImageOptions.Builder()
	         .cacheInMemory(true) // default
	         .cacheOnDisc(true) // default
	         .bitmapConfig(Bitmap.Config.ARGB_8888) // default
	         .showImageForEmptyUri(R.drawable.connections_phone_default_user_image)//Default image
		         .showImageOnFail(R.drawable.connections_phone_default_user_image)
		         .showImageOnLoading(R.drawable.connections_phone_default_user_image)
	         .handler(new Handler()) // default
	         .build();
	 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
	 		.memoryCache(new WeakMemoryCache())
	 		.denyCacheImageMultipleSizesInMemory()
	 		
	 		.discCacheExtraOptions(250, 250, CompressFormat.JPEG, 100, null)//Size of cached images
	 		.discCache(new UnlimitedDiscCache(getCacheDir()))
	 		.imageDownloader(new BaseImageDownloader(this)) // defaut
	 		.build();
	 		L.disableLogging(); //Disable logging
	 		imageLoader.init(config);
	 	}
	 
	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		inflator= getLayoutInflater();
 		connectionView = inflator.inflate(R.layout.chhose_member, null);

 		isGroup= getIntent().getExtras().getBoolean("isGroup");
 		
 		
 		rlMain.addView(connectionView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		srch= (SearchView)connectionView.findViewById(R.id.srchMember);
 		prg= (ProgressBar)connectionView.findViewById(R.id.prgMember);
 		bEngine= BizmoEngine.getInstance();
 		
 		
 	
	
 		
 		setImageLoader();
 		lvMembers= (ListView)connectionView.findViewById(R.id.lvMembers);
 		llBottom= (LinearLayout)connectionView.findViewById(R.id.llBottom);
 		
 		tvCancel= (TextView)connectionView.findViewById(R.id.tvCancel);
 		tvSelect= (TextView)connectionView.findViewById(R.id.tvSelect);
 		
 		if(isGroup)
 			llBottom.setVisibility(View.VISIBLE);
 		else
 			llBottom.setVisibility(View.GONE);
 		
 		ivInviteUser.setVisibility(View.GONE);
		tvHeader.setText(R.string.choose);
		ivOpenMenu.setVisibility(View.GONE);
		
		mAdapter= new MamberAdapter(this,  null,true);
		
		mAdapter.setFilterQueryProvider(new FilterQueryProvider() {

	        @Override
	        public Cursor runQuery(CharSequence constraint) {

	            String strItemCode = constraint.toString();
	            return chatEngine.getMembers(strItemCode);

	        }
	    });
		 memberloader= new MemberLoader(ChooseMemberActivity.this, chatEngine);
		 memberloader.setConstraints(null);
		 getLoaderManager().initLoader(150, null, ChooseMemberActivity.this);
		 
		 //set Adapter
		 lvMembers.setAdapter(mAdapter);
		 lvMembers.setTextFilterEnabled(true);
		 
		 lvMembers.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) 
	            {
	            	//Open chat screen
	            	if(!isGroup)
	            	{
		            	TextView tv= (TextView)view.findViewById(R.id.tvMemberName);
		            	User u= (User)(tv.getTag());
		            	Intent in= new Intent(ChooseMemberActivity.this, ChatActivity.class);
		    			in.putExtra("uid", u.userId);
		    			in.putExtra("uname", u.userName);
		    	       startActivity(in);
		    	       finish();
	            	}
	            	else
	            	{
	            		//add connection to group
	            	}
	            }
	        });
		 
		 
			srch.setOnQueryTextListener(new SearchView.OnQueryTextListener() 
			 {
		            @Override
		            public boolean onQueryTextSubmit(String newText) {
		            	
		            	mAdapter.getFilter().filter(newText);
		                return true;
		            }

		            @Override
		            public boolean onQueryTextChange(String newText)
		            {
		            	mAdapter.getFilter().filter(newText);
		                return true;
		            }
		        });
			
			
			tvCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			
			tvSelect.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showInputDialog();
				}
			});
 		
 	}
	
	private void showInputDialog()
	{
		 AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

         // Setting Dialog Title
         // Setting Dialog Message
         alertDialog.setMessage(R.string.group_title);
         final EditText input = new EditText(this);
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                 LinearLayout.LayoutParams.MATCH_PARENT,
                                 LinearLayout.LayoutParams.MATCH_PARENT);
           input.setLayoutParams(lp);
           alertDialog.setView(input);

           alertDialog.setNeutralButton(R.string.Create, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					createGroupChat(input.getText().toString());
				}
			});
       // Showing Alert Message
         alertDialog.show();
	}
	
	private void createGroupChat(String title)
	{
		prg.setVisibility(View.VISIBLE);
		boolean isCreated=chatEngine.createGroupChat(title, vecSelected);
		
		if(isCreated)
		{
			Toast.makeText(this, "successs", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return memberloader;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
	private class MamberAdapter extends CursorAdapter
	{
		public class ViewHolder {
			TextView tvUserName;
			ImageView ivUserImage;
			CheckBox chkBox;
		}
		public MamberAdapter(Context context, Cursor c, Boolean flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			String uname  = cursor.getString(cursor.getColumnIndex("uname"));
			String uid  = cursor.getString(cursor.getColumnIndex("_id"));
			String userImage  = cursor.getString(cursor.getColumnIndex("profile_image"));
			
			User u= new User();
			u.userId= uid;
			u.userName= uname;
			
			
			
			if(!isGroup)
				viewHolder.chkBox.setVisibility(View.GONE);
			else
			{
				viewHolder.chkBox.setTag(u.userId);
				
				if(vecSelected.contains(uid))
				{
					viewHolder.chkBox.setChecked(true);
				}
				else
				{
					viewHolder.chkBox.setChecked(false);
				}
				
				viewHolder.chkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() 
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
					{
						String usid= (String) buttonView.getTag();
						if(isChecked)
						{
							if(!vecSelected.contains(usid))
							    vecSelected.add(usid);
						}
						else
						{
							if(vecSelected.contains(usid))
								vecSelected.remove(usid);
						}
					}
				});
			}
			
			viewHolder.tvUserName.setText(uname+"");
			viewHolder.tvUserName.setTag(u);
			 imageLoader.displayImage(userImage, viewHolder.ivUserImage, options);
		
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{
			View view= (LinearLayout)inflator.inflate(R.layout.add_member_strip, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.tvUserName = (TextView) view
					.findViewById(R.id.tvMemberName);
			viewHolder.chkBox = (CheckBox) view
					.findViewById(R.id.chlSelectContact);
			viewHolder.ivUserImage = (ImageView) view
					.findViewById(R.id.ivConnectionImage);
			view.setTag(viewHolder);
			return view;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		getLoaderManager().destroyLoader(150);
	}
	
	
	

}
