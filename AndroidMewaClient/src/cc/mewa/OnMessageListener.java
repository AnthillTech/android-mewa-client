package cc.mewa;

import java.util.List;

/**
 * OnMessageListener - message listener for receiving incoming messages and events.
 * 
 * @author Jacek Dermont
 */
public interface OnMessageListener {
	
	/**
	 * Invoked when receiving <b>connected</b> message from the channel.
	 */
	public void onConnected();
	
	/**
	 * Invoked <b>only</b> when receiving <b>disconnected</b> message from the channel. When this happens, client closes the connection.
	 */
	public void onDisconnected();
	
	/**
	 * Invoked when received error message from the channel.
	 * 
	 * @param reason - text message
	 */
	public void onError(String reason);
	
	/**
	 * Invoked whenever a device joins the channel.
	 * 
	 * @param device - name of the device
	 */
	public void onDeviceJoinedChannel(String device);
	
	/**
	 * Invoked whenever a device leaves the channel.
	 * 
	 * @param device - name of the device
	 */
	public void onDeviceLeftChannel(String device);
	
	/**
	 * Invoked when receiving an event sent from a device.
	 * 
	 * @param fromDevice - name of the device
	 * @param eventId - event type
	 * @param params - event parametres
	 */
	public void onEvent(String fromDevice, String eventId, String params);
	
	/**
	 * Invoked when receiving a message sent from a device.
	 * 
	 * @param fromDevice - name of the device
	 * @param msgId - message type
	 * @param params - message parametres
	 */
	public void onMessage(String fromDevice, String msgId, String params);
	
	/**
	 * Invoked when a channel, after requested, sends list of connected devices to this device.
	 * 
	 * @param deviceList - list of connected devices in channel
	 */
	public void onDevicesEvent(List<String> deviceList);
}