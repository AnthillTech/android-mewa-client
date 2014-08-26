# android-mewa-client

Android library for mewa client. Tested on Android 2.3.3+ (API 10). Pure Java applications should also benefit from the library, as no Android-specific code is used there.

## Libraries used

* google-gson for json parsing
* tyrus-standalone-client for websockets support

## Installation

Just import the AndroidMewaClient project to Eclipse. The android projects wanting to use it, shall reference this project.
MewaClientExample is a sample project referencing to the library.

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

// it's better to close connection after leaving
@Override
protected void onDestroy() {
  if (connection != null) {
    connection.close();
  }
  super.onDestroy();
}
```
