package my.gomland.android.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ShinSung on 2018-07-01.
 */
public interface BluetoothListener {
    void discoveryDevice(BluetoothDevice device);
    void discoveryFinished();
    void connectionState(@Bluetooth.State int state);
    void receiveMessage(String message);
}
