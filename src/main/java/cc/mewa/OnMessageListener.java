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
	 * Invoked when disconnected from channel or the entire WebSocket is closed.
	 */
	public void onClosed();
	
	/**
	 * Invoked when received error message from the channel.
	 * Error messages: "already-connected-error", "authorization-error", "not-connected-error".
	 * On "authorization-error" automatically closes WebSocket.
	 * 
	 * @param reason - text message
	 */
	public void onError(String reason);
	
	/**
	 * Invoked whenever a device joins the channel.
	 * 
	 * @param device - name of the device
	 */
	public void onDeviceJoinedChannel(String timestamp, String device);
	
	/**
	 * Invoked whenever a device leaves the channel.
	 * 
	 * @param timestamp - ISO 8601 time when packet was processed in the channel
	 * @param device - name of the device
	 */
	public void onDeviceLeftChannel(String timestamp, String device);
	
	/**
	 * Invoked when receiving an event sent from a device.
	 * 
	 * @param timestamp - ISO 8601 time when packet was processed in the channel
	 * @param fromDevice - name of the device
	 * @param eventId - event type
	 * @param params - event parameters
	 */
	public void onEvent(String timestamp, String fromDevice, String eventId, String params);
	
	/**
	 * Invoked when receiving a message sent from a device.
	 * 
	 * @param timestamp - ISO 8601 time when packet was processed in the channel
	 * @param fromDevice - name of the device
	 * @param msgId - message type
	 * @param params - message parameters
	 */
	public void onMessage(String timestamp, String fromDevice, String msgId, String params);
	
	/**
	 * Invoked when a channel, after requested, sends list of connected devices to this device.
	 * 
	 * @param timestamp - ISO 8601 time when packet was processed in the channel
	 * @param deviceList - list of connected devices in channel
	 */
	public void onDevicesEvent(String timestamp, List<String> deviceList);
	
	
	/**
	 * Invoked when a channel, after requested, sends list of last events given by request parameters.
	 * List is in form of Arrays of Strings: timestamp, device, eventId, params
	 * 
	 * @param timestamp - ISO 8601 time when packet was processed in the channel
	 * @param eventList - list of events in array: timestamp, device, eventId, params
	 */
	public void onLastEvents(String timestamp, List<String[]> eventList);
	
	/**
	 * Invoked after sending event with ack parameter set to true. Indicates event was sent successfully.
	 */
	public void onAck();
}