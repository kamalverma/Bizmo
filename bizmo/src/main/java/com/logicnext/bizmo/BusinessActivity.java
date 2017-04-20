package com.logicnext.bizmo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class BusinessActivity extends MainActivity implements OnClickListener
{
	
	LayoutInflater inflator;
	View businessView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		inflator= getLayoutInflater();
		
		businessView = inflator.inflate(R.layout.business, null);
		rlMain.addView(businessView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		tvHeader.setText(R.string.business);
		
		ivInviteUser.setVisibility(View.GONE);
		ivOpenMenu.setOnClickListener(this);
	}
	
	
	@Override
	public void onClick(View v) {
		
		if(v== ivOpenMenu)
		{
			toggleDrawer();
		}
	}

}
