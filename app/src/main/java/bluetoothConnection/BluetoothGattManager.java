package bluetoothConnection;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.CpuUsageInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import connInterface.DeviceInterface;
import entity.Sensor;
import entity.SensorData;
import entity.SensorNode;
import utils.LogManager;

class BluetoothGattManager {

    private static final String UUID_CUSTOM_CONTAINS = "ffe0";
    private static final String LOG_STRING = "BLUETOOTH_INTERFACE";

    private BluetoothGattCallback gattCallback;
    private BluetoothGatt gattCon;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gattCharacteristic;
    private DeviceInterface.connectionStatus gattCurrentState;
    private int characteristicLength;
    private String currentCharacteristic;
    private String lastCharacteristic;
    private boolean characteristicReadDone;
    private boolean servicesDiscoverDone;
    private final Object lockRead = new Object();
    private final Object lockServices = new Object();

    public BluetoothGattManager() {

        gattCallback = getGattCallBackInstance();
        gattCurrentState = DeviceInterface.connectionStatus.STATUS_DISCONNECTED;
        gattCon = null;
        gattService = null;
        gattCharacteristic = null;
        currentCharacteristic = "";
        characteristicReadDone = false;
        servicesDiscoverDone = false;
        characteristicLength = 0;
        lastCharacteristic = "";
    }

    private BluetoothGattCallback getGattCallBackInstance() {

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i("BLE STATUS", ""+status);
                switch(newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        gattCurrentState = DeviceInterface.connectionStatus.STATUS_CONNECTING;
                        Log.i("BLE STATE TO", "CONNECTED");
                        LogManager.logFile(LOG_STRING, "DISPOSITIVO CONECTADO -> " + gatt.getDevice().getAddress());
                        synchronized (lockServices) {
                            servicesDiscoverDone = true;
                            lockServices.notify();
                        }

                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        gattCurrentState = DeviceInterface.connectionStatus.STATUS_DISCONNECTED;
                        Log.i("BLE STATE TO", "DISCONNECTED");
                        LogManager.logFile(LOG_STRING, "DISPOSITIVO DESCONECTADO -> " + gatt.getDevice().getAddress());
                        gattCon = null;
                        gattService = null;
                        gattCharacteristic = null;
                        break;
                    default:
                        Log.i("BLE STATE TO", "CHANGING");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> services = gatt.getServices();
                    Log.i("BLE SERVICES", "SERVICES FOUND");
                    for (int i = 0; i < services.size(); i++) {
                        Log.i("BLE SERVICE NAME " + i, services.get(i).getUuid().toString());
                        if(services.get(i).getUuid().toString().contains(UUID_CUSTOM_CONTAINS)) {
                            List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
                                for (int j = 0; j < characteristics.size(); j++) {

                                gattService = gatt.getService(characteristics.get(j).getUuid());
                                gattCharacteristic = characteristics.get(j);
                                gattCurrentState = DeviceInterface.connectionStatus.STATUS_CONNECTED;
                                Log.i("BLE CHARACTERISTIC", gattCharacteristic.getUuid().toString());

                                gattCon.setCharacteristicNotification(gattCharacteristic, true);
                                /*
                                BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(gattCharacteristic.getUuid());
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gattCon.writeDescriptor(descriptor);
                                */

                            }
                        }
                    }
                }
                else {
                    Log.i("BLE SERVICES", "FAILED");
                }

                synchronized (lockServices) {
                    servicesDiscoverDone = true;
                    lockServices.notify();
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.i("BLE CHARAC READ VALUE", "" + characteristic.getStringValue(0));

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {

                currentCharacteristic = characteristic.getStringValue(0);
                Log.i("BLE CHARAC NOTIFY VALUE", "" + currentCharacteristic);

                /*
                if(characteristicLength == 0) {
                    currentCharacteristic = new StringBuilder("");

                    characteristicLength = Integer.valueOf(characteristic.getStringValue(0));
                }
                else {
                    currentCharacteristic.append(characteristic.getStringValue(0));
                    if(currentCharacteristic.toString().length() == characteristicLength) {
                        characteristicLength = 0;
                        lastCharacteristic = currentCharacteristic.toString();
                    }
                }
                */

//                synchronized (lockRead) {
//                    lockRead.notify();
//                }
            }
        };

        return gattCallback;
    }

    public boolean writeCharacteristicValue(byte[] value) {
        if(value == null || gattCon == null || gattCharacteristic == null)
            return false;
        if(gattCharacteristic.setValue(value)) {
            return gattCon.writeCharacteristic(gattCharacteristic);
        }
        return false;
    }

    public String readCharacteristicValue() {
        if(gattCon == null || gattCharacteristic == null)
            return null;

//        if(gattCon.readCharacteristic(gattCharacteristic)) {
//
//        }
//        try {
//            synchronized (lockRead) {
//                lockRead.wait(100);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if(!lastCharacteristic.equals(currentCharacteristic)) {
            lastCharacteristic = new String(currentCharacteristic);
            return lastCharacteristic;
        }
        else
            return "";
    }

    public boolean discoverServices() {
        servicesDiscoverDone = false;
        synchronized (lockServices) {
            try {
                lockServices.wait(30000);
                if(gattCon != null) {
                    if(gattCon.discoverServices()) {
                        Log.i("BLE SERVICES", "DISCOVERING SERVICES...");
                        lockServices.wait(30000);
                    }
                }
                else {
                    Log.i("BLE SERVICES", "DEVICE DISCONNECTED..");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return servicesDiscoverDone;
    }

    public DeviceInterface.connectionStatus getGattCurrentState() {
        return gattCurrentState;
    }

    public BluetoothGattCallback getGattCallback() {
        return gattCallback;
    }

    public BluetoothGatt getGatt() {
        return gattCon;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gattCon = gatt;
    }
/*
    private List<SensorData> processJsonData(String data) {
        Log.i("BLE GETDATA", "RECV DATA: " +data);
        List<SensorData> sensorDataList = new ArrayList<>();
        try {
            JSONObject nodeData = new JSONObject(data);
            Date dateNow = new Date();
            JSONArray sensors = nodeData.getJSONArray("SENSORS");
            for(int i = 0; i < sensors.length(); i++) {
                SensorData sensorData = new SensorData();
                sensorData.setDate(dateNow);
                //sensorData.setSensorNodeId(nodeData.getString("ID"));
                JSONObject sensor = new JSONObject(sensors.getString(i));
                sensorData.setSensorId(sensor.getString("ID"));
                sensorData.setInfo(sensor.getString("DATA"));
                sensorDataList.add(sensorData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("BLE GETDATA", "PROCESSING ERROR");
            return sensorDataList;
        }

        return sensorDataList;
    }


    private SensorNode processJsonSyncNode(String data) {
        Log.i("BLE SYNC", "SYNC DATA" +data);
        try {
            JSONObject sync = new JSONObject(data);
            SensorNode node = new SensorNode();
            node.setId(sync.getString("ID"));
            node.setName(sync.getString("NAME"));
            node.setSensorsAttached(processJsonSyncSensors(sync.getJSONArray("SENSORS")));
            if(node.getSensorsAttached().size() > 0)
                return node;

        } catch (JSONException e) {
            Log.i("BLE SYNC", "JSON: PROCESSING NODE ERROR");
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private List<Sensor> processJsonSyncSensors(JSONArray sensors) {
        List<Sensor> sensorList = new ArrayList<>();
        try {
            for(int i = 0; i < sensors.length(); i++) {
                JSONObject sensorObj = sensors.getJSONObject(i);
                Sensor sensor = new Sensor();
                sensor.setName(sensorObj.getString("NAME"));
                sensor.setId(sensorObj.getString("ID"));
                sensorList.add(sensor);
            }
            return sensorList;
        } catch (JSONException e) {
            Log.i("BLE SYNC", "JSON: PROCESSING SENSORS ERROR");
            e.printStackTrace();
            return sensorList;
        }
    }
*/
}
