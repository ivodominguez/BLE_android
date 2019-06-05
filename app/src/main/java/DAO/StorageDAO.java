package DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import entity.SensorData;
import entity.SensorNode;
import entity.Sensor;

public class StorageDAO extends SQLiteOpenHelper {

    private static final String DB_NAME = "AppTECUIDA.db";
    private static final int DB_VERSION = 1;

    private static final String DB_TABLE_SENSOR = "SENSORS";
    private static final String DB_TABLE_NODES = "NODES";
    private static final String DB_TABLE_SENSORDATA = "SENSORDATA";

    private static final String DB_TABLE_SENSOR_ATT_ID = "sensorId";
    private static final String DB_TABLE_SENSOR_ATT_FK_NODEID = "nodeId";
    private static final String DB_TABLE_SENSOR_ATT_DESC = "description";

    private static final String DB_TABLE_NODES_ATT_ID = "nodeId";
    private static final String DB_TABLE_NODES_ATT_DESC = "description";
    private static final String DB_TABLE_NODES_ATT_EXCHANGE_INTERVAL = "exchangeInterval";

    private static final String DB_TABLE_SENSORDATA_ATT_FK_SENSORID = "sensorId";
    private static final String DB_TABLE_SENSORDATA_ATT_FK_NODEID = "nodeId";
    private static final String DB_TABLE_SENSORDATA_ATT_ID = "dataId";
    private static final String DB_TABLE_SENSORDATA_ATT_DATE = "date";
    private static final String DB_TABLE_SENSORDATA_ATT_INFO = "info";


    public static final int SORT_BY_DATE = 10;
    public static final int SORT_BY_SENSORNODE = 20;


    private static StorageDAO instance = null;
    private Context context;

    private StorageDAO(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    public static StorageDAO getInstance(Context context) {
        if(instance == null) {
            instance = new StorageDAO(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        dropTables(sqLiteDatabase);
        onCreate(sqLiteDatabase);
    }

    private void createTable(SQLiteDatabase database) {

        /*

        String sqlNode = "CREATE TABLE " + DB_TABLE_NODES + "(" +
                            DB_TABLE_NODES_ATT_ID + " TEXT PRIMARY KEY," +
                            DB_TABLE_NODES_ATT_DESC + " TEXT," +
                            DB_TABLE_NODES_ATT_EXCHANGE_INTERVAL + " INTEGER)";

        database.execSQL(sqlNode);

        String sqlSensor = "CREATE TABLE " + DB_TABLE_SENSOR + "(" +
                            DB_TABLE_SENSOR_ATT_ID + "TEXT PRIMARY KEY," +
                            DB_TABLE_SENSOR_ATT_DESC + "TEXT," +
                            DB_TABLE_SENSOR_ATT_FK_NODEID + "TEXT PRIMARY KEY," +
                            "FOREIGN KEY (" + DB_TABLE_SENSOR_ATT_FK_NODEID + ") " +
                            "REFERENCES " + DB_TABLE_NODES + "(" + DB_TABLE_NODES_ATT_ID + ") " +
                            "ON DELETE CASCADE";

        database.execSQL(sqlSensor);

        String sqlData = "CREATE TABLE " + DB_TABLE_SENSORDATA + "(" +
                            DB_TABLE_SENSORDATA_ATT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            DB_TABLE_SENSORDATA_ATT_DATE + " DATE, " +
                            DB_TABLE_SENSORDATA_ATT_INFO + " TEXT," +
                            DB_TABLE_SENSORDATA_ATT_FK_NODEID + " TEXT," +
                            DB_TABLE_SENSORDATA_ATT_FK_SENSORID + " TEXT," +
                            "FOREIGN KEY (" + DB_TABLE_SENSORDATA_ATT_FK_NODEID + ") " +
                            "REFERENCES " + DB_TABLE_NODES + "(" + DB_TABLE_NODES_ATT_ID + ")," +
                            "FOREIGN KEY (" + DB_TABLE_SENSORDATA_ATT_FK_SENSORID + ")" +
                            "REFERENCES " + DB_TABLE_SENSOR + "(" + DB_TABLE_SENSOR_ATT_ID + ")";

        database.execSQL(sqlData);

        */

        String sqlData = "CREATE TABLE " + DB_TABLE_SENSORDATA + "(" +
                DB_TABLE_SENSORDATA_ATT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DB_TABLE_SENSORDATA_ATT_DATE + " INTEGER, " +
                DB_TABLE_SENSORDATA_ATT_INFO + " TEXT," +
                DB_TABLE_SENSORDATA_ATT_FK_NODEID + " TEXT," +
                DB_TABLE_SENSORDATA_ATT_FK_SENSORID + " TEXT)";

        database.execSQL(sqlData);

    }

    public void dropTables(SQLiteDatabase database) {

        database.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_SENSORDATA);
        database.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_SENSOR);
        database.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NODES);

    }

    public void insertTable (SensorNode node) {
        ContentValues values = new ContentValues();
        values.put(DB_TABLE_NODES_ATT_DESC, node.getName());
        values.put(DB_TABLE_NODES_ATT_ID, node.getId());
        values.put(DB_TABLE_NODES_ATT_EXCHANGE_INTERVAL, node.getDataExchangeInterval_ms());

        getWritableDatabase().insert(DB_TABLE_NODES, null, values);

        for(int i = 0; i < node.getSensorsAttached().size(); i++) {
            values = new ContentValues();
            values.put(DB_TABLE_SENSOR_ATT_DESC, node.getSensorsAttached().get(i).getName());
            values.put(DB_TABLE_SENSOR_ATT_ID, node.getSensorsAttached().get(i).getId());
            values.put(DB_TABLE_SENSOR_ATT_FK_NODEID, node.getId());

            getWritableDatabase().insert(DB_TABLE_SENSOR, null, values);
        }

    }

    public long insertTable (SensorData data) {
        ContentValues values = new ContentValues();
        values.put(DB_TABLE_SENSORDATA_ATT_DATE, data.getDate().getTime());
        values.put(DB_TABLE_SENSORDATA_ATT_INFO, data.getInfo());
        values.put(DB_TABLE_SENSORDATA_ATT_FK_NODEID, data.getSensorNodeId());
        values.put(DB_TABLE_SENSORDATA_ATT_FK_SENSORID, data.getSensorId());

        long dataId = getWritableDatabase().insert(DB_TABLE_SENSORDATA, null, values);

        return dataId;
    }

    public List<SensorData> selectFromTable_sensorData (Date startDate, Date endDate, SensorNode node) {

        String sql;

        if((startDate == null) && (endDate == null) && (node == null)) {
            sql = "SELECT * FROM " + DB_TABLE_SENSORDATA + " ORDER BY " + DB_TABLE_SENSORDATA_ATT_DATE;
        }
        else if(node == null) {
            sql = "SELECT * FROM " + DB_TABLE_SENSORDATA + " WHERE " + DB_TABLE_SENSORDATA_ATT_DATE + " BETWEEN "
                     + startDate.getTime() + " AND " + endDate.getTime() + " ORDER BY " + DB_TABLE_SENSORDATA_ATT_DATE;
        }
        else {
            sql = "SELECT * FROM " + DB_TABLE_SENSORDATA + " WHERE " + DB_TABLE_SENSORDATA_ATT_DATE + " BETWEEN "
                    + startDate.getTime() + " AND " + endDate.getTime() + " AND " + DB_TABLE_SENSORDATA_ATT_FK_NODEID + " = " + node.getId()
                    + " ORDER BY " + DB_TABLE_SENSORDATA_ATT_DATE;
        }

        Cursor cursor = getWritableDatabase().rawQuery(sql, null);

        int colSensorId = cursor.getColumnIndex(DB_TABLE_SENSORDATA_ATT_FK_SENSORID);
        int colNodeId = cursor.getColumnIndex(DB_TABLE_SENSORDATA_ATT_FK_NODEID);
        int colDate = cursor.getColumnIndex(DB_TABLE_SENSORDATA_ATT_DATE);
        int colInfo = cursor.getColumnIndex(DB_TABLE_SENSORDATA_ATT_INFO);

        List<SensorData> dataList = new ArrayList<>();

        while(cursor.moveToNext()) {
            SensorData data = new SensorData();
            data.setSensorNodeId(cursor.getString(colNodeId));
            data.setSensorId(cursor.getString(colSensorId));
            data.setDate(new Date(cursor.getLong(colDate)));
            data.setInfo(cursor.getString(colInfo));

            dataList.add(data);
        }

        cursor.close();

        return dataList;

    }

    public void deleteFromTable_sensorData (Date startDate, Date endDate, SensorNode node) {
        String sql;
        if(node == null) {
            sql = "DELETE FROM " + DB_TABLE_SENSORDATA + " WHERE " + DB_TABLE_SENSORDATA_ATT_DATE + " BETWEEN "
                    + startDate.getTime() + " AND " + endDate.getTime();
        }
        else {
            sql = "DELETE FROM " + DB_TABLE_SENSORDATA + " WHERE " + DB_TABLE_SENSORDATA_ATT_DATE + " BETWEEN "
                    + startDate.getTime() + " AND " + endDate.getTime() + " AND " + DB_TABLE_SENSORDATA_ATT_FK_NODEID + "=" + node.getId();
        }
        getWritableDatabase().execSQL(sql);
    }



}
