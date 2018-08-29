package my.gomland.android.example;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Set;

import my.gomland.android.bluetooth.Bluetooth;
import my.gomland.android.bluetooth.BluetoothListener;

public class MainActivity extends AppCompatActivity implements BluetoothListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bluetooth.Helper.initialize(this, this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 111);
        }
    }


    @Override
    public void onClick(View view) {
        if(view instanceof Button){
            String text = ((Button)view).getText().toString();
            String[] device = text.split("@@");
            if(device.length > 1){
                String macAddress = device[1];
                Bluetooth.Helper.connect(Bluetooth.UuidType.UDP, macAddress);
            }
        }
    }

    @Override
    public void discoveryDevice(BluetoothDevice device) {
        if(TextUtils.isEmpty(device.getName())){
            return;
        }

        LinearLayout deviceList = findViewById(R.id.device_list);
        Button deviceBtn = new Button(this);
        deviceBtn.setText(device.getName() + "@@" + device.getAddress());
        deviceBtn.setOnClickListener(this);
        deviceList.addView(deviceBtn);
    }

    @Override
    public void discoveryFinished() {
        //TODO
    }

    @Override
    public void connectionState(int state) {
        TextView textView = findViewById(R.id.receiver);

        switch (state) {
            case Bluetooth.State.CONNECTED:
                textView.setText("CONNECTED");
                break;
            case Bluetooth.State.LOST:
            case Bluetooth.State.FAIL:
                textView.setText("CONNECTION LOST");
                break;
        }
    }

    @Override
    public void receiveMessage(String message) {
        TextView textView = findViewById(R.id.receiver);
        textView.append("\n" + message);
    }

    public void onClickDiscovery(View view) {
        LinearLayout deviceList = findViewById(R.id.device_list);
        deviceList.removeAllViews();

        Set<BluetoothDevice> devices = Bluetooth.Helper.getPairedDevice();
        for(BluetoothDevice device : devices){
            Button deviceBtn = new Button(this);
            deviceBtn.setText(device.getName() + "@@" + device.getAddress());
            deviceBtn.setOnClickListener(this);
            deviceList.addView(deviceBtn);
        }

        Bluetooth.Helper.startDiscovery();
    }

    public void onClickCancel(View view) {
        Bluetooth.Helper.cancelDiscovery();
    }

    public void onClickSend(View view) {
        EditText editText = findViewById(R.id.send_message);
        if (!TextUtils.isEmpty(editText.getText())) {
            Bluetooth.Helper.sendMessage(editText.getText().toString());
        }
    }

    public void onClickListen(View view) {
        Bluetooth.Helper.listen(Bluetooth.UuidType.SERIAL);
    }

    public void onClickDisconnect(View view) {
        Bluetooth.Helper.disconnect();
    }

}
