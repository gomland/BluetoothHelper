# BluetoothHelper
Bluetooth 편리하게 사용할 수 있게 돕는 클래스입니다.
1개의 인스턴스는 1개의 Connection을 관리합니다.

### Need Permission
* ACCESS_COARSE_LOCATION

### Create Instance
```java
Bluetooth bluetooth = BluetoothFactory.createNewInstance(Context, BluetoothListener);
```

### BluetoothListener
동작에 대한 응답을 받는 인터페이스입니다.
```java
public interface BluetoothListener {
    void discoveryDevice(BluetoothDevice device);
    void discoveryFinished();
    void connectionState(@Bluetooth.State int state);
    void receiveMessage(String message);
}
```

### Connect
```java
bluetooth.connect(Bluetooth.UuidType, {target MAC address});
```

### Listen
```java
bluetooth.listen(Bluetooth.UuidType);
```

### Send Message
```java
bluetooth.sendMessage({message}});
```

### Disconnect
```java
bluetooth.disconnect();
```