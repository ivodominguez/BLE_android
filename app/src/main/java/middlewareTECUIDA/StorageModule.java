package middlewareTECUIDA;

import android.content.Context;

import java.util.Date;
import java.util.List;

import DAO.StorageDAO;
import entity.SensorData;
import entity.SensorNode;

public class StorageModule {

    private Context context;
    private final StorageDAO storageInstance;
    private static StorageModule instance = null;

    public static StorageModule getInstance(Context context) {
        if(instance == null) {
            instance = new StorageModule(context);
        }
        return instance;
    }

    private StorageModule(Context context) {
        this.context = context;
        storageInstance = StorageDAO.getInstance(context);
    }

    public long saveData(SensorData data) {
        synchronized (storageInstance) {
            return storageInstance.insertTable(data);
        }
    }

    public void deleteData(Date startDate, Date endDate, SensorNode node) {
        synchronized (storageInstance) {
            storageInstance.deleteFromTable_sensorData(startDate, endDate, node);
        }
    }

    public List<SensorData> getData(Date startDate, Date endDate, SensorNode node) {
        synchronized (storageInstance) {
            return storageInstance.selectFromTable_sensorData(startDate, endDate, node);
        }
    }

}
