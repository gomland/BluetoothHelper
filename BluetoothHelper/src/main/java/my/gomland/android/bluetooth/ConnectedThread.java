package my.gomland.android.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ConnectedThread extends Thread {
    static final int RECEIVED = -11;
    private Handler mHandler;

    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mHandler = handler;

        try {
            mInputStream = socket.getInputStream();
            mOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (mInputStream == null || mOutputStream == null) {
            mHandler.sendEmptyMessage(Bluetooth.State.FAIL);
            return;
        }

        mHandler.sendEmptyMessage(Bluetooth.State.CONNECTED);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int byteAvailable = mInputStream.available();   // 수신 데이터 확인

                if (byteAvailable > 0) {   // 데이터가 수신된 경우.
                    byte[] packetBytes = new byte[byteAvailable];
                    mInputStream.read(packetBytes);
                    final String data = new String(packetBytes, "utf-8");
                    Message message = new Message();
                    message.what = RECEIVED;
                    message.obj = data;
                    mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mHandler.sendEmptyMessage(Bluetooth.State.LOST);
    }

    public void write(byte[] buffer) {
        try {
            mOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
