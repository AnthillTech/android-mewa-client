package cc.mewa;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
	
	public static String connect(String channel,String device, String password, String[] subscribedEvents) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "connect");
		jsonObject.addProperty("channel", channel);
		jsonObject.addProperty("device", device);
		jsonObject.addProperty("password", password);
		JsonArray events = new JsonArray();
		if (subscribedEvents != null) {			
			for (String subscribedEvent : subscribedEvents) {
				events.add(new JsonPrimitive(subscribedEvent));
			}
		}
		jsonObject.add("subscribe", events);
		return jsonObject.toString();
	}
	
	public static String disconnect() {
		return "{\"type\": \"disconnect\"}";
	}
		
	public static String getDevices() {
		return "{\"type\": \"get-devices\"}";
	}
	
	public static String sendEvent(String eventId, String params, boolean ack) {		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "send-event");
		jsonObject.addProperty("id", eventId);
		jsonObject.addProperty("ack", ack);
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
