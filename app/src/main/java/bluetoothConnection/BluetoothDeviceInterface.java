package bluetoothConnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import connInterface.DeviceInterface;
import entity.Sensor;
import entity.SensorData;
import entity.SensorNode;

public class BluetoothDeviceInterface implements DeviceInterface {

    private BluetoothDevice bluetoothDevice;
    private BluetoothGattManager gattManager;

    private SensorNode node;

    private Context context;

    public BluetoothDeviceInterface(Context context, BluetoothDevice device) {
        gattManager = null;
        node = null;
        this.context = context;
        this.bluetoothDevice = device;
    }

    @Override
    public List<SensorData> getData() {
        List<SensorData> dataList = new ArrayList<>();
        String info = gattManager.readCharacteristicValue();
        if(!info.equals("")) {
            SensorData data = new SensorData();
            data.setDate(new Date());
            data.setInfo(info);
            data.setSensorId(this.node.getSensorsAttached().get(0).getId());
            data.setSensorNodeId(this.node.getId());
            dataList.add(data);
        }
        return dataList;
    }

    @Override
    public boolean sendData(String data) {
        return gattManager.writeCharacteristicValue(data.getBytes());
    }

    @Override
    public SensorNode connectToDevice() {
        gattManager = new BluetoothGattManager();
        gattManager.setGatt(bluetoothDevice.connectGatt(context, false, gattManager.getGattCallback()));
        if(!gattManager.discoverServices())
            return null;
        if(gattManager.getGatt() != null) {
            SensorNode node = new SensorNode();
            BluetoothDevice device = gattManager.getGatt().getDevice();
            node.setName(device.getName());
            node.setId(device.getAddress());
            Sensor sensor = new Sensor();
            sensor.setId("CUSTOM_SENSOR");
            sensor.setName("GENERIC_SENSOR");
            List<Sensor> sensors = new ArrayList<>();
            sensors.add(sensor);
            node.setSensorsAttached(sensors);
            this.node = node;
            return node;
        }
        return null;
    }

    @Override
    public void disconnectDevice() {
        gattManager.getGatt().disconnect();
    }

    @Override
    public connectionStatus checkConnection() {
        return gattManager.getGattCurrentState();
    }
}
