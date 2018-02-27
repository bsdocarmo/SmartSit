/*
 * Copyright 2014 Pierre Chabardes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.habitus.smartsit.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.habitus.smartsit.activities.MainActivity;
import com.habitus.smartsit.postureRecognize.StandardPosture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ocean on 19/12/2017.
 */

public class BluetoothCommandService {

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    //    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    // Debugging
    private static final String TAG = "BluetoothCommandService";
    private static final boolean D = true;
    // Unique UUID for this application
    private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    // Constants that indicate command to computer
    private static final int EXIT_CMD = -1;
    private static volatile BluetoothCommandService bluetoothCommandService = new BluetoothCommandService();
    //    private int mConnectionLostCount;
    // Member fields
    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private BluetoothDevice mSavedDevice;
    private Bluetooth bt;

    //private constructor.
    private BluetoothCommandService() {

        //Prevent form the reflection api.
        if (bluetoothCommandService != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static BluetoothCommandService getInstance() {
        if (bluetoothCommandService == null) { //if there is no instance available... create new one
            synchronized (BluetoothCommandService.class) {
                if (bluetoothCommandService == null)
                    bluetoothCommandService = new BluetoothCommandService();
            }
        }

        return bluetoothCommandService;
    }

    public void setContext(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        //mConnectionLostCount = 0;
        mHandler = handler;
    }

    public void checkBonded() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
//            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "onCreate: BLUETOOTH   " + device.getName() + "-----" + device.getAddress());
//              Bluetooth adapter name
                if (device.getName().contains("HC-05") || device.getName().contains("OCEAN")) {
                    Log.d(TAG, "onCreate: BLUETOOTH is CONNECTED ");
                    bt = new Bluetooth(device.getName(), device.getAddress(), Arrays.toString(device.getUuids()));
                }
//                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            Log.d(TAG, "onCreate: BLUETOOTH IS NOT CONNECTED");
//            String noDevices = getResources().getText(R.string.none_paired).toString();
//            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * Check Bluetooth state.
     */
    public void checkBTState() {
//      Check for Bluetooth support and then check to make sure it is turned on
//      Emulator doesn't support Bluetooth and will return null
        if (mAdapter == null) {
//            Utilities.toast(this, "Bluetooth not support");
        } else {
            if (mAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth ON");
//                ensureDiscoverable();
//                if (mCommandService == null) {
//                    setupCommand();
//
//                }
                checkBonded();
                if (bt != null) {
//                  Get the Bluetooth Device object
                    Log.d(TAG, "checkBTState: BLUETOOTH - MAC CHECKBTSTATE " + bt.getMAC());
                    BluetoothDevice device = mAdapter.getRemoteDevice(bt.getMAC());
//                  Attempt to connect to the device
                    Log.d(TAG, "checkBTState:  BLUETOOTH  " + device.toString());
                    connect(device);
//                    mCommandService.connect(device);
                }
            }
        }
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // save connected device
        mSavedDevice = device;
        // reset connection lost count
        //mConnectionLostCount = 0;

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public void write(int out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
//        mConnectionLostCount++;
//        if (mConnectionLostCount < 3) {
//        	// Send a reconnect message back to the Activity
//	        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
//	        Bundle bundle = new Bundle();
//	        bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost. Reconnecting...");
//	        msg.setData(bundle);
//	        mHandler.sendMessage(msg);
//
//        	connect(mSavedDevice);
//        } else {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        connect(mSavedDevice);
//        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
//                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(myUUID));
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothCommandService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommandService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            int expect = 2;
//            byte[] buffer = new byte[n];
            // Keep listening to the InputStream while connected
            while (true) {

                try {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    int bytesRead = -1;

                    /*  Lê os bytes recebidos e os armazena no buffer até que
                    uma quebra de linha seja identificada. Nesse ponto, assumimos
                    que a mensagem foi transmitida por completo.
                     */
                    do {
                        bytes = mmInStream.read(buffer, bytesRead+1, 1);
                        bytesRead+=bytes;

                    } while(buffer[bytesRead] != '\n');
//
//                    /*  A mensagem recebida é enviada para a Activity principal.
//                     */
////                    toMainActivity(Arrays.copyOfRange(buffer, 0, bytesRead-1));
                    String strIncom = new String(buffer, "UTF8");

                    StandardPosture standardPosture = StandardPosture.getInstance();
                    float[][] standardMat = standardPosture.getStandardMat();


                    Log.d(TAG, "buffer - :>>>  " + strIncom);
                    Log.d("TAG2", standardMat[0][0] + "     " + standardMat[0][1] + "     " + standardMat[1][0] + "     " + standardMat[1][1]);
//                    mHandler.obtainMessage(MainActivity.RECEIVE_MESSAGE, 0, -1, Arrays.copyOfRange(buffer, 0, bytesRead-1)).sendToTarget();

//                    // Read from the InputStream
//                    int bytes = mmInStream.read(buffer);;

//                    Log.d(TAG, "run: " + bytes);
                    // Read from the InputStream

//                    String strIncom = new String(buffer);
//                    Log.d(TAG, "buffer - :>>>  " + strIncom);
                    mHandler.obtainMessage(MainActivity.RECEIVE_MESSAGE, expect, -1, strIncom).sendToTarget();     // Send to message queue Handler
////                    if(bytes>= expect){
//                        Log.d(TAG, "buffer lenght >>" + buffer.length);
//                        Log.d(TAG, "bytes  lenght >>" + bytes);
//                        Log.d(TAG, "buffer - :>>>  " + strIncom);
////                    }
////                    if(buffer.length >= n){
////                    }
//                    // Send the obtained bytes to the UI Activity
////                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
////                            .sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        void write(int out) {
            try {
                mmOutStream.write(out);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        void cancel() {
            try {
                mmOutStream.write(EXIT_CMD);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}