package middlewareTECUIDA;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.Sensor;
import entity.SensorNode;

/**
 *
 * @author Diego Justi
 */

public class SensorNodesManager {

    private static SensorNodesManager instance = null;
    private List<SensorNode> sensorsNodes;

    private SensorNodesManager() {
        sensorsNodes = Collections.synchronizedList(new ArrayList<SensorNode>());
    }

    private SensorNodesManager(List<SensorNode> sensorsNodes) {
        this.sensorsNodes = sensorsNodes;
    }


    public static SensorNodesManager getInstance() {
        if (instance == null) {
            instance = new SensorNodesManager();
        }

        return instance;
    }

    public static SensorNodesManager getInstance(List<SensorNode> sensorsNodes) {
        if (instance == null) {
            instance = new SensorNodesManager(sensorsNodes);
        }

        return instance;
    }

    public boolean removeSensorFromSensorNode(Sensor sensor, SensorNode sensorNode) {

        List<Sensor> sensors = null;

        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId()) || (sensorsNodes.get(i) == sensorNode)) {
                sensors = sensorsNodes.get(i).getSensorsAttached();
                break;
            }
        }

        if (sensors != null) {
            for (int i = 0; i < sensors.size(); i++) {
                if (sensors.get(i).getId().equals(sensor.getId()) || (sensors.get(i) == sensor)) {
                    return sensors.remove(sensors.get(i));
                }
            }
        }

        return false;
    }

    public boolean editSensorFromSensorNode(Sensor sensor, SensorNode sensorNode) {

        List<Sensor> sensors = null;

        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId()) || (sensorsNodes.get(i) == sensorNode)) {
                sensors = sensorsNodes.get(i).getSensorsAttached();
                break;
            }
        }

        if (sensors != null) {
            for (int i = 0; i < sensors.size(); i++) {
                if (sensors.get(i).getId().equals(sensor.getId()) || (sensors.get(i) == sensor)) {
                    Sensor s = sensors.get(i);
                    s.setActive(sensor.isActive());
                    s.setId(sensor.getId());
                    s.setName(sensor.getName());
                    return true;
                }
            }
        }

        return false;
    }

    public boolean addSensorToSensorNode(Sensor sensor, SensorNode sensorNode) {

        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId()) || (sensorsNodes.get(i) == sensorNode)) {
                return sensorsNodes.get(i).addSensor(sensor);
            }
        }
        return false;
    }

    public boolean addSensorNode(SensorNode sensorNode) {
        return sensorsNodes.add(sensorNode);
    }

    public boolean removeSensorNode(SensorNode sensorNode) {
        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId())) {
                return sensorsNodes.remove(sensorsNodes.get(i));
            }
        }

        return sensorsNodes.remove(sensorNode);
    }

    public SensorNode removeSensorNode(int i) {
        if(i >= 0 && i < sensorsNodes.size()) {
            return sensorsNodes.remove(i);
        }
        return null;
    }

    public SensorNode getSensorNode(SensorNode sensorNode) {
        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId()) || (sensorsNodes.get(i) == sensorNode)) {
                return sensorsNodes.get(i);
            }
        }
        return null;
    }

    public boolean setSensorNodeDataExchangeInterval(SensorNode sensorNode, long dataExchangeInterval_ms) {
        if(dataExchangeInterval_ms < 100)
            return false;
        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(sensorNode.getId()) || (sensorNode == sensorsNodes.get(i))) {
                sensorsNodes.get(i).setDataExchangeInterval_ms(dataExchangeInterval_ms);
                return true;
            }
        }
        return false;
    }

    public boolean editSensorNode(SensorNode newSensorNode, SensorNode oldSensorNode) {
        for (int i = 0; i < sensorsNodes.size(); i++) {
            if (sensorsNodes.get(i).getId().equals(oldSensorNode.getId()) || sensorsNodes.get(i) == oldSensorNode) {
                SensorNode sn = sensorsNodes.get(i);
                sn.setId(newSensorNode.getId());
                sn.setDataExchangeInterval_ms(newSensorNode.getDataExchangeInterval_ms());
                sn.setName(newSensorNode.getName());
                sn.setSensorsAttached(newSensorNode.getSensorsAttached());
                return true;
            }
        }
        return false;
    }

    public int getListSize() {
        return sensorsNodes.size();
    }

    public SensorNode getSensorNode(int i) {
        if(i >= 0 && i < sensorsNodes.size()) {
            return sensorsNodes.get(i);
        }
        return  null;
    }

    public List<SensorNode> getSensorsNodes() {
        return sensorsNodes;
    }

    public void setSensorsNodes(List<SensorNode> sensorsNodes) {
        this.sensorsNodes = sensorsNodes;
    }



}


