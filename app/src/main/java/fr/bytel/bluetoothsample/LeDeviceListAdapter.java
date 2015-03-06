package fr.bytel.bluetoothsample;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Bluetooth devices list adapter
 *
 */
public class LeDeviceListAdapter extends BaseAdapter {

    /**
     * list of bluetooth devices
     */
    private ArrayList<BluetoothDevice> mLeDevices;

    /**
     * main activity object
     */
    private BluetoothActivity main_view = null;


    /**
     * Build bluetooth device adapter
     *
     * @param main_view
     *      main activity view
     */
    public LeDeviceListAdapter(BluetoothActivity main_view) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        this.main_view=main_view;
    }

    /**
     * Add a bluetooth device to list view
     *
     * @param device
     */
    public void addDevice(BluetoothDevice device) {

        if(!mLeDevices.contains(device)) {

            Log.i(this.getClass().getName(), "New Bluetooth device found with name : " + device.getName());
            Log.i(this.getClass().getName(), "Device with address : " + device.getAddress());

            //filter only Flower Power
            if (device.getName()!=null) {
                if (device.getName().startsWith("Flower power")) {
                    this.main_view.getListViewAdapter().add(device);

                    this.main_view.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LeDeviceListAdapter.this.main_view.getListViewAdapter().notifyDataSetChanged();
                        }
                    });

                    mLeDevices.add(device);
                }
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }

    /**
     * Retrieve Bluetooth Device by position id
     *
     * @param position
     *      position id
     * @return
     *      Bluetooth device objet | null if not found
     */
    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    /**
     * Clear Bluetooth device list
     */
    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

}