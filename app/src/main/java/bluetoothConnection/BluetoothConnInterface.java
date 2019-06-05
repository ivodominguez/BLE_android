package bluetoothConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import connInterface.ConnectionInterface;
import connInterface.DeviceInterface;
import utils.LogManager;

/**
 * Created by Diego Justi on 4/16/2018.
 */

public class BluetoothConnInterface implements ConnectionInterface{

    private static final String LOG_STRING = "BLUETOOTH_INTERFACE";

    private static final long SCAN_DURATION = 5000;

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private ScanSettings scanSettings;
    private long scanDuration_ms;

    private Handler mHandler;

    private List<ScanFilter> scanFilters;
    private final List<BluetoothDevice> leDeviceList;

    private final Object lock = new Object();

    private static BluetoothConnInterface instance = null;

    public static BluetoothConnInterface getInstance(Context context) {
        if(instance == null) {
            instance = new BluetoothConnInterface(context);
        }
        return instance;
    }

    private BluetoothConnInterface(Context context) {
        this.context = context;
        scanDuration_ms = SCAN_DURATION;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        leDeviceList = Collections.synchronizedList(new ArrayList<BluetoothDevice>());
        mHandler = new Handler();
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
        scanFilters = new ArrayList<>();

    }

    public boolean bluetoothInitialize(){

        if(bluetoothAdapter == null)
            return false;
        if (!bluetoothAdapter.isEnabled()) {
            if(!bluetoothAdapter.enable()) {
                return false;
            }
        }

       // Log.i("BLE", "BLUETOOTH ACTIVATED");

        return true;
    }

    public void scanBluetoothLeDevices(boolean scan) {

        if(scan) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    leScanner.stopScan(mScanCallBack);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }, SCAN_DURATION);

            leScanner.startScan(scanFilters, scanSettings, mScanCallBack);

        } else {
            synchronized (lock) {
                lock.notify();
            }
            leScanner.stopScan(mScanCallBack);
        }
    }

    private ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("BLE SCAN", "RESULT: "+result.toString());
            LogManager.logFile(LOG_STRING, "DISPOSITIVO ENCONTRADO -> " + result.getDevice().toString());

                //MUDAR PARA ALGO MAIS DECENTE
            if (result.toString().contains("mServiceUuids=[0000ffe0")) {
//                  connectToDevice(result.getDevice());
                if (leDeviceList.size() > 0) {
                    for (int i = 0; i < leDeviceList.size(); i++) {
                        if (!result.getDevice().getAddress().equals(leDeviceList.get(i).getAddress())) {
                            Log.i("BLE SCAN", "DEVICE ADDED: " + result.toString());
                            leDeviceList.add(result.getDevice());
                        } else {
                            Log.i("BLE SCAN", "DEVICE ALREADY ON LIST: " + result.toString());
                        }
                    }
                } else {
                    leDeviceList.add(result.getDevice());
                    Log.i("BLE SCAN", "DEVICE ADDED: " + result.toString());
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult sr : results) {
                Log.i("BLE SCAN", "BATCH RES: "+sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("BLE SCAN", "ERROR: "+errorCode);
        }
    };
/*
    private void connectToDevice(BluetoothDevice device) {
        Log.i("BLE CONN", "TRYING CONNECTION WITH: "+device.getAddress());
        gattManagerList.add(new BluetoothGattManager());
        BluetoothGatt gatt;
        gatt = device.connectGatt(context, false, gattManagerList.get(gattManagerList.size()-1).getGattCallback());
        gattManagerList.get(gattManagerList.size()-1).setGatt(gatt);
//      scanLeDevices(false);
    }
*/
    //BEFORE API 21, THE CALLBACK IS BELOW
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            /*
            for(int j = 0; j < leDeviceList.size(); j++) {
                if (!leDeviceList.get(j).getAddress().equals(bluetoothDevice.getAddress())) {
                    leDeviceList.add(bluetoothDevice);
                    Log.i("BLE SCAN CALLBACK", "DEVICE ADDED:" + bluetoothDevice.getName() + "-" + bluetoothDevice.getAddress());
                }
                else
                    Log.i("BLE SCAN CALLBACK", "DEVICE ALREADY ADDED:" + bluetoothDevice.getName() + "-" + bluetoothDevice.getAddress());
            }
            **/
        }
    };

    private List<DeviceInterface> getDeviceList() {
        List<DeviceInterface> deviceList = new ArrayList<>();
        List<BluetoothDevice> devicesTemp = new ArrayList<>();
        if(leDeviceList.size() == 0) {
            return deviceList;
        }

        devicesTemp.add(leDeviceList.get(0));
        for(int i = 1; i < leDeviceList.size(); i++) {
            boolean found = false;
            for(int j = 0; j < devicesTemp.size(); j++) {
                if (devicesTemp.get(j).getAddress().equals(leDeviceList.get(i).getAddress())) {
                   found = true;
                }
            }
            if(!found) {
                devicesTemp.add(leDeviceList.get(i));
            }
        }

        Log.i("BLE SCAN", "" + "DEVS FOUND: " + devicesTemp.size());
        for(int i = 0; i < devicesTemp.size(); i++) {
            Log.i("BLE SCAN", "" + "CREATING A DEV: " + devicesTemp.get(i).getAddress());
            BluetoothDeviceInterface deviceInterface = new BluetoothDeviceInterface(context, devicesTemp.get(i));
            deviceList.add(deviceInterface);
        }
        leDeviceList.clear();
        return deviceList;
    }


    @Override
    public boolean startService() {
        if(instance != null) {
            return instance.bluetoothInitialize();
        }
        return false;
    }

    @Override
    public boolean endService() {
        if(instance != null) {
            return instance.bluetoothAdapter.disable();
        }
        return false;
    }

    @Override
    public List<DeviceInterface> scanDevices() {
        if(instance != null) {
            instance.scanBluetoothLeDevices(true);
            try {
                synchronized (lock) {
                    lock.wait();
                }
                Log.i("BLE SCAN", "DONE");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getDeviceList();
        }
        return null;
    }

    public long getScanDuration_ms() {
        return scanDuration_ms;
    }

    public void setScanDuration_ms(long scanDuration_ms) {
        this.scanDuration_ms = scanDuration_ms;
    }
}
