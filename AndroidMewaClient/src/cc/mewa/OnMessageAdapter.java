package cc.mewa;

import java.util.List;

public abstract class OnMessageAdapter implements OnMessageListener {	
	@Override
	public void onDeviceJoinedChannel(String device) {};
	
	@Override
	public void onDeviceLeftChannel(String device) {};
	
	@Override
	public void onEvent(String fromDevice, String eventId, String params) {};
	
	@Override
	public void onMessage(String fromDevice, String msgId, String params) {};
	
	@Override
	public void onDevicesEvent(List<String> deviceList) {};
}
