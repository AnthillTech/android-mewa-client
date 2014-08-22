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

/**
 * MewaClient - websocket client implementation for mewa api
 * 
 * @author Jacek Dermont
 */
@ClientEndpoint
public class MewaClient {
	public static final String TAG = "MewaClient";
	
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
	
	/**
	 * AlreadyConnectedToChannelException - an exception raised when trying to connect to already connected channel
	 */
	public class AlreadyConnectedToChannelException extends Exception {
		private static final long serialVersionUID = 17462239345384243L;

		public AlreadyConnectedToChannelException() {
			
		}

		public AlreadyConnectedToChannelException(String message) {
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
	 * Constructor for MewaClient. Takes WebSocket URI, channel name, device name and channel password as parametres.
	 * Example: new MewaClient("ws://localhost/ws","user.channel1","java","password1")
	 * 
	 * @param uri - WebSocket URI
	 * @param channel - channel name
	 * @param device - device name
	 * @param password - channel password
	 */
	public MewaClient(String uri, String channel, String device, String password) {
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
	 * Checks if the client is connected to the channel.
	 *  
	 * @return Returns whether is connected to channel or not.
	 */
	public boolean isConnected() {
		return connected && session != null && session.isOpen();
	}
	
	/**
	 * Connects to the WebSocket. Whether the channel actually accepts this device will be notified by
	 * <b>OnMessageListener.onConnected()</b> or <b>OnMessageListener.onError()</b>.
	 * 
	 * @throws InitConnectionException - if some errors occured during connection initialization
	 * @throws AlreadyConnectedToChannelException - if the client is already connected
	 */
	public void connect() throws InitConnectionException,AlreadyConnectedToChannelException {
		if (isConnected()) {
			throw new AlreadyConnectedToChannelException("Already connected to the channel.");
		}
		
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
	
	/**
	 * Closes WebSocket. Raises no exception, doesn't check if the connection was already closed.
	 */
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
	
	/**
	 * Sends request device list event to the channel. Returns false if sending the request failed.
	 * Note that all events are asynchronous. The response will be notified via <b>OnMessageListener.onDevicesEvent()</b> 
	 *  
	 * @return Returns false if sending the request failed.
	 */
	public boolean requestDevicesList() {
		return send(Protocol.getDevices());
	}
	
	/**
	 * Sends event to channel with parametres. Returns false if the sending failed.
	 * 
	 * @param eventId - event type
	 * @param params - event parametres
	 * @return Returns false if sending the event failed.
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
	 * @return Returns false if sending the message failed.
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
	
	// Occurs on WebSocket error
	@OnError
    public void onError(Throwable t) {
		close();
        t.printStackTrace();
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
