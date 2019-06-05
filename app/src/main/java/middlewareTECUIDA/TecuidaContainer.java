package middlewareTECUIDA;

import android.content.Context;

public class TecuidaContainer {

    //private static TecuidaContainer instance = null;
    private Context context;
    private StorageModule storageModule;
    private ConnectionModule connectionModule;
    private SensorNodesManager sensorNodesManager;
    private static TecuidaContainer instance = null;

    public static TecuidaContainer getInstance(Context context) {
        if(instance == null) {
            instance = new TecuidaContainer(context);
        }
        return instance;
    }

    private TecuidaContainer(Context context) {
        this.context = context;
        storageModule = StorageModule.getInstance(context);
        connectionModule = ConnectionModule.getInstance(context);
        sensorNodesManager = SensorNodesManager.getInstance();
    }


    public StorageModule getStorageModule() {
        return storageModule;
    }

    public ConnectionModule getConnectionModule() {
        return connectionModule;
    }

    public SensorNodesManager getSensorNodesManager() {
        return sensorNodesManager;
    }


}
