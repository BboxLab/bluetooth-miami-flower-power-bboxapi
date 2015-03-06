package fr.bytel.bluetoothsample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import fr.bytel.bluetoothsample.R;
import fr.bytel.bluetoothsample.bboxapi.BboxManager;
import fr.bytel.bluetoothsample.network.BoxNetworkTemplate;
import fr.bytel.bluetoothsample.network.NetworkFunctions;
import fr.bytel.bluetoothsample.util.ConvertFunctions;

/**
 * Sample activity featuring scanning and ON/OFF on Awox Aroma light led
 *
 * @author Bertrand Martel Bouygues Telecom on 24/02/15.
 */
public class BluetoothActivity extends Activity {

    public static String PACKAGE_NAME;

    private BboxManager bboxManager = null;

    /**
     * define if device is connected
     */
    private boolean connected = false;

    /**
     * list view device selected position
     */
    private int list_item_position = 0;

    private BluetoothCustomService mBluetoothDeviceService = null;

    private String mDeviceAddress = "";

    private static final long SCAN_PERIOD = 10000;

    /**
     * view featuring devices
     */
    private ListView device_list_view = null;

    /**
     * message handler
     */
    private Handler mHandler=null;
    /**
     * List of bluetooth device manager
     */
    private LeDeviceListAdapter mLeDeviceListAdapter=null;

    /**
     * Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * define if bluetooth is enabled on device
     */
    private final static int REQUEST_ENABLE_BT = 1;

    /**œ
     * define if bluetooth scanning is active or not
     */
    private boolean mScanning=false;

    /**
     * list view adapter attached to new device view
     */
    private StableArrayAdapter list_view_adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth Smart is not supported on your device", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /*initialize bluetooth scanning objects*/
        if (mLeDeviceListAdapter!=null)
        {
            mLeDeviceListAdapter.clear();
        }
        else
        {
            mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        }

        mHandler=null;
        mHandler=new Handler();

        /* scanning button */
        final Button button_find_accessory = (Button) findViewById(R.id.scanning_button);

        final Switch button_switch = (Switch) findViewById(R.id.switch1);

        /*
        button_switch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (connected)
                {
                    mBluetoothDeviceService.setOnOff(button_switch.isChecked());
                }
            }
        });
        */
        button_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
            }

        });

        button_find_accessory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mScanning) {
                    Toast.makeText(BluetoothActivity.this, "Looking for new accessories", Toast.LENGTH_SHORT).show();
                    mBluetoothDeviceService.close();
                    list_view_adapter.clear();
                    list_view_adapter.notifyDataSetChanged();
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    scanLeDevice(true);
                }
                else
                {
                    Toast.makeText(BluetoothActivity.this, "Scanning already engaged...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        device_list_view = (ListView) findViewById(R.id.listView);

        final ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

        list_view_adapter = new StableArrayAdapter(this,R.layout.new_device_layout, list,this);

        device_list_view.setAdapter(list_view_adapter);

        device_list_view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                //pairingDialog.show(AccessoryList.this, "","Association en cours...", true);

                /*stop scanning*/
                if (mScanning==true) {

                    mHandler.removeCallbacksAndMessages(null);

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    Toast.makeText(BluetoothActivity.this, "Device has been successfully discovered", Toast.LENGTH_SHORT).show();

                }

                list_item_position=position;

                /*connect to bluetooth gatt server on the device*/
                mDeviceAddress=list_view_adapter.getItem(position).getAddress();
                mBluetoothDeviceService.connect(mDeviceAddress);
            }

        });

        ArrayList<BoxNetworkTemplate> network = NetworkFunctions.getIpList();
        System.out.println(network.size());
        if (network.size()>0) {
            this.bboxManager = new BboxManager(getApplicationContext(), network.get(0).getIpAddress());
        }

        Intent gattServiceIntent = new Intent(this, BluetoothCustomService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * Callback for Bluetooth adapter
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();

                }
            });
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (ActionFilterGatt.ACTION_GATT_CONNECTED.equals(action)) {
                connected=true;
                device_list_view.getChildAt(list_item_position).setBackgroundColor(Color.GREEN);
                //mConnected = true;
                //updateConnectionState();
                invalidateOptionsMenu();

            } else if (ActionFilterGatt.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mConnected = false;
                //updateConnectionState();
                invalidateOptionsMenu();

            } else if (ActionFilterGatt.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                mBluetoothDeviceService.initiateNotification();

            }
            else if (ActionFilterGatt.ACTION_DATA_AVAILABLE.equals(action)) {

                if (bboxManager!=null && !bboxManager.getSessionId().equals(""))
                {

                }


                if (intent.getStringArrayListExtra("STATUS")!=null)
                {
                    ArrayList<String> values = intent.getStringArrayListExtra("STATUS");
                    if (values.size()>0)
                    {
                        if (values.get(0).toString().equals("ON"))
                        {
                            Switch button_switch = (Switch) findViewById(R.id.switch1);
                            button_switch.setChecked(true);
                        }
                        else if (values.get(0).toString().equals("OFF"))
                        {
                            Switch button_switch = (Switch) findViewById(R.id.switch1);
                            button_switch.setChecked(false);
                        }
                    }
                }



            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothDeviceService != null) {
            //final boolean result = mBluetoothDeviceService.connect(mDeviceAddress);
            //Log.i(ContentValues.TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothDeviceService = ((BluetoothCustomService.LocalBinder) service).getService();
            if (!mBluetoothDeviceService.initialize()) {
                Log.e(ContentValues.TAG, "Unable to initialize Bluetooth");
                finish();
            }
            else
            {
                //send notification via bbox API
                mBluetoothDeviceService.addFlowerPowerListener(new IFlowerPowerListener() {
                    @Override
                    public void onSunLightChange(double value) {
                        bboxManager.buildBleNotification("sunlight",value,"ppf");
                    }

                    @Override
                    public void onSoilEcChange(double soilEC) {
                        bboxManager.buildBleNotification("soilEC",soilEC,"mS/cm");
                    }

                    @Override
                    public void onSoilTempChange(double temp) {
                        bboxManager.buildBleNotification("soilTemp",temp,"celsius");
                    }

                    @Override
                    public void onAirTempChange(double temp) {
                        bboxManager.buildBleNotification("airTemp",temp,"celsius");
                    }

                    @Override
                    public void onSoilWcChange(double wc) {
                        bboxManager.buildBleNotification("soilWC",wc,"0-60");
                    }
                });
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothDeviceService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothDeviceService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mServiceConnection!=null) {
            unbindService(mServiceConnection);
        }
    }

    /**
     * Scan new Bluetooth device
     *
     * @param enable
     *      true if bluetooth start scanning / stop scanning if false
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BluetoothActivity.this, "End of scanning...", Toast.LENGTH_SHORT).show();
                            mScanning = false;
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    /**
     * Getter for list view adapter
     *
     * @return
     *      list view adapter
     */
    public StableArrayAdapter getListViewAdapter()
    {
        return list_view_adapter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * add filter to intent to receive notification from bluetooth service
     *
     * @return
     *      intent filter
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ActionFilterGatt.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
