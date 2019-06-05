package connInterface;


import java.util.List;

import entity.SensorData;
import entity.SensorNode;

/**
 *
 * @author Diego Justi
 */

public interface DeviceInterface {

    enum connectionStatus {
        STATUS_CONNECTING,
        STATUS_CONNECTED,
        STATUS_DISCONNECTED
    }

    List<SensorData> getData();

    boolean sendData(String data);

    SensorNode connectToDevice();

    void disconnectDevice();

    connectionStatus checkConnection();

}
