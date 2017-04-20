package com.bizmo.interfaces;


public interface OnCurrentUserMessageLitener {
	
	public void onMessageReceived();
	
	public void userTyping();
	
	public void pausedTyping();
	
	public void onPresenseReceived(String presence);
	
	

}
