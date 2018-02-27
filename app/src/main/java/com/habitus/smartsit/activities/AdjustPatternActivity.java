package com.habitus.smartsit.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.habitus.smartsit.R;
import com.habitus.smartsit.adapter.SensorAdapter;
import com.habitus.smartsit.bluetooth.Bluetooth;
import com.habitus.smartsit.bluetooth.BluetoothCommandService;
import com.habitus.smartsit.postureRecognize.CurrentPosture;
import com.habitus.smartsit.postureRecognize.DefaultValues;
import com.habitus.smartsit.postureRecognize.StandardPosture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdjustPatternActivity extends AppCompatActivity {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int RECEIVE_MESSAGE = 9;
    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "Device";
    public static final String TOAST = "toast";
    private static final String TAG = "MAINACTIVITY";
    // bluetooth
    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 2;
    private static boolean podeCalibrar = false;
    private static GridView gridView;
    private static ProgressDialog mDialog;
    private List<CurrentPosture> postures = new ArrayList<>();
    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Log.d(TAG, "handleMessage: " + msg.toString());
            switch (msg.what) {
                case RECEIVE_MESSAGE:
//                  if receive massage
//                  byte[] readBuf = (byte[]) msg.obj;
//                  String strIncom = new String(readBuf, 0, msg.arg1);
//                  create string from bytes array
                    String strIncom = (String) msg.obj;

//                  String strIncom = new String(Arrays.toString(readBuf));
//                  if(strIncom.length() >= msg.arg1){

                    try {
//                      ToDo: Chamar a classe para análise desses dados por aqui.
//                      ToDo: Receber do arduino a quantidade de sensores e onde esta cada um.
                        JSONObject obj = new JSONObject(strIncom);

                        if (podeCalibrar) {

                            CurrentPosture currentPosture = new CurrentPosture();
                            float[][] currentMat = new float[DefaultValues.getM()][DefaultValues.getN()];

                            currentMat[0][0] = Float.parseFloat(obj.getString("a"));
                            currentMat[0][1] = Float.parseFloat(obj.getString("b"));
                            currentMat[1][0] = Float.parseFloat(obj.getString("c"));
                            currentMat[1][1] = Float.parseFloat(obj.getString("d"));
                            currentPosture.setCurrentMat(currentMat);

                            postures.add(currentPosture);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    }
//                    Log.d(TAG, " RECEIVE_MESSAGE --> " + strIncom );
//                    toast.makeText(MainActivity.this, "Data from Android : " + strIncom, toast.LENGTH_SHORT).show();
//                    strIncom = "";
//                    sb.append(strIncom);                                                // append string
//                    int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
//                    if (endOfLineIndex > 0) {                                            // if end-of-line,
//                        String sbprint = sb.substring(0, endOfLineIndex);               // extract string
//                        sb.delete(0, sb.length());                                      // and clear
//                        toast.makeText(MainActivity.this, "Data from Android : " + sbprint, toast.LENGTH_SHORT).show();
////                        txtArduino.setText("Data from Arduino: " + sbprint);            // update TextView
////                        btnOff.setEnabled(true);
////                        btnOn.setEnabled(true);
//                    }
//                    Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
//                    Log.d(TAG, sb.toString());
                    break;
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommandService.STATE_CONNECTED:
//                            for(int i=0; i<10;i++){
//                                mConnectedThread.write(i);
//                            }
//                            mTitle.setText(R.string.title_connected_to);
//                            mTitle.append(mConnectedDeviceName);
                            break;
                        case BluetoothCommandService.STATE_CONNECTING:
//                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothCommandService.STATE_LISTEN:
                            break;
                        case BluetoothCommandService.STATE_NONE:
//                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
//                  Save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    private BluetoothCommandService mCommandService;
    private Bluetooth bt;
    private List<Integer> list = new ArrayList<>();
    private Button confirm;
    private Button prosseguir;

    public static void animateImageView(final ImageView v, final int c) {

        final ValueAnimator colorAnim = ObjectAnimator.ofFloat(0f, 1f);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (Float) animation.getAnimatedValue();
                int alpha = adjustAlpha(c, factor);
                v.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP);
                if (factor == 0.0) {
                    v.setColorFilter(null);
                }
            }
        });

        colorAnim.setDuration(7000);
        colorAnim.start();

    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_pattern);

        confirm = findViewById(R.id.confirm);
        prosseguir = findViewById(R.id.prosseguir);

        mCommandService = mCommandService.getInstance();
        mCommandService.setContext(this, mHandler);
        if (mCommandService.getState() == 0)
            mCommandService.checkBTState();

        DefaultValues.setM(2);
        DefaultValues.setN(2);
        DefaultValues.setQTD_SENSORS();

        for (int i = 0; i < DefaultValues.getQTD_SENSORS(); i++) {
            list.add(R.drawable.sensor);
        }

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        checkBTState();

        gridView = findViewById(R.id.adp_sensor);

        gridView.setNumColumns(DefaultValues.getN());
        gridView.setVerticalSpacing(40);
        gridView.setAdapter(new SensorAdapter(this, list));
    }

    public void confirm(View view) {
        confirm.setVisibility(View.GONE);
        prosseguir.setVisibility(View.VISIBLE);
        mDialog = new ProgressDialog(AdjustPatternActivity.this, R.drawable.progress_bar);
        mDialog = ProgressDialog.show(this,"Calibrando", "Espere um momento, por favor.",true);
        WaitTime wait = new WaitTime();
        wait.execute();
    }

    public void prosseguir(View view) {
        calcStandart();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void calcStandart() {
        int[] soma = new int[4];
        for (int i = 0; i < postures.size(); i++) {
            soma[0] += postures.get(i).getCurrentMat()[0][0];
            soma[1] += postures.get(i).getCurrentMat()[0][1];
            soma[2] += postures.get(i).getCurrentMat()[1][0];
            soma[3] += postures.get(i).getCurrentMat()[1][1];
        }

        StandardPosture standardPosture = StandardPosture.getInstance();
        float[][] standardMat = standardPosture.getStandardMat();

        standardMat[0][0] = soma[0] / 4;
        standardMat[0][1] = soma[1] / 4;
        standardMat[1][0] = soma[2] / 4;
        standardMat[1][1] = soma[3] / 4;
        standardPosture.setStandardMat(standardMat);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void sendMessage(JSONObject msg) {
        byte[] msgToBT = msg.toString().getBytes();
        mCommandService.write(msgToBT);
    }

    private void sendMessage(String msg) {
        byte[] msgToBT = msg.getBytes();
        mCommandService.write(msgToBT);
    }

//    private void checkBonded() {
//        // Get a set of currently paired devices
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//
//        // If there are paired devices, add each one to the ArrayAdapter
//        if (pairedDevices.size() > 0) {
////            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
//            for (BluetoothDevice device : pairedDevices) {
//                Log.d(TAG, "onCreate: BLUETOOTH   " + device.getName() + "-----" + device.getAddress());
////              Bluetooth adapter name
//                if (device.getName().contains("HC-05") || device.getName().contains("OCEAN")) {
//                    Log.d(TAG, "onCreate: BLUETOOTH is CONNECTED ");
//                    bt = new Bluetooth(device.getName(), device.getAddress(), Arrays.toString(device.getUuids()));
//                }
////                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//            }
//        } else {
//            Log.d(TAG, "onCreate: BLUETOOTH IS NOT CONNECTED");
////            String noDevices = getResources().getText(R.string.none_paired).toString();
////            mPairedDevicesArrayAdapter.add(noDevices);
//        }
//    }
//
//    private void setupCommand() {
//        // Initialize the BluetoothChatService to perform bluetooth connections
//        mCommandService = new BluetoothCommandService(this, mHandler);
//        Log.d(TAG, "setupCommand: Connected with the class");
//    }

//    /**
//     * Check Bluetooth state.
//     */
//    private void checkBTState() {
////      Check for Bluetooth support and then check to make sure it is turned on
////      Emulator doesn't support Bluetooth and will return null
//        if (mBluetoothAdapter == null) {
//            Utilities.toast(this, "Bluetooth not support");
//        } else {
//            if (mBluetoothAdapter.isEnabled()) {
//                Log.d(TAG, "Bluetooth ON");
////                ensureDiscoverable();
//                if (mCommandService == null) {
//                    setupCommand();
//                    checkBonded();
//                }
//
//                if (bt != null) {
////                  Get the Bluetooth Device object
//                    Log.d(TAG, "checkBTState: BLUETOOTH - MAC CHECKBTSTATE " + bt.getMAC());
//                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bt.getMAC());
////                  Attempt to connect to the device
//                    Log.d(TAG, "checkBTState:  BLUETOOTH  " + device.toString());
//                    mCommandService.connect(device);
//                }
//            } else {
//                try {
//                    Utilities.toast(this, "Bluetooth is OFF");
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                } catch (Exception ignored) {
//                }
//            }
//        }
//    }

    private static class WaitTime extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            podeCalibrar = true;
            mDialog.show();
            animateImageView((ImageView) gridView.getChildAt(0), Color.rgb(0, 255, 0));
            animateImageView((ImageView) gridView.getChildAt(1), Color.rgb(0, 255, 0));
            animateImageView((ImageView) gridView.getChildAt(2), Color.rgb(0, 255, 0));
            animateImageView((ImageView) gridView.getChildAt(3), Color.rgb(0, 255, 0));
        }

        protected void onPostExecute() {
            mDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mDialog.dismiss();
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... params) {
            long delayInMillis = 5000;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    podeCalibrar = false;
                    mDialog.dismiss();
                }
            }, delayInMillis);
            return null;
        }
    }
}
