package my.gomland.android.bluetooth;

import android.content.Context;
import android.support.annotation.NonNull;

public class BluetoothFactory {
    public static Bluetooth createNewInstance(@NonNull Context context, @NonNull BluetoothListener bluetoothListener) {
        BluetoothImpl bluetooth = new BluetoothImpl();
        bluetooth.initialize(context, bluetoothListener);
        return bluetooth;
    }
}
