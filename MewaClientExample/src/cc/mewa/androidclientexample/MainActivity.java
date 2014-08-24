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
	
	public static final String WS_URI = "ws://mewa.cc/ws";
	public static final String CHANNEL = "user.channel1";
	public static final String DEVICE = "android";
	public static final String PASSWORD = "c1";
	
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
				Log.d(TAG,"onConnected()");
				handler.sendEmptyMessage(0);
			}

			// note that this happens only when client explicitly gets disconnect signal from the channel
			// the WebSocket itself might be still alive
			@Override
			public void onDisconnected() {
				Log.d(TAG,"onDisconnected()");
			}
			
			// happens whenever WebSocket is closed
			@Override
			public void onClosed() {
				Log.d(TAG,"onClosed()");
				handler.sendEmptyMessage(2);
				clientIsDisconnected();
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
				
				// let's assume something bad happened and close the connection entirely
				connection.close();
			}
			
			@Override
			public void onDevicesEvent(String timestamp, List<String> deviceList) {
				Log.d(TAG,"onDevicesEvent() "+deviceList.toString());
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", timestamp+" --- "+"Devices: "+deviceList.toString());
				msg.setData(data);
				handler.sendMessage(msg);
			}
			
			@Override
			public void onEvent(String timestamp, String fromDevice, String eventId, String params) {
				Log.d(TAG,"onEvent() "+fromDevice+" "+eventId+" "+params);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", timestamp+" --- "+"Event "+eventId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			};
			
			@Override
			public void onMessage(String timestamp, String fromDevice, String msgId, String params) {
				Log.d(TAG,"onMessage() "+fromDevice+" "+msgId+" "+params);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", timestamp+" --- "+"Message "+msgId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceJoinedChannel(String timestamp, String device) {
				Log.d(TAG,"onDeviceJoinedChannel() "+device);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", timestamp+" --- "+device+" joined the channel");
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceLeftChannel(String timestamp, String device) {
				Log.d(TAG,"onDeviceLeftChannel() "+device);
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", timestamp+" --- "+device+" left the channel");
				msg.setData(data);
				handler.sendMessage(msg);
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
	    if (connection == null || !connection.isConnectedToChannel()) {
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
			    	try {
			    		connection.connect();
					} catch (InitConnectionException e) {
						e.printStackTrace();
						return "error";
					}
					return "ok";
				}
	        	
				@Override
			    protected void onPostExecute(String result) {
			        if (dialog.isShowing()) {
			            dialog.dismiss();
			        }
			        
			        if (result.equals("ok")) {
				    	clientIsConnected();
			        } else {
			        	Message msg = new Message();
						msg.what = 1;
						Bundle data = new Bundle();
						data.putString("text", "Could not connect");
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
		connection.sendEvent("event.X", "{ }");
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == 0) {
			loggerTxt.append("Connected\n");
		} else if (msg.what == 1) {
			String text = msg.getData().getString("text");
			loggerTxt.append(text+"\n");
		} else {
			loggerTxt.append("Connection closed.\n");
		}
		
		return false;
	}
    
}
