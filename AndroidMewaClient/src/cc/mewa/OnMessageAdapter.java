package cc.mewa;

import java.util.List;


/**
 * OnMessageAdapter - abstract class implementing most OnMessageListener methods. Implemented methods are empty. This is just for convenience.
 * 
 * @author Jacek Dermont
 */
public abstract class OnMessageAdapter implements OnMessageListener {	
	
	/**
	 * Invoked whenever a device joins the channel.
	 * 
	 * @param timestamp - ISO 8601 time when packed was processed in the channel
	 * @param device - name of the device
	 */
	@Override
	public void onDeviceJoinedChannel(String timestamp, String device) {};
	
	/**
	 * Invoked whenever a device leaves the channel.
	 * 
	 * @param timestamp - ISO 8601 time when packed was processed in the channel
	 * @param device - name of the device
	 */
	@Override
	public void onDeviceLeftChannel(String timestamp, String device) {};
	
	/**
	 * Invoked when receiving an event sent from a device.
	 * 
	 * @param timestamp - ISO 8601 time when packed was processed in the channel
	 * @param fromDevice - name of the device
	 * @param eventId - event type
	 * @param params - event parameters
	 */
	@Override
	public void onEvent(String timestamp, String fromDevice, String eventId, String params) {};
	
	/**
	 * Invoked when receiving a message sent from a device.
	 * 
	 * @param timestamp - ISO 8601 time when packed was processed in the channel
	 * @param fromDevice - name of the device
	 * @param msgId - message type
	 * @param params - message parameters
	 */
	@Override
	public void onMessage(String timestamp, String fromDevice, String msgId, String params) {};
	
	/**
	 * Invoked when a channel, after requested, sends list of connected devices to this device.
	 * 
	 * @param timestamp - ISO 8601 time when packed was processed in the channel
	 * @param deviceList - list of connected devices in channel
	 */
	@Override
	public void onDevicesEvent(String timestamp, List<String> deviceList) {};
}
