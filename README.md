# android-mewa-client

Android library for mewa client.

## Libraries used

* google-gson for json parsing
* tyrus-standalone-client for websockets support

## Usage

It basically uses MewaClient as client and OnMessageListener as listener for incoming messages and events.

```java
final MewaClient client = new MewaClient("ws://mewa.cc/ws","test","android","pass");
client.setOnMessageListener(new OnMessageListener() {
  @Override
  public void onConnected() {
    Log.d(TAG,"onConnected()");
    client.requestDevicesList();
  }

  @Override
  public void onDisconnected() {
    // note that this happens only when client explicitly gets disconnect signal from the channel
    // when it receives '{ "message" : "disconnected" }', not when some error occures
    Log.d(TAG,"onDisconnected()");
  }

  @Override
  public void onError(String reason) {
    Log.d(TAG,"Error occured. Reason: "+reason);
  }
  
  @Override
  public void onDevicesEvent(List<String> deviceList) {
    Log.d(TAG,"Devices connected: "+deviceList.toString());
  }
  
  @Override
  public void onEvent(String fromDevice, String eventId, String params) {
    Log.d(TAG,String.format("Event %s from %s with params %s",eventId,fromDevice,params));
  };
  
  @Override
  public void onMessage(String fromDevice, String msgId, String params) {
    Log.d(TAG,String.format("Message %s from %s with params %s",msgId,fromDevice,params));
  }

  @Override
  public void onDeviceJoinedChannel(String device) {
    Log.d(TAG,device+" joined the channel");
  }

  @Override
  public void onDeviceLeftChannel(String device) {
    Log.d(TAG,device+" left the channel");
  }
});

try {
  client.connect();
} catch (InitConnectionException e) {
  e.printStackTrace();
} catch (AlreadyConnectedToChannelException e) {
  e.printStackTrace();
}
```
