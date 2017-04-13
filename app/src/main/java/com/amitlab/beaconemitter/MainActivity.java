package com.amitlab.beaconemitter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseSettings mAdvertiseSettings;
    private AdvertiseData mAdvertiseData;

    /* Full Bluetooth UUID that defines the Health Thermometer Service */
    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView mTextView = (TextView)this.findViewById(R.id.msg);
        //mTextView.setText("Hello Amit !");

        String result = checkTransmissionSupported(this);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            //Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            //finish();
//            Log.i(TAG, "BLE Not Supported.");
//            mTextView.setText("BLE Not Supported.");
//        }

        if(result == "SUPPORTED")
        {
                Log.i(TAG, "BLE Supported.");
                mTextView.setText("BLE Supported :))");
                // Get the default adapter
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            boolean a = mBluetoothAdapter.isMultipleAdvertisementSupported();
            boolean b = mBluetoothAdapter.isOffloadedFilteringSupported();
            boolean c = mBluetoothAdapter.isOffloadedScanBatchingSupported();

            if(a && b && c) {
                mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                setAdvertiseSettings();
                setAdvertiseData();

                mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
            }
            else {
                Log.i(TAG, "BLE Not Supported.");
                mTextView.setText("BLE Not Supported.");
            }
        }
        else
        {
            Log.i(TAG, "BLE Not Supported.");
            mTextView.setText("BLE Not Supported.");
        }
    }

    protected void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();

        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        mBuilder.setConnectable(false);
        mBuilder.setTimeout(0);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);

        mAdvertiseSettings = mBuilder.build();
    }

    protected void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();

        /*ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = getIdAsByte(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020"));
        mManufacturerData.put(0, (byte)0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte)0xAC); // Beacon Identifier
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte)0x00); // first byte of Major
        mManufacturerData.put(19, (byte)0x09); // second byte of Major
        mManufacturerData.put(20, (byte)0x00); // first minor
        mManufacturerData.put(21, (byte)0x06); // second minor
        mManufacturerData.put(22, (byte)0xB5); // txPower
        mBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID*/

        mBuilder.setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(THERM_SERVICE)
                .addServiceData(THERM_SERVICE, buildTempPacket())
                .build();

        mAdvertiseData = mBuilder.build();
    }

    public byte[] getIdAsByte(UUID uuid)
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };

    private byte[] buildTempPacket() {
        int value;
        try {
            //value = Integer.parseInt(mCurrentValue.getText().toString());
            value = 5;
        } catch (NumberFormatException e) {
            value = 0;
        }

        return new byte[] {(byte)value, 0x00};
    }

    public static String checkTransmissionSupported(Context context) {
        String returnCode = "SUPPORTED";

        if (android.os.Build.VERSION.SDK_INT < 21) {
            returnCode = "NOT_SUPPORTED_MIN_SDK";
        }
        else if (!context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            returnCode = "NOT_SUPPORTED_BLE";
        }
        else {
            try {
                // Check to see if the getBluetoothLeAdvertiser is available.  If not, this will throw an exception indicating we are not running Android L
                if (((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeAdvertiser() == null) {
                    if (!((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isMultipleAdvertisementSupported()) {
                        returnCode = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS";
                    }
                    else {
                        returnCode = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER";
                    }
                }
            } catch (Exception e) {
                returnCode = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER";
            }
        }

        return returnCode;
    }
}
