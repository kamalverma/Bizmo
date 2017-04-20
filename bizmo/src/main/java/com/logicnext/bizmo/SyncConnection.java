package com.logicnext.bizmo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncConnection extends Service
{
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
