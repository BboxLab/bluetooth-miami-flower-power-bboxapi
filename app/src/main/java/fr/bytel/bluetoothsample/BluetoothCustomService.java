package fr.bytel.bluetoothsample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.app.*;

import java.util.ArrayList;
import java.util.UUID;

import fr.bytel.bluetoothsample.util.ConvertFunctions;
import fr.bytel.bluetoothsample.util.ManualResetEvent;


/**
 * Bluetooth Custom service used to manage all bluetooth operations read/write/discover services...
 *
 * @author Bertrand Martel Bouygues Telecom on 24/02/15.
 */
public class BluetoothCustomService extends Service {

    private ArrayList<IFlowerPowerListener> listenersList = new ArrayList<>();

    private final static int TIMEOUT = 5000;

    public final static String LIVE_SERVICE_UUID = "39e1fa00-84a8-11e2-afba-0002a5d5c51b";
    public final static String SUNLIGHT_UUID     = "39e1fa01-84a8-11e2-afba-0002a5d5c51b";
    public final static String SOIL_EC           = "39e1fa02-84a8-11e2-afba-0002a5d5c51b";
    public final static String SOIL_TEMP         = "39e1fa03-84a8-11e2-afba-0002a5d5c51b";
    public final static String AIR_TEMP          = "39e1fa04-84a8-11e2-afba-0002a5d5c51b";
    public final static String SOIL_WC           = "39e1fa05-84a8-11e2-afba-0002a5d5c51b";

    private final static String LIVE_MEASUREMENT_PERIOD="39e1FA06-84a8-11e2-afba-0002a5d5c51b";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private ManualResetEvent eventManager = new ManualResetEvent(false);

    private final static String TAG = BluetoothCustomService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    private String mBluetoothDeviceAddress;

    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    String intentAction;

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ActionFilterGatt.ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ActionFilterGatt.ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        /*read on/off state on the device*/
                        broadcastUpdate(ActionFilterGatt.ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    System.out.println("characteristic write received ");
                    eventManager.set();
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    eventManager.set();
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ActionFilterGatt.ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
                {
                    System.out.println("descriptor write received ");
                    eventManager.set();
                }

                @Override
                // Characteristic notification
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {

                    if (characteristic.getUuid().toString().equals(SUNLIGHT_UUID))
                    {
                        double sunlight = convertLight(characteristic.getValue());

                        for (int i = 0; i < listenersList.size();i++)
                        {
                            listenersList.get(i).onSunLightChange(sunlight);
                        }
                    }
                    else if (characteristic.getUuid().toString().equals(SOIL_EC))
                    {
                        double soilElec=convertSoilEC(characteristic.getValue());

                        for (int i = 0; i < listenersList.size();i++)
                        {
                            listenersList.get(i).onSoilEcChange(soilElec);
                        }
                    }
                    else if (characteristic.getUuid().toString().equals(SOIL_TEMP))
                    {
                        double soilTemp = convertTemperature(characteristic.getValue());

                        for (int i = 0; i < listenersList.size();i++)
                        {
                            listenersList.get(i).onSoilTempChange(soilTemp);
                        }
                    }
                    else if (characteristic.getUuid().toString().equals(AIR_TEMP))
                    {
                        double airTemp = convertTemperature(characteristic.getValue());

                        for (int i = 0; i < listenersList.size();i++)
                        {
                            listenersList.get(i).onAirTempChange(airTemp);
                        }
                    }
                    else if (characteristic.getUuid().toString().equals(SOIL_WC))
                    {
                        double soilWC=convertSoilMoisture(characteristic.getValue());

                        for (int i = 0; i < listenersList.size();i++)
                        {
                            listenersList.get(i).onSoilWcChange(soilWC);
                        }
                    }
                    broadcastUpdate(ActionFilterGatt.ACTION_DATA_AVAILABLE, characteristic);
                }
            };

    public void addFlowerPowerListener(IFlowerPowerListener listener)
    {
        listenersList.add(listener);
    }

    public void readLightCharacteristic()
    {
        if (mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID)) !=null &&
                mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID))
                        .getCharacteristic(UUID.fromString(SUNLIGHT_UUID))!=null)
        {
            readCharacteristic(mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(SUNLIGHT_UUID)));
        }
    }

    public void setLiveMeasurementPeriod(int period)
    {
        System.out.println(ConvertFunctions.byteArrayToStringMessage("test",ConvertFunctions.convertIntToByteArray(period),'|'));

        if (mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID)) !=null &&
                mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID))
                        .getCharacteristic(UUID.fromString(LIVE_MEASUREMENT_PERIOD))!=null)
        {
            writeCharacteristic(mBluetoothGatt.getService(UUID.fromString(LIVE_SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(LIVE_MEASUREMENT_PERIOD)),ConvertFunctions.convertIntToByteArray(period));
        }
    }

    public void enableGattNotifications(UUID service,UUID charac)
    {
        BluetoothGattDescriptor descriptor = mBluetoothGatt.getService(service)
                .getCharacteristic(charac).getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        try {
            eventManager.setOpen(false);
            eventManager.waitOne(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enableDisableNotification(UUID service,UUID charac,boolean enable)
    {
        if (mBluetoothGatt.getService(service) !=null &&
                mBluetoothGatt.getService(service)
                        .getCharacteristic(charac)!=null)
        {
            mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(service)
                    .getCharacteristic(charac), enable);
        }
    }

    /**
     * Light is measured using phtosynthetic photon flux density (PPFD) : photon quantity in solar energy
     *
     * @param data
     *      conversion data
     * @return
     *
     */
    private double convertLight(byte[] data)
    {
        double rawValue = ConvertFunctions.convertByteArrayToInt(data) * 1.0;
        //double sunlight = 0.08640000000000001 * (192773.17000000001 * Math.pow(rawValue, -1.0606619));

        return rawValue;
    }

    private double convertSoilMoisture(byte[] data)
    {
        double rawValue = ConvertFunctions.convertByteArrayToInt(data) * 1.0;

        double soilMoisture = 11.4293 + (0.0000000010698 * Math.pow(rawValue, 4.0) - 0.00000152538 * Math.pow(rawValue, 3.0) +  0.000866976 * Math.pow(rawValue, 2.0) - 0.169422 * rawValue);

        soilMoisture = 100.0 * (0.0000045 * Math.pow(soilMoisture, 3.0) - 0.00055 * Math.pow(soilMoisture, 2.0) + 0.0292 * soilMoisture - 0.053);

        if (soilMoisture < 0.0) {
            soilMoisture = 0.0;
        } else if (soilMoisture > 60.0) {
            soilMoisture = 60.0;
        }

        return soilMoisture;
    }

    private double convertTemperature(byte[] data)
    {
        double rawValue = ConvertFunctions.convertByteArrayToInt(data) * 1.0;

        double temperature = 0.00000003044 * Math.pow(rawValue, 3.0) - 0.00008038 * Math.pow(rawValue, 2.0) + rawValue * 0.1149 - 30.449999999999999;

        if (temperature < -10.0) {
            temperature = -10.0;
        } else if (temperature > 55.0) {
            temperature = 55.0;
        }

        return temperature;
    }

    private double convertSoilEC(byte[] data)
    {
        double rawValue = ConvertFunctions.convertByteArrayToInt(data) * 1.0;

        // TODO: convert raw (0 - 1771) to 0 to 10 (mS/cm)
        double soilElectricalConductivity = rawValue;

        return soilElectricalConductivity;
    }

    /**
     * read value for a specific characterisitic
     *
     * @param charac
     *      characteristic
     */
    private void readCharacteristic(BluetoothGattCharacteristic charac)
    {
        mBluetoothGatt.readCharacteristic(charac);
        try {
            eventManager.setOpen(false);
            eventManager.waitOne(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write values to bluetooth characteristic
     *
     * @param charac
     *      bluetooth characterisitic
     * @param value
     *      value to write
     */
    private void writeCharacteristic(BluetoothGattCharacteristic charac,byte[] value)
    {
        charac.setValue(value);
        mBluetoothGatt.writeCharacteristic(charac);
        try {
            eventManager.setOpen(false);
            eventManager.waitOne(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * broadcast characteristic value
     *
     * @param action
     *      action to be sent (data available)
     * @param charac
     *      characteristic response to manage
     */
    public void broadcastUpdate(final String action,BluetoothGattCharacteristic charac) {

        ArrayList<String> values = new ArrayList<String>();
        String valueName="";

        if (charac.getUuid().toString().equals(SUNLIGHT_UUID)) {

            System.out.println(ConvertFunctions.byteArrayToStringMessage("functions",charac.getValue(),'|'));
            System.out.println(convertLight(charac.getValue()));
        }
        final Intent intent = new Intent(action);
        intent.putStringArrayListExtra(valueName,values);
        sendBroadcast(intent);
    }

    /**
     * Connect device
     */
    public boolean connect(String address)
    {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        //connect to gatt server on the device
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        return true;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void initiateNotification() {

        enableDisableNotification(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(SUNLIGHT_UUID), true);
        enableGattNotifications(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(SUNLIGHT_UUID));

        enableDisableNotification(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(SOIL_EC), true);
        enableGattNotifications(UUID.fromString(LIVE_SERVICE_UUID),UUID.fromString(SOIL_EC));


        enableDisableNotification(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(SOIL_TEMP), true);
        enableGattNotifications(UUID.fromString(LIVE_SERVICE_UUID),UUID.fromString(SOIL_TEMP));

        enableDisableNotification(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(AIR_TEMP), true);
        enableGattNotifications(UUID.fromString(LIVE_SERVICE_UUID),UUID.fromString(AIR_TEMP));

        enableDisableNotification(UUID.fromString(LIVE_SERVICE_UUID), UUID.fromString(SOIL_WC), true);
        enableGattNotifications(UUID.fromString(LIVE_SERVICE_UUID),UUID.fromString(SOIL_WC));

        setLiveMeasurementPeriod(5);
    }

    public class LocalBinder extends Binder {
        BluetoothCustomService getService() {
            return BluetoothCustomService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();
}