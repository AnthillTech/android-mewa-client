package cc.mewa;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * MewaConnection - websocket client implementation for mewa api
 * 
 * @author Jacek Dermont
 */
@ClientEndpoint
public class MewaConnection {
	/**
	 * InitConnectionException - an exception raised during initializing connection
	 */
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
		
	/**
	 * Constructor for MewaConnection. Takes WebSocket URI, channel name, device name and channel password as parametres.
	 * Example: new MewaClient("ws://localhost/ws","user.channel1","java","password1")
	 * 
	 * @param uri - WebSocket URI
	 * @param channel - channel name
	 * @param device - device name
	 * @param password - channel password
	 */
	public MewaConnection(String uri, String channel, String device, String password) {
		this.uri = uri;
		this.channel = channel;
		this.device = device;
		this.password = password;
		
		connected = false;
		client = ClientManager.createClient();
	}

	/**
	 * Sets OnMessageListener (or OnMessageAdapter), which will listen on incoming events. Set null to remove any OnMessageListeners.
	 * 
	 * @param onMessageListener - An OnMessageListener (or OnMessageAdapter)
	 */
	public void setOnMessageListener(OnMessageListener onMessageListener) {
		this.onMessageListener = onMessageListener;
	}
	
	/**
	 * Checks if the client is connected to the channel. May return false, while the WebSocket itself is still alive.
	 *  
	 * @return Returns whether is connected to channel or not.
	 */
	public boolean isConnectedToChannel() {
		return connected;
	}
	
	/**
	 * Connects or, if active, reconnects to the channel. Whether the channel actually accepts this device will be notified by
	 * <i>OnMessageListener.onConnected()</i> or <i>OnMessageListener.onError()</i>.
	 * 
	 * @throws InitConnectionException - if some errors occured during connection initialization
	 */
	public void connect() throws InitConnectionException {
		if (session != null && session.isOpen()) {
			close();
		}
		
		try {
			session = client.connectToServer(MewaConnection.this, URI.create(uri));
		} catch (DeploymentException e) {
			throw new InitConnectionException(e.getMessage());
		} catch (IOException e) {
			throw new InitConnectionException(e.getMessage());
		}
		
		listenerThread = new WSListenerThread();
		listenerThread.start();
	}
	
	/**
	 * Closes WebSocket. Raises no exception, doesn't check if the connection was already closed.
	 */
	public void close() {
		if (session != null) {
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
	
	/**
	 * Sends "disconnect" request to the channel. Keep in mind that the WebSocket will be still active. Returns false if sending the request failed.
	 * It is better to use <i>close()</i> instead.
	 * 
	 * @return Returns false if sending the request failed (i.e. connection not opened).
	 */
	public boolean disconnect() {
		return send(Protocol.disconnect());
	}
	
	/**
	 * Sends request device list event to the channel. Returns false if sending the request failed.
	 * Note that all events are asynchronous. The response will notify via <i>OnMessageListener.onDevicesEvent()</i> 
	 *  
	 * @return Returns false if sending the request failed (i.e. connection not opened).
	 */
	public boolean requestDevicesList() {
		return send(Protocol.getDevices());
	}
	
	/**
	 * Sends event to channel with parametres. Returns false if the sending failed.
	 * 
	 * @param eventId - event type
	 * @param params - event parametres
	 * @return Returns false if sending the event failed (i.e. connection not opened).
	 */
	public boolean sendEvent(String eventId, String params) {
		return send(Protocol.sendEvent(eventId, params));
	}
	
	/**
	 * Sends message to another device with parametres. Returns false if the sending failed.
	 * 
	 * @param device - other device name
	 * @param msgId - message type
	 * @param params - message parametres
	 * @return Returns false if sending the message failed (i.e. connection not opened).
	 */
	public boolean sendMessage(String device,String msgId, String params) {
		return send(Protocol.sendMessage(device, msgId, params));
	}
	
	/**
	 * Private method for sending any type of message. Returns false if the sending failed.
	 * 
	 * @param message - the message
	 * @return Returns false if the sending failed.
	 */
	private boolean send(String message) {
		if (connected == false || session == null) return false;
		
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	// Occurs after opening WebSocket
	@OnOpen
	public void onOpen(Session session) {
		try {
			session.getBasicRemote().sendText(Protocol.connect(channel, device, password));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Occurs after getting any type of message
	@OnMessage
    public void onMessage(String msg) {
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(msg).getAsJsonObject();
		String message = jsonObject.get("type").getAsString();
		if (message.equals("connected")) {
			connected = true;
			if (onMessageListener != null) {
				onMessageListener.onConnected();
			}
		} else if (message.equals("disconnected")) {
			connected = false;
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
				String time = jsonObject.get("time").getAsString();
				String device = jsonObject.get("device").getAsString();
				onMessageListener.onDeviceJoinedChannel(time, device);
			}
		} else if (message.equals("left-channel")) {
			if (onMessageListener != null) {
				String time = jsonObject.get("time").getAsString();
				String device = jsonObject.get("device").getAsString();
				onMessageListener.onDeviceLeftChannel(time, device);
			}
		} else if (message.equals("event")) {
			if (onMessageListener != null) {
				String time = jsonObject.get("time").getAsString();
				String device = jsonObject.get("device").getAsString();
				String eventId = jsonObject.get("id").getAsString();
				String params = jsonObject.get("params").getAsString();
				onMessageListener.onEvent(time, device, eventId, params);
			}
		} else if (message.equals("message")) {
			if (onMessageListener != null) {
				String time = jsonObject.get("time").getAsString();
				String device = jsonObject.get("device").getAsString();
				String msgId = jsonObject.get("id").getAsString();
				String params = jsonObject.get("params").getAsString();
				onMessageListener.onMessage(time, device, msgId, params);
			}
		} else if (message.equals("devices-event")) {
			if (onMessageListener != null) {
				String time = jsonObject.get("time").getAsString();
				Gson gson = new Gson();
				Type type = new TypeToken<List<String>>(){}.getType();
				List<String> devicesList =  gson.fromJson(jsonObject.get("devices"), type );
				onMessageListener.onDevicesEvent(time, devicesList);
			}
		}
    }
	
	// Occurs on WebSocket error
	@OnError
    public void onError(Throwable t) {
		close();
        t.printStackTrace();
    }
	
	// Occurs on WebSocket close
	@OnClose
	public void onClose() {
		close();
		if (onMessageListener != null) {
			onMessageListener.onClosed();
		}
	}

	// Internal listener thread
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
