package com.st.sn.bluetoothdevices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter blueTAdapter;
    private ListView lvDevices;
    private ArrayList<Device> devArrList;
    private ArrayAdapter<Device> devArrAdapter;
    class Device{
        int signal;
        String id;
        Device(int signal, String id){
            this.signal = signal;
            this.id = id;
        }
        @Override
        public String toString()
        {
            return this.id + "  (" + this.signal + "дБм)";
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devArrList = new ArrayList<Device>();
        devArrAdapter = new ArrayAdapter<Device>(getApplicationContext(), R.layout.custom_textview, devArrList);
        lvDevices = (ListView) findViewById(R.id.lv_devices);
        lvDevices.setAdapter(devArrAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bcastReceiver, filter);
        blueTAdapter = BluetoothAdapter.getDefaultAdapter();
        final Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                if(blueTAdapter != null) {
                    if (blueTAdapter.isDiscovering()){
                        blueTAdapter.cancelDiscovery();
                        btn.setText("Начать новый поиск");
                    }
                    else{
                        btn.setText("Остановить поиск");
                        devArrAdapter.clear();
                        devArrAdapter.notifyDataSetChanged();
                        blueTAdapter.startDiscovery();
                    }
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blueTAdapter != null) {
            blueTAdapter.cancelDiscovery();
        }
        unregisterReceiver(bcastReceiver);
    }

    private final BroadcastReceiver bcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                devArrAdapter.add(new Device(rssi,device.getName()));
                devArrAdapter.sort(new Comparator<Device>() {
                    @Override
                    public int compare(Device lhs, Device rhs) {
                        return rhs.signal - lhs.signal;
                    }
                });
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Button btn_ = (Button) findViewById(R.id.button);
                btn_.setText("Начать новый поиск");
            }
        }
    };
}
