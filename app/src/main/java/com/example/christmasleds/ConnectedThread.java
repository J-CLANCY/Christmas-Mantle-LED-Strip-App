package com.example.christmasleds;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ConnectedThread extends Thread {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mHandler = handler;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmOutStream = tmpOut;
    }

    public void run() {
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(int input, String mode) {
        ByteBuffer buffer = ByteBuffer.allocate(6);
                //.putInt(input).array();          //converts entered String into bytes
        byte[] modeBytes = mode.getBytes();
        buffer.put(modeBytes);
        buffer.putInt(input);
        byte[] bytes = buffer.array();

        for (byte bite: bytes){
            System.out.println(bite);
        }

        /*try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }*/
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
