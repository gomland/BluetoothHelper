package my.gomland.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.Set;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by ShinSung on 2018-07-01.
 */
public interface Bluetooth {
    @StringDef({
            UuidType.DEFAULT,
            UuidType.SERIAL,
            UuidType.HEADSET,
            UuidType.UDP
    })
    @Retention(SOURCE)
    @interface UuidType {
        String DEFAULT = "00000000-0000-1000-8000-00805F9B34FB";
        String SERIAL = "00001101-0000-1000-8000-00805F9B34FB";
        String HEADSET = "00001108-0000-1000-8000-00805F9B34FB";
        String UDP = "00000002-0000-1000-8000-00805F9B34FB";
    }

    @IntDef({
            State.CONNECTED,
            State.FAIL,
            State.LOST
    })
    @Retention(SOURCE)
    @interface State {
        int CONNECTED = 0;
        int FAIL = 1;
        int LOST = 2;
    }

    Bluetooth Helper = new BluetoothImpl();

    void initialize(@NonNull Context context, @NonNull BluetoothListener bluetoothListener);

    void disconnect();

    void startDiscovery();

    void cancelDiscovery();

    Set<BluetoothDevice> getPairedDevice();

    void listen(@UuidType @NonNull final String uuid);

    void connect(@UuidType @NonNull String uuid, @NonNull String address);

    void sendMessage(String message);
}
