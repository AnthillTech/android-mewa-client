package cc.mewa;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

@ClientEndpoint
public class MewaClient {
	public static final String TAG = "MewaClient";
	
	public class InitConnectionException extends Exception {
		private static final long serialVersionUID = 7864421347737698156L;

		public InitConnectionException() {
	    	  
		}

		public InitConnectionException(String message) {
			super(message);
		}
	 }
	
	private String uri;
	private String channel;
	private String device;
	private String password;
	
	private boolean connected;
	private ClientManager client;
	private Session session;
	
	private WSListenerThread listenerThread;
	private OnMessageListener onMessageListener;
		
	public MewaClient(String uri, String channel, String device, String password) {
		this.uri = uri;
		this.channel = channel;
		this.device = device;
		this.password = password;
		
		connected = false;
		client = ClientManager.createClient();
	}

	public void setOnMessageListener(OnMessageListener onMessageListener) {
		this.onMessageListener = onMessageListener;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void connect() throws InitConnectionException {
		try {
			session = client.connectToServer(MewaClient.this, URI.create(uri));
		} catch (DeploymentException e) {
			throw new InitConnectionException(e.getMessage());
		} catch (IOException e) {
			throw new InitConnectionException(e.getMessage());
		}
		listenerThread = new WSListenerThread();
		listenerThread.start();
		connected = true;
	}
	
	public void close() {
		if (session != null) {
			send(Protocol.diconnect());
			try {
				session.close();
			} catch (Exception e) {     
				e.printStackTrace();
			}
			session = null;
		}
		
		if (listenerThread != null) {
			listenerThread.disconnect();
			listenerThread = null;
		}
		
		connected = false;
	}
	
	public boolean requestDevicesList() {
		return send(Protocol.getDevices());
	}
	
	public boolean sendEvent(String eventId, String params) {
		return send(Protocol.sendEvent(eventId, params));
	}
	
	public boolean sendMessage(String device,String msgId, String params) {
		return send(Protocol.sendMessage(device, msgId, params));
	}
	
	private boolean send(String message) {
		if (connected == false || session == null) return false;
		
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}	
	
	@OnOpen
	public void onOpen(Session session) {
		try {
			session.getBasicRemote().sendText(Protocol.connect(channel, device, password));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@OnMessage
    public void onMessage(String msg) {
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(msg).getAsJsonObject();
		String message = jsonObject.get("message").getAsString();
		if (message.equals("connected")) {
			if (onMessageListener != null) {
				onMessageListener.onConnected();
			}
		} else if (message.equals("disconnected")) {
			close();
			if (onMessageListener != null) {
				onMessageListener.onDisconnected();
			}
		} else if (message.equals("already-connected-error")) {
			if (onMessageListener != null) {
				onMessageListener.onError("already-connected-error");
			}
		} else if (message.equals("authorization-error")) {
			if (onMessageListener != null) {
				close();
				onMessageListener.onError("authorization-error");
			}
		} else if (message.equals("not-connected-error")) {
			if (onMessageListener != null) {
				onMessageListener.onError("not-connected-error");
			}
		} else if (message.equals("joined-channel")) {
			if (onMessageListener != null) {
				onMessageListener.onDeviceJoinedChannel(jsonObject.get("device").getAsString());
			}
		} else if (message.equals("left-channel")) {
			if (onMessageListener != null) {
				onMessageListener.onDeviceLeftChannel(jsonObject.get("device").getAsString());
			}
		} else if (message.equals("event")) {
			if (onMessageListener != null) {
				String device = jsonObject.get("device").getAsString();
				String eventId = jsonObject.get("id").getAsString();
				String params = jsonObject.get("params").getAsString();
				onMessageListener.onEvent(device, eventId, params);
			}
		} else if (message.equals("message")) {
			if (onMessageListener != null) {
				String device = jsonObject.get("device").getAsString();
				String msgId = jsonObject.get("id").getAsString();
				String params = jsonObject.get("params").getAsString();
				onMessageListener.onMessage(device, msgId, params);
			}
		} else if (message.equals("devices-event")) {
			if (onMessageListener != null) {
				Gson gson = new Gson();
				Type type = new TypeToken<List<String>>(){}.getType();
				List<String> devicesList =  gson.fromJson(jsonObject.get("devices"), type );
				onMessageListener.onDevicesEvent(devicesList);
			}
		}
    }
	
	@OnError
    public void onError(Throwable t) {
		close();
        t.printStackTrace();
    }

	private class WSListenerThread extends Thread {
		private Object waitObject;
		
		public WSListenerThread() {
			waitObject = new Object();
		}
		
		public void disconnect() {
			if (waitObject != null) {
				synchronized (waitObject) {
					waitObject.notifyAll();
				}
			}
		}
		
		private void keepAlive() {
			synchronized (waitObject) {
				try {
					waitObject.wait();
				} catch (InterruptedException e) {

				}
			}
		}
		
		@Override
		public void run() {
			keepAlive();
		}
	}
}
