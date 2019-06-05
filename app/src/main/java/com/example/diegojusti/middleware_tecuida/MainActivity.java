package com.example.diegojusti.middleware_tecuida;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import bluetoothConnection.BluetoothConnInterface;
import entity.Sensor;
import entity.SensorData;
import entity.SensorNode;
import middlewareTECUIDA.ConnectionModule;
import middlewareTECUIDA.TecuidaContainer;
import utils.DateFormatter;


public class MainActivity extends AppCompatActivity {


    private Context context;
    private TecuidaContainer tecuidaContainer;
    private ListView listViewData;
    private EditText editTextFirstData;
    private EditText editTextLastData;
    private ArrayAdapter<String> dataList;

    private final int REQ_BLUETOOTH = 0;
    private final int REQ_LOCATION = 1;
    private final int REQ_STORAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, REQ_BLUETOOTH);
        }

        context = this;
        tecuidaContainer = TecuidaContainer.getInstance(this);
        BluetoothConnInterface bt = BluetoothConnInterface.getInstance(context);
        tecuidaContainer.getConnectionModule().addConnectionInterface(bt);

        listViewData = findViewById(R.id.listViewData);
        editTextFirstData = findViewById(R.id.editTextFirstDate);
        editTextLastData = findViewById(R.id.editTextLastDate);
        dataList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewData.setAdapter(dataList);
        setDatabase();


        // startService(new Intent(this, ServiceThread.class));

        editTextFirstData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i("onTextChanged: ", "seq="+charSequence+" i="+i+" i1="+i1+" i2="+i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String firstDate = editTextFirstData.getText().toString();
                String lastDate = editTextLastData.getText().toString();
                if((firstDate.length() == 10) && (DateFormatter.parseStringDate(firstDate) != null)) {
                    List<SensorData> ret = null;
                    if(lastDate.isEmpty()){
                        ret = tecuidaContainer.getStorageModule().getData(DateFormatter.parseStringDate(firstDate), new Date(), null);
                    }
                    else if (DateFormatter.parseStringDate(lastDate) != null){
                        ret = tecuidaContainer.getStorageModule().getData(DateFormatter.parseStringDate(firstDate), DateFormatter.parseStringDate(lastDate), null);
                    }

                    if(ret != null) {
                        fillListViewData(ret);
                    }
                }
            }
        });

        editTextLastData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String firstDate = editTextFirstData.getText().toString();
                String lastDate = editTextLastData.getText().toString();
                if((lastDate.length() == 10) && (DateFormatter.parseStringPatternDate(lastDate) != null) && (DateFormatter.parseStringPatternDate(lastDate) != null)) {
                    List<SensorData> ret = null;
                    ret = tecuidaContainer.getStorageModule().getData(DateFormatter.parseStringPatternDate(firstDate), DateFormatter.parseStringPatternDate(lastDate), null);
                    if(ret != null) {
                        fillListViewData(ret);
                    }
                }
            }
        });

        /*
        PowerManager.WakeLock mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire(3600000);

         container = new TecuidaContainer(context); BluetoothConnInterface bt = BluetoothConnInterface.getInstance(context); container.getConnectionModule().addConnectionInterface(bt);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PowerManager.WakeLock mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
                mWakeLock.acquire(3600000);

                while(container.getConnectionModule().isAlive()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mWakeLock.release();
            }
        });*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menuSearch).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuNodes:
                startActivity(new Intent(this, NodesActivity.class));
                break;
            case R.id.menuRealTime:
                startActivity(new Intent(this, DataActivity.class));
                break;
            case R.id.menuSearch:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.menuSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return true;
    }

    private void fillListViewData (List<SensorData> dataList) {

        int max;
        if(dataList.size() > 100) {
            max = 100;
            Toast.makeText(this, getString(R.string.toast_maxList), Toast.LENGTH_SHORT).show();
        }
        else
            max = dataList.size();

        this.dataList.clear();
        for(int i = 0; i < max; i++) {
            SensorData data = dataList.get(i);
            String label =  "\n" + getString(R.string.data_info) + ": " + data.getInfo() +
                            "\n" + getString(R.string.data_date) + ": " + DateFormatter.getFormattedPatternDate(data.getDate()) +
                            "\n" + getString(R.string.data_id) + ": " + data.getSensorNodeId() +
                            "\n";
            this.dataList.add(label);
        }
        this.dataList.notifyDataSetChanged();
    }

    private void setDatabase () {

        tecuidaContainer.getStorageModule().deleteData(DateFormatter.parseStringDate("2018-01-01"), DateFormatter.parseStringDate("2018-12-12"), null);

        List<Sensor> sensors = new ArrayList<>();
        sensors.add(new Sensor());

        SensorNode node1 = new SensorNode();
        node1.setId("5C:F8:21:F9:69:C2");
        node1.setSensorsAttached(sensors);

        SensorNode node2 = new SensorNode();
        node2.setId("5C:F8:21:88:09:9A");
        node2.setSensorsAttached(sensors);

        SensorNode node3 = new SensorNode();
        node3.setId("5C:F8:21:B3:34:1F");
        node3.setSensorsAttached(sensors);

        for(int i = 0; i < 10; i++) {
            SensorData data = new SensorData();
            data.setSensorNodeId(node1.getId());
            data.setSensorId("1");
            int mili = ThreadLocalRandom.current().nextInt(300, 800);
            String dateTime = "2018-06-10 " + "19:" + fillOneZeroLeft(""+i) + ":27:" + fillDoubleZeroLeft(""+mili);
            String[] tempRange = {"35.5", "36.0", "36.5", "37.0", "37.5"};
            int pos = ThreadLocalRandom.current().nextInt(0, 5);
            data.setDate(DateFormatter.parseStringDateTime(dateTime));
            data.setInfo(tempRange[pos] + " C");
            tecuidaContainer.getStorageModule().saveData(data);
        }

        for(int i = 0; i < 10; i++) {
            SensorData data = new SensorData();
            data.setSensorNodeId(node2.getId());
            data.setSensorId("1");
            int mili = ThreadLocalRandom.current().nextInt(200, 900);
            String dateTime = "2018-06-10 " + "19:" + fillOneZeroLeft(""+i) + ":27:" + fillDoubleZeroLeft(""+mili);
            int bpm = ThreadLocalRandom.current().nextInt(70, 90);
            data.setDate(DateFormatter.parseStringDateTime(dateTime));
            data.setInfo(""+bpm + " BPM");
            tecuidaContainer.getStorageModule().saveData(data);

            SensorData data2 = new SensorData();
            data2.setSensorNodeId(node2.getId());
            data2.setSensorId("1");
            int mili2 = ThreadLocalRandom.current().nextInt(200, 900);
            String dateTime2 = "2018-06-10 " + "19:" + fillOneZeroLeft(""+i) + ":57:" + fillDoubleZeroLeft(""+mili2);
            int bpm2 = ThreadLocalRandom.current().nextInt(70, 90);
            data2.setDate(DateFormatter.parseStringDateTime(dateTime2));
            data2.setInfo(""+bpm2 + " BPM");
            tecuidaContainer.getStorageModule().saveData(data2);
        }


        // -----------------

        for(int i = 0; i < 10; i++) {
            SensorData data = new SensorData();
            data.setSensorNodeId(node1.getId());
            data.setSensorId("1");
            int mili = ThreadLocalRandom.current().nextInt(300, 800);
            String dateTime = "2018-06-13 " + "21:" + fillOneZeroLeft(""+i) + ":49:" + fillDoubleZeroLeft(""+mili);
            String[] tempRange = {"35.5", "36.0", "36.5", "37.0", "37.5"};
            int pos = ThreadLocalRandom.current().nextInt(0, 5);
            data.setDate(DateFormatter.parseStringDateTime(dateTime));
            data.setInfo(tempRange[pos] + " C");
            tecuidaContainer.getStorageModule().saveData(data);
        }

        for(int i = 0; i < 10; i++) {
            SensorData data = new SensorData();
            data.setSensorNodeId(node2.getId());
            data.setSensorId("1");
            int mili = ThreadLocalRandom.current().nextInt(200, 900);
            String dateTime = "2018-06-13 " + "21:" + fillOneZeroLeft(""+i) + ":49:" + fillDoubleZeroLeft(""+mili);
            int bpm = ThreadLocalRandom.current().nextInt(70, 90);
            data.setDate(DateFormatter.parseStringDateTime(dateTime));
            data.setInfo(""+bpm+" BPM");
            tecuidaContainer.getStorageModule().saveData(data);

            SensorData data2 = new SensorData();
            data2.setSensorNodeId(node2.getId());
            data2.setSensorId("1");
            int mili2 = ThreadLocalRandom.current().nextInt(100, 900);
            String dateTime2 = "2018-06-13 " + "21:" + fillOneZeroLeft(""+i) + ":19:" + fillDoubleZeroLeft(""+mili2);
            int bpm2 = ThreadLocalRandom.current().nextInt(70, 90);
            data2.setDate(DateFormatter.parseStringDateTime(dateTime2));
            data2.setInfo(""+bpm2 + " BPM");
            tecuidaContainer.getStorageModule().saveData(data2);
        }

        List<SensorData> dataList = tecuidaContainer.getStorageModule().getData(DateFormatter.parseStringDate("2018-06-01"), DateFormatter.parseStringDate("2018-06-16"), null);

        fillListViewData(dataList);
    }

    private String fillOneZeroLeft(String str) {
        if(str.length() == 2)
            return str;
        else
            return "0"+str;
    }

    private String fillDoubleZeroLeft(String str) {
        if(str.length() == 3)
            return str;
        else if(str.length() == 2)
            return "0"+str;
        else
            return "00"+str;
    }

    private String numberPrecision (String str, int precision) {
        int dot = str.indexOf(".");
        return str.substring(0, dot) + "." + str.substring(dot+1, dot+1+precision);
    }

}


