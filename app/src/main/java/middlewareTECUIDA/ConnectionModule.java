package middlewareTECUIDA;


import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import DAO.StorageDAO;
import connInterface.ConnectionInterface;
import connInterface.DeviceInterface;
import entity.Sensor;
import entity.SensorData;
import entity.SensorNode;
import utils.LogManager;

public class ConnectionModule extends Thread{

    public enum State {
        FINISH,
        STARTUP,
        SCAN_DEVICES,
        CONNECT_TO_DEVICES,
        SYNC_DEVICES,
        CHECK_CONNECTION,
        RECEIVE_DATA,
        SEND_DATA,
        IDLE
    }

    public static final long SCAN_TIME_INTERVAL = 10000;
    public static final long CHECK_CONNECTION_INTERVAL = 1000;
    public static final long MAX_SLEEP_TIME = 300000;
    public static final long SYNC_TRIES = 3;

    public static final String SYNC_NODE = "SYNC_NODE";
    public static final String DATA_SYNC_NAME = "NAME";

    //private static final String LOG_STRING = "STATEMACHINE";
    private static final String LOG_STRING = "GERENCIAMENTO_DOS_NOS";

    private static ConnectionModule instance = null;
    private State currentState;
    private long scanTimeInterval_ms;
    private final SensorNodesManager sensorNodesManager;
    private final List<DeviceInterface> deviceList;
    private final List<ConnectionInterface> connectionInterfaceObjects;
    private final Queue<String> broadcastData;
    private final Queue<SensorData> dataToSend;
    private long lastScanTime;
    private long maxSleepTime;
    private final Context context;

    private Thread scanDevice;
    private Thread connectDevice;

    public static ConnectionModule getInstance(Context context) {
        if(instance == null) {
            instance = new ConnectionModule(context);
        }
        return instance;
    }

    private ConnectionModule(Context context) {
        this.context = context;
        currentState = State.STARTUP;
        sensorNodesManager = SensorNodesManager.getInstance();
        connectionInterfaceObjects = new ArrayList<>();
        deviceList = new ArrayList<>();
        broadcastData = new LinkedList<>();
        dataToSend = new LinkedList<>();
        scanTimeInterval_ms = SCAN_TIME_INTERVAL;
        lastScanTime = 0;
        maxSleepTime = 500;
        scanDevice = new Thread();
        connectDevice = new Thread();
        this.start();
    }

    public boolean addConnectionInterface(ConnectionInterface conn) {
        return connectionInterfaceObjects.add(conn);
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean broadcastData(String data) {
        return broadcastData.offer(data);
    }

    public boolean sendDataToNode(SensorData data) {
        if(data == null)
            return false;
        if(data.getInfo() == null || data.getSensorId() == null || data.getSensorNodeId() == null)
            return false;
        return dataToSend.offer(data);
    }

    public long getScanTimeInterval_ms() {
        return scanTimeInterval_ms;
    }

    public void setScanTimeInterval_ms(long scanTimeInterval_ms) {
        this.scanTimeInterval_ms = scanTimeInterval_ms;
    }


    @Override
    public void run() {
        long timeNow;
        while(currentState != State.FINISH) {

            switch(currentState) {
                case STARTUP:
                    //ALSO CHECK FOR PREVIOUS SAVED DEVS, THEN NEXT STATE SHOULD BE SYNC_DEVS

                    Log.i("STATEMACHINE", "STARTUP");
                    LogManager.logFile(LOG_STRING, "ESTADO -> INICIALIZACAO");

                    if (connectionInterfaceObjects.size() > 0) {
                        currentState = State.SCAN_DEVICES;
                    } else {
                        sleepTime(500);
                    }

                    break;

                case SCAN_DEVICES:
                    Log.i("STATEMACHINE", "SCAN");
                    LogManager.logFile(LOG_STRING, "ESTADO -> ESCANEAMENTO DE DISPOSITIVOS");
                    timeNow = SystemClock.elapsedRealtime();
                    if((lastScanTime + scanTimeInterval_ms) < timeNow) {

                        lastScanTime = SystemClock.elapsedRealtime();

                        if(!scanDevice.isAlive()) {
                            Log.i("THREAD SCAN", "STARTED");
                            scanDevice = new Thread(new scanDev());
                            scanDevice.start();
                        }

                    }
                    else {
                        synchronized (deviceList) {
                            if (deviceList.size() > 0) {
                                currentState = State.CONNECT_TO_DEVICES;
                            }
                            else {
                                currentState = State.RECEIVE_DATA;
                            }
                        }
                    }
                    break;

                case CONNECT_TO_DEVICES:
                    Log.i("STATEMACHINE", "CONNECT");
                    LogManager.logFile(LOG_STRING, "ESTADO -> CONEXAO COM DISPOSTIVOS");

                    if(!connectDevice.isAlive()) {
                        Log.i("THREAD CONN", "STARTED");
                        connectDevice = new Thread(new connectDev());
                        connectDevice.start();
                    }

                    if(sensorNodesManager.getListSize() > 0) {
                        //NOTIFY METHOD TO STORAGE MODULE
                        Log.i("STATEMACHINE", "GOING TO RECEIVE DATA");
                        currentState = State.RECEIVE_DATA;
                    }
                    else {
                        Log.i("STATEMACHINE", "NO CONNECTED DEVICES!!");
                        currentState = State.SCAN_DEVICES;
                    }
                    break;

                case SYNC_DEVICES:
                    //WILL USE ??
                    for(int i = 0; i < sensorNodesManager.getListSize(); i++) {
                        SensorNode node = sensorNodesManager.getSensorNode(i);
                        if (node.getId() == null || node.getId().equals("")) {
                            if (node.getSensorsAttached().size() > 0) {

                            }
                        }
                    }
                    currentState = State.RECEIVE_DATA;
                    break;

                case RECEIVE_DATA:
                    Log.i("STATEMACHINE", "RECEIVE");
                    LogManager.logFile(LOG_STRING, "ESTADO -> RECEBIMENTO DE DADOS");

                    for(int i = 0; i < sensorNodesManager.getListSize(); i++) {
                        timeNow = SystemClock.elapsedRealtime();
                        SensorNode node = sensorNodesManager.getSensorNode(i);
                        if((node.getLastExchangeTime_ms() + node.getDataExchangeInterval_ms()) < timeNow) {
                            Log.i("STATEMACHINE", "RECEIVING DATA...");
                            if(node.getConnInterface().checkConnection() == DeviceInterface.connectionStatus.STATUS_CONNECTED) {
                                //Date dateNow = new Date();
                                List<SensorData> sensorData = node.getConnInterface().getData();
                                for(int j = 0; j < sensorData.size(); j++) {
                                    //sensorData.get(j).setSensorNodeId(node.getId());
                                    String info = sensorData.get(j).getInfo();
                                    Log.i("STATEMACHINE", "DATA GOT = " + info);
                                    LogManager.logFile(LOG_STRING, "DADO RECEBIDO DE -> " + node.getId() + "=" + sensorData.get(j).getInfo());
                                    StorageDAO.getInstance(context).insertTable(sensorData.get(j));
                                }
                                node.setLastExchangeTime_ms(timeNow);
                            }
                            else {
                                sensorNodesManager.removeSensorNode(i);
                            }
                        }
                    }
                    currentState = State.SEND_DATA;
                    break;

                case SEND_DATA:
                    Log.i("STATEMACHINE", "SEND");
                    if(broadcastData.size() > 0) {
                        for(int i = 0; i < sensorNodesManager.getListSize(); i++) {
                            SensorNode node = sensorNodesManager.getSensorNode(i);
                            Log.i("STATEMACHINE", "SENDING DATA BROADCAST...");
                            if(node.getConnInterface().checkConnection() == DeviceInterface.connectionStatus.STATUS_CONNECTED) {
                                if(node.getConnInterface().sendData(broadcastData.poll())) {
                                    broadcastData.peek();
                                }
                            }
                            else {
                                sensorNodesManager.removeSensorNode(i);
                                dataToSend.peek();
                            }
                        }
                    }
                    if(dataToSend.size() > 0) {
                        boolean nodeFound = false;
                        for(int i = 0; i < sensorNodesManager.getListSize(); i++) {
                            SensorNode node = sensorNodesManager.getSensorNode(i);
                            SensorData data = dataToSend.poll();
                            if(node.getId().equals(data.getSensorNodeId())) {
                                nodeFound = true;
                                Log.i("STATEMACHINE", "SENDIND DATA TO NODE...");
                                if(node.getConnInterface().checkConnection() == DeviceInterface.connectionStatus.STATUS_CONNECTED) {
                                    if (node.getConnInterface().sendData(data.getInfo())) {
                                        dataToSend.peek();
                                    }
                                }
                                else {
                                    sensorNodesManager.removeSensorNode(i);
                                    dataToSend.peek();
                                }
                            }
                        }
                        if(!nodeFound) {
                            dataToSend.peek();
                        }
                    }
                    currentState = State.IDLE;

                    break;
                case IDLE:

                    Log.i("STATEMACHINE", "IDLE");
                    LogManager.logFile(LOG_STRING, "ESTADO -> OCIOSO = SUSPENDER POR " + maxSleepTime + "ms");
                    if(maxSleepTime > MAX_SLEEP_TIME)
                        sleepTime(MAX_SLEEP_TIME);
                    else
                        sleepTime(maxSleepTime);

                    currentState = State.SCAN_DEVICES;

                    break;
            }
        }

        for(int i = 0; i < connectionInterfaceObjects.size(); i++) {
            connectionInterfaceObjects.get(i).endService();
        }

    }


    private void sleepTime(final long millis) {
        try {
            sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class scanDev implements Runnable {

        @Override
        public void run() {

            List<DeviceInterface> tempList = null;
            for (int i = 0; i < connectionInterfaceObjects.size(); i++) {
                if(connectionInterfaceObjects.get(i).startService()) {
                    Log.i("STATEMACHINE", "SCANNING....");
                    tempList = connectionInterfaceObjects.get(i).scanDevices();
                    Log.i("STATEMACHINE",  "THREAD LIST SIZE="+tempList.size());
                }
            }

            if(tempList != null) {
                synchronized (deviceList) {
                    deviceList.addAll(tempList);
                }
            }
        }
    }

    class connectDev implements Runnable {

        @Override
        public void run() {
            synchronized (deviceList) {
                long timeNow = SystemClock.elapsedRealtime();
                for(int i = 0; i < deviceList.size(); i++) {
                    SensorNode node = deviceList.get(i).connectToDevice();
                    if(node != null) {
                        Log.i("STATEMACHINE", "SENSOR NODE ADDED: " + node.getId());

                        node.setConnInterface(deviceList.get(i));
                        node.setLastExchangeTime_ms(timeNow);
                        if(maxSleepTime > node.getDataExchangeInterval_ms()) {
                            maxSleepTime = node.getDataExchangeInterval_ms();
                        }
                        sensorNodesManager.addSensorNode(node);

                    }

                }
                deviceList.clear();
            }
        }
    }

}
