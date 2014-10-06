# android-mewa-client

Android library for mewa client. Tested on Android 2.3.3+ (API 10). Pure Java applications should also benefit from the library, the Android-specific code is used for wake lock, but it can be easily removed.

## Libraries used

* google-gson for json parsing
* tyrus-standalone-client for websockets support

## Building and import

Run:
```sh
ant build
```
to create android-mewa-client.jar in the path. You can attach this jar to your project. Or you can import the main project, for example in eclipse, and reference to it.

Run:
```sh
ant build-example
```
to build an example application. It will create MewaClientExample.apk in the path. The app requires Android 2.3.3+.

To clean bin/ and gen/ files, run:
```sh
ant clean
```

## Usage

It basically uses MewaConnection as client and OnMessageListener as listener for incoming messages and events.

```java
MewaConnection connection = new MewaConnection("ws://mewa.cc/ws","channel","android","pass");
connection.setOnMessageListener(new OnMessageListener() {
  @Override
  public void onConnected() {
    Log.d(TAG,"onConnected()");
    client.requestDevicesList(); // requests for devices list after connecting
  }

  @Override
  public void onClosed() {
    Log.d(TAG,"onClosed()");
  }

  @Override
  public void onError(String reason) {
    Log.d(TAG,"Error occured. Reason: "+reason);
  }
  
  @Override
  public void onDevicesEvent(String timestamp, List<String> deviceList) {
    Log.d(TAG,"Devices connected: "+deviceList.toString());
  }
  
  @Override
  public void onEvent(String timestamp, String fromDevice, String eventId, String params) {
    Log.d(TAG,String.format("Event %s from %s with params %s",eventId,fromDevice,params));
  };
  
  @Override
  public void onMessage(String timestamp, String fromDevice, String msgId, String params) {
    Log.d(TAG,String.format("Message %s from %s with params %s",msgId,fromDevice,params));
  }

  @Override
  public void onDeviceJoinedChannel(String timestamp, String device) {
    Log.d(TAG,device+" joined the channel");
  }

  @Override
  public void onDeviceLeftChannel(String timestamp, String device) {
    Log.d(TAG,device+" left the channel");
  }
});

try {
  connection.connect();
} catch (InitConnectionException e) {
  e.printStackTrace();
}

// it's better to close connection before leaving
@Override
protected void onDestroy() {
  if (connection != null) {
    connection.close();
  }
  super.onDestroy();
}
```
