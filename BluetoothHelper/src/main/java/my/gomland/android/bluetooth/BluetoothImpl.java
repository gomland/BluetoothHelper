package my.gomland.android.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ShinSung on 2018-07-01.
 */
class BluetoothImpl implements Bluetooth {
    private final int REQUEST_ENABLE_BLUETOOTH = 19192;

    private BluetoothSocket mSocket;
    private ConnectedThread mConnectedThread;

    private Context mContext;
    private BluetoothListener mBluetoothListener;

    private BluetoothHandler mBluetoothHandler = new BluetoothHandler();
    private BluetoothBroadcastReceiver mBluetoothBroadcastReceiver;


    BluetoothImpl(){
    }

    void initialize(@NonNull Context context, @NonNull BluetoothListener bluetoothListener) {
        mContext = context;
        mBluetoothListener = bluetoothListener;

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            Log.e("Bluetooth", "### ACCESS_COARSE_LOCATION 권한 허용이 필요합니다. ### ");
        }
    }

    @Override
    public void disconnect() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        if (mConnectedThread != null) {
            if (!mConnectedThread.isInterrupted()) {
                mConnectedThread.interrupt();
            }
            mConnectedThread.close();
            mConnectedThread = null;
        }
    }

    @Override
    public void startDiscovery() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isDiscovering()) {
            btAdapter.startDiscovery();
        }

        registerReceiver();
    }

    @Override
    public void cancelDiscovery() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        unregisterReceiver();
    }

    @Override
    public Set<BluetoothDevice> getPairedDevice() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    }

    @Override
    public void listen(@UuidType @NonNull final String uuid) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        mContext.startActivity(intent);

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new Thread() {
            @Override
            public void run() {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothServerSocket serverSocket = null;
                try {
                    serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(btAdapter.getName(), UUID.fromString(uuid));
                    mSocket = serverSocket.accept();
                    connected();
                } catch (IOException e) {
                    e.printStackTrace();
                    mBluetoothHandler.sendEmptyMessage(Bluetooth.State.FAIL);
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    public void connect(@UuidType @NonNull String uuid, @NonNull String address) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
        } catch (IOException e) {
            e.printStackTrace();
            mBluetoothHandler.sendEmptyMessage(Bluetooth.State.FAIL);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    mSocket.connect();
                    connected();
                } catch (IOException e) {
                    e.printStackTrace();
                    mBluetoothHandler.sendEmptyMessage(Bluetooth.State.FAIL);
                }
            }
        }.start();
    }

    @Override
    public void sendMessage(String message) {
        if (mConnectedThread != null) {
            mConnectedThread.write(message.getBytes());
        }
    }

    private void registerReceiver() {
        if (mBluetoothBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            mBluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();

            mContext.registerReceiver(mBluetoothBroadcastReceiver, filter);
        }
    }

    private void unregisterReceiver() {
        if (mBluetoothBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBluetoothBroadcastReceiver);
        }
        mBluetoothBroadcastReceiver = null;
    }

    private synchronized void connected() {
        if (mConnectedThread != null) {
            mConnectedThread.close();
        }

        mConnectedThread = new ConnectedThread(mSocket, mBluetoothHandler);
        mConnectedThread.start();
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mBluetoothListener.discoveryDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mBluetoothListener.discoveryFinished();
                unregisterReceiver();
            }
        }
    }

    private class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectedThread.RECEIVED:
                    if (msg.obj instanceof String) {
                        mBluetoothListener.receiveMessage((String) msg.obj);
                    }
                    break;
                default:
                    mBluetoothListener.connectionState(msg.what);
            }
        }
    }
}
