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
		jsonObject.addProperty("message", "connect");
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("password", password);
		return jsonObject.toString();
	}
	
	public static String diconnect() {
		/*JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", "disconnect");
		return jsonObject.toString();*/
		return "{\"message\": \"disconnect\"}";
	}
	
	public static String getDevices() {
		/*JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", "get-devices");
		return jsonObject.toString();*/
		return "{\"message\": \"get-devices\"}";
	}
	
	public static String sendEvent(String eventId, String params) {		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", "send-event");
		jsonObject.addProperty("id", eventId);
		jsonObject.addProperty("params", params);
		return jsonObject.toString();
	}
	
	public static String sendMessage(String device, String msgId, String params) {		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", "send-message");
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("id", msgId);
		jsonObject.addProperty("params", params);
		return jsonObject.toString();
	}

	private Protocol() {
		
	}

}
