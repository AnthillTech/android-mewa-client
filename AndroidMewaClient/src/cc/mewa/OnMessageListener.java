package cc.mewa;

import java.util.List;

public interface OnMessageListener {
	public void onConnected();
	public void onDisconnected();
	public void onError(String reason);
	public void onDeviceJoinedChannel(String device);
	public void onDeviceLeftChannel(String device);
	public void onEvent(String fromDevice, String eventId, String params);
	public void onMessage(String fromDevice, String msgtId, String params);
	public void onDevicesEvent(List<String> deviceList);
}