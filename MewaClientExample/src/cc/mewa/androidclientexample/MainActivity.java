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
import cc.mewa.MewaClient;
import cc.mewa.MewaClient.AlreadyConnectedToChannelException;
import cc.mewa.MewaClient.InitConnectionException;
import cc.mewa.OnMessageListener;

public class MainActivity extends Activity implements Handler.Callback {
	public static final String TAG = "MainActivity";
	
	public static final String WS_URI = "ws://mewa.cc/ws";
	public static final String CHANNEL = "test.channel1";
	public static final String DEVICE = "android";
	public static final String PASSWORD = "test";
	
	Button connectBtn,deviceListBtn,sendEventBtn;
	EditText loggerTxt;
	
	boolean connected;
	Handler handler;
	AsyncTask<String,String,String> task;
	
	MewaClient client;
	OnMessageListener onMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        connectBtn = (Button)findViewById(R.id.connectBtn);
        deviceListBtn = (Button)findViewById(R.id.deviceListBtn);
        sendEventBtn = (Button)findViewById(R.id.sendEventBtn);
        buttonsDisable();
        loggerTxt = (EditText)findViewById(R.id.loggerTxt);
        
        connected = false;
        handler = new Handler(this);
        
        onMessageListener = new OnMessageListener() {
        	
			@Override
			public void onConnected() {
				handler.sendEmptyMessage(0);
			}

			@Override
			public void onDisconnected() {
				// note that this happens only when client explicitly gets disconnect signal from the channel
				connected = false;
				buttonsDisable();
			}

			@Override
			public void onError(String reason) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", reason);
				msg.setData(data);
				handler.sendMessage(msg);
				
				// let's assume something bad happened and close the webSocket
				if (client != null) {
					client.close();
				}
				connected = false;
				buttonsDisable();
			}
			
			@Override
			public void onDevicesEvent(List<String> deviceList) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Devices: "+deviceList.toString());
				msg.setData(data);
				handler.sendMessage(msg);
				
			}
			
			@Override
			public void onEvent(String fromDevice, String eventId, String params) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Event "+eventId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			};
			
			@Override
			public void onMessage(String fromDevice, String msgId, String params) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", "Message "+msgId+" from "+fromDevice+" with params "+params);
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceJoinedChannel(String device) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", device+" joined the channel");
				msg.setData(data);
				handler.sendMessage(msg);
			}

			@Override
			public void onDeviceLeftChannel(String device) {
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("text", device+" left the channel");
				msg.setData(data);
				handler.sendMessage(msg);
			}
        };
    }
    
    @Override
    protected void onDestroy() {
    	if (client != null && client.isConnected()) {
    		client.close();
    	}
    	super.onDestroy();
    }
    
    private void buttonsEnable() {
    	connectBtn.setText("Disconnect");
    	deviceListBtn.setEnabled(true);
    	sendEventBtn.setEnabled(true);
    }
    
    private void buttonsDisable() {
    	connectBtn.setText("Connect");
    	deviceListBtn.setEnabled(false);
    	sendEventBtn.setEnabled(false);
    }
    
	public void connectClick(View v) {
	    if (connected == false) {
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
			    	client = new MewaClient(WS_URI,CHANNEL,DEVICE,PASSWORD);
			    	client.setOnMessageListener(onMessageListener);
			    	try {
						client.connect();
					} catch (InitConnectionException e) {
						e.printStackTrace();
						return "error";
					} catch (AlreadyConnectedToChannelException e) {
						// TODO Auto-generated catch block
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
				    	buttonsEnable();
				    	connected = true;
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
					if (client != null && client.isConnected()) {
						client.close();
					}
					buttonsDisable();
			    	connected = false;
				}
	        };
	        task.execute();
	    } else {
	    	client.close();
	    	buttonsDisable();
	    	connected = false;
	    }
	}
	
	public void devicesListClick(View v) {
		client.requestDevicesList();
	}
	
	public void sendEventClick(View v) {
		client.sendEvent("event.X", "{ }");
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == 0) {
			loggerTxt.append("Connected\n");
		} else {
			String text = msg.getData().getString("text");
			loggerTxt.append(text+"\n");
		}
		
		return false;
	}
    
}
