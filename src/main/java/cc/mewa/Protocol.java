package cc.mewa;

import com.google.gson.JsonObject;

/**
 * Protocol - helper class for sending messages
 * 
 * @author ashiren
 */
class Protocol {
	
	public static String connect(String channel,String device, String password) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "connect");
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("password", password);
		return jsonObject.toString();
	}
	
	public static String disconnect() {
		/*JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "get-devices");
		return jsonObject.toString();*/
		return "{\"type\": \"disconnect\"}";
	}
		
	public static String getDevices() {
		/*JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "get-devices");
		return jsonObject.toString();*/
		return "{\"type\": \"get-devices\"}";
	}
	
	public static String sendEvent(String eventId, String params) {		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "send-event");
		jsonObject.addProperty("id", eventId);
		jsonObject.addProperty("params", params);
		return jsonObject.toString();
	}
	
	public static String sendMessage(String device, String msgId, String params) {		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "send-message");
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("id", msgId);
		jsonObject.addProperty("params", params);
		return jsonObject.toString();
	}

	private Protocol() {
		
	}

}
