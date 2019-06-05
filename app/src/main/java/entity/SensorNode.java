package entity;

import java.util.ArrayList;
import java.util.List;

import connInterface.DeviceInterface;

/**
 *
 * @author Diego Justi
 */
public class SensorNode {

    public static final long DATA_EXCHANGE_INTERVAL = 5000;

    private String id;
    private String name;
    private List<Sensor> sensorsAttached;
    //private List<SensorData> sensorsData;
    private DeviceInterface connInterface;
    private long dataExchangeInterval_ms;
    private long lastExchangeTime_ms;
    private boolean isSink;

    public SensorNode(String id, String name, DeviceInterface deviceInterface, long dataExchangeInterval_ms, boolean isSink) {
        this.id = id;
        this.name = name;
        this.dataExchangeInterval_ms = dataExchangeInterval_ms;
        this.isSink = isSink;
        this.connInterface = deviceInterface;
        lastExchangeTime_ms = 0;
        sensorsAttached = new ArrayList<>();
    }


    public SensorNode() {
        id = "";
        name = "";
        dataExchangeInterval_ms = DATA_EXCHANGE_INTERVAL;
        isSink = false;
        connInterface = null;
        lastExchangeTime_ms = 0;
        sensorsAttached = new ArrayList<>();
    }

    public long getLastExchangeTime_ms() {
        return lastExchangeTime_ms;
    }

    public void setLastExchangeTime_ms(long lastExchangeTime_ms) {
        this.lastExchangeTime_ms = lastExchangeTime_ms;
    }

    public boolean addSensor(Sensor sensor)  {
        return sensorsAttached.add(sensor);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sensor> getSensorsAttached() {
        return sensorsAttached;
    }

    public void setSensorsAttached(List<Sensor> sensorsAttached) {
        this.sensorsAttached = sensorsAttached;
    }

    public long getDataExchangeInterval_ms() {
        return dataExchangeInterval_ms;
    }

    public void setDataExchangeInterval_ms(long dataExchangeInterval_ms) {
        this.dataExchangeInterval_ms = dataExchangeInterval_ms;
    }

    public boolean isSink() {
        return isSink;
    }

    public void setIsSink(boolean isSink) {
        this.isSink = isSink;
    }

    public DeviceInterface getConnInterface() {
        return connInterface;
    }

    public void setConnInterface(DeviceInterface connInterface) {
        this.connInterface = connInterface;
    }

}
