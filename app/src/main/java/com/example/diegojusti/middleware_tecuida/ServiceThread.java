package com.example.diegojusti.middleware_tecuida;


import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import bluetoothConnection.BluetoothConnInterface;
import middlewareTECUIDA.TecuidaContainer;

public class ServiceThread extends Service {


/*
    public ServiceThread() {


    }*/

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //new Running(this).start();
        TecuidaContainer tecuidaContainer;
        tecuidaContainer = TecuidaContainer.getInstance(this);
        BluetoothConnInterface bt = BluetoothConnInterface.getInstance(this);
        tecuidaContainer.getConnectionModule().addConnectionInterface(bt);
        return START_STICKY;

    }

    class Running extends Thread {

        private Context context;

        public Running (Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            TecuidaContainer tecuidaContainer;
            tecuidaContainer = TecuidaContainer.getInstance(context);
            BluetoothConnInterface bt = BluetoothConnInterface.getInstance(context);
            tecuidaContainer.getConnectionModule().addConnectionInterface(bt);
            Log.i("SERVICE_TASK", "MIDDLEWARE STARTED");

            while (true) {
                while (tecuidaContainer.getConnectionModule().isAlive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tecuidaContainer = TecuidaContainer.getInstance(context);
                bt = BluetoothConnInterface.getInstance(context);
                tecuidaContainer.getConnectionModule().addConnectionInterface(bt);
                Log.i("SERVICE_TASK", "MIDDLEWARE RESTARTED");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
