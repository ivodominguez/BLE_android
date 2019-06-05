package connInterface;


import java.util.List;

/**
 *
 * @author Diego Justi
 */

public interface ConnectionInterface {

    boolean startService();

    boolean endService();

    List<DeviceInterface> scanDevices();


}
