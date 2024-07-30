package com.example.christmasleds;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int MESSAGE_READ = 2;
    private static final int CONNECTING_STATUS = 3;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private String MAC_address;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket mBTSocket = null;
    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private TextView btStatus;
    private int mDefaultColor = 0;
    private ImageView colourWheel;
    private ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner dropdown = findViewById(R.id.spinner);
        items = new ArrayList<String>(Arrays.asList("RetroC9p", "BlueWhite", "RainbowColors", "FairyLight",
                                        "RedGreenWhite", "PartyColors", "RedWhite", "Snow", "Holly",
                                        "Ice", "HeatColors", "Irish"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = parent.getItemAtPosition(position).toString();
                System.out.println("Chosen value: " + value);
                int index = items.indexOf(value);
                System.out.println("Index of value: " + index);

                if (mConnectedThread != null){
                    mConnectedThread.write(index, "p");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        btStatus = (TextView) findViewById(R.id.bluetoothStatus);
        btStatus.setText("Status: Unconnected");
        colourWheel= (ImageView)this.findViewById(R.id.imageView);
        colourWheel.setImageResource(R.drawable.images);

        colourWheel.setOnTouchListener(new ImageView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                System.out.println("x: " + Integer.toString(x));
                System.out.println("y: " + Integer.toString(y));
                System.out.println("vx: " + Integer.toString(v.getWidth()));
                System.out.println("vy: " + Integer.toString(v.getHeight()));

                if (v == findViewById(R.id.imageView)) {
                    ImageView imageView = ((ImageView) v);
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    Bitmap bMapScaled = Bitmap.createScaledBitmap(bitmap, v.getWidth(), v.getHeight(), true);

                    System.out.println("scaled_width: " + Integer.toString(bMapScaled.getWidth()));
                    System.out.println("scaled_height: " + Integer.toString(bMapScaled.getHeight()));
                    int pixel = bMapScaled.getPixel(x, y);
                    btStatus.setTextColor(pixel);

                    if (mConnectedThread != null){
                        mConnectedThread.write(pixel, "s");
                    }
                }
                return true;
            }
        });


        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getBaseContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equals("HC-05")){
                    btStatus.setText("Status: Discovered HC-05");
                    MAC_address = device.getAddress(); // MAC address
                }
            }
        }
    };

    public void connect(View view){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            MainActivity.this.finish();
            System.exit(0);
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        checkForPaired();

        if (MAC_address == null){
            discover();
        }

        connectBT();
    }

    private void checkForPaired() {
        btStatus.setText("Status: Checking paired devices...");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")){
                    btStatus.setText("Status: Found HC-05 in Paired");
                    MAC_address = device.getAddress(); // MAC address
                }
            }
        }
    }

    private void discover() {
        // Check if the device is already discovering
        btStatus.setText("Status: Discovering...");
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectBT() {
        if(!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            return;
        }

        // Spawn a new thread to avoid blocking the GUI one
        new Thread()
        {
            @Override
            public void run() {
                boolean fail = false;

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC_address);
                btStatus.setText("Status: Creating Socket...");
                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                btStatus.setText("Status: Connecting...");
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!fail) {
                    btStatus.setText("Status: Connected");
                    mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, "HC-05")
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        if (receiver != null){
            unregisterReceiver(receiver);
        }
    }
}