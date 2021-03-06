package cc.mewa.androidclientexample;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cc.mewa.MewaConnection;
import cc.mewa.MewaConnection.InitConnectionException;
import cc.mewa.OnMessageListener;

public class MainActivity extends Activity implements Handler.Callback {
	public static final String TAG = "MainActivity";
	
	public static final String WS_URI = "ws://channels.followit24.com/ws";
	public static final String CHANNEL = "jdermont.channel1";
	public static final String DEVICE = "android";
	public static final String PASSWORD = "tpjdauck";
	
	Button connectBtn,deviceListBtn,sendEventBtn;
	EditText loggerTxt;
	
	Handler handler;
	AsyncTask<String,String,String> task;
	
	MewaConnection connection;
	OnMessageListener onMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        connectBtn = (Button)findViewById(R.id.connectBtn);
        deviceListBtn = (Button)findViewById(R.id.deviceListBtn);
        sendEventBtn = (Button)findViewById(R.id.sendEventBtn);
        clientIsDisconnected();
        loggerTxt = (EditText)findViewById(R.id.loggerTxt);
        
        handler = new Handler(this);
        
        onMessageListener = new OnMessageListener() {
			@Override
			public void onConnected() {
				Log.d(TAG,"onConnected(2)");
				handler.sendEmptyMessage(0);
			}
			
			@Override
			public void onClosed() {
				Log.d(TAG,"onClosed()");
				handler.sendEmptyMessage(2);
			}

			@Override
			public void onError(String reason) {
				Log.d(TAG,"onError() "+reason);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", reason);
				msg.setData(data);
				handler.sendMessage(msg);
			}
			
			@Override
			public void onDevicesEvent(String timestamp, List<String> deviceList) {
				Log.d(TAG,"onDevicesEvent() "+deviceList.toString());
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Devices: "+deviceList.toString());
				msg.setData(data);
				handler.sendMessage(msg);
			}
			
			@Override
			public void onEvent(String timestamp, String fromDevice, String eventId, String params) {
				Log.d(TAG,"onEvent() "+fromDevice+" "+eventId+" "+params);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Event "+eventId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			};
			
			@Override
			public void onMessage(String timestamp, String fromDevice, String msgId, String params) {
				Log.d(TAG,"onMessage() "+fromDevice+" "+msgId+" "+params);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Message "+msgId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceJoinedChannel(String timestamp, String device) {
				Log.d(TAG,"onDeviceJoinedChannel() "+device);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", device+" joined the channel");
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceLeftChannel(String timestamp, String device) {
				Log.d(TAG,"onDeviceLeftChannel() "+device);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", device+" left the channel");
				msg.setData(data);
				handler.sendMessage(msg);
			}
			
			@Override
			public void onAck() {
				Log.d(TAG,"onAck() ");
			}

			@Override
			public void onLastEvents(String timestamp, List<String[]> eventList) {
				Log.d(TAG,"onLastEvents() "+eventList.size());
			}
        };
    }
    
    @Override
    protected void onDestroy() {
    	if (connection != null) {
    		connection.close();
    	}
    	super.onDestroy();
    }
    
    private void clientIsConnected() {
    	connectBtn.setText("Disconnect");
    	deviceListBtn.setEnabled(true);
    	sendEventBtn.setEnabled(true);
    }
    
    private void clientIsDisconnected() {
    	connectBtn.setText("Connect");
    	deviceListBtn.setEnabled(false);
    	sendEventBtn.setEnabled(false);
    }
    
	public void connectClick(View v) {
	    if (connection == null) {
	        task = new AsyncTask<String,String,String>() {
	        	ProgressDialog dialog;
	        	
	        	@Override
	            protected void onPreExecute() {
	        		dialog = new ProgressDialog(MainActivity.this);
	                dialog.setMessage("Connecting to server...");
	                dialog.setCancelable(true);
	                dialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface d) {
							Log.d(TAG,"cancelled");
							cancel(true);
						}
	                });
	                dialog.show();
	            }
	        	
				@Override
				protected String doInBackground(String... params) {
					connection = new MewaConnection(WS_URI,CHANNEL,DEVICE,PASSWORD);
					connection.setOnMessageListener(onMessageListener);
					//connection.subscribeToEvents(new String[] { "org.fi24.light", "org.fi24.switch" });
					connection.subscribeToAllEvents();
					try {
						connection.connect();
					} catch (InitConnectionException e) {
						return "error";
					}
					return "ok";
				}
	        	
				@Override
			    protected void onPostExecute(String result) {
			        if (dialog.isShowing()) {
			            dialog.dismiss();
			        }
			        
			        if (!result.equals("ok")) {
			        	Message msg = new Message();
						msg.what = 1;
						Bundle data = new Bundle();
						data.putString("text", "Could not connect.");
						msg.setData(data);
						handler.sendMessage(msg);
			        }
			    }
				
				@Override
				protected void onCancelled() {
					if (connection != null) {
						connection.setOnMessageListener(null);
						connection.close();
					}
					clientIsDisconnected();
				}
	        };
	        task.execute();
	    } else {
	    	connection.close();
	    	clientIsDisconnected();
	    }
	}
	
	public void devicesListClick(View v) {
		connection.requestDevicesList();
	}
	
	public void sendEventClick(View v) {
		connection.sendEvent("event.X", "{ }", true);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == 0) {
			clientIsConnected();
			loggerTxt.append("Connected\n");
		} else if (msg.what == 1) {
			String text = msg.getData().getString("text");
			loggerTxt.append(text+"\n");
		} else {
			clientIsDisconnected();
			loggerTxt.append("Connection closed.\n");
		}
		
		return false;
	}
    
}
