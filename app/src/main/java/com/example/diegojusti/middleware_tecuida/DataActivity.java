package com.example.diegojusti.middleware_tecuida;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import entity.SensorData;
import middlewareTECUIDA.TecuidaContainer;
import utils.DateFormatter;

public class DataActivity extends AppCompatActivity {

    private ArrayAdapter<String> currentData;
    private ListView dataList;
    private TecuidaContainer tecuidaContainer;
    private Button buttonRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        dataList = findViewById(R.id.listViewData);
        tecuidaContainer = TecuidaContainer.getInstance(this);
        currentData = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dataList.setAdapter(currentData);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillData();
            }
        });

        fillData();
    }

    private void fillData() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MILLISECOND, -10000);
        List<SensorData> sensorDataList = tecuidaContainer.getStorageModule().getData(cal.getTime(), new Date(), null);
        currentData.clear();
        for(int i = sensorDataList.size()-1; i >= 0 ; i-- ){
            SensorData data = sensorDataList.get(i);
            String label =  "\n" + getString(R.string.data_info) + ": " + data.getInfo() +
                    //"\n" + getString(R.string.data_date) + ": " + DateFormatter.getFormattedDate(data.getDate()) +
                    "\n" + getString(R.string.data_id) + ": " + data.getSensorNodeId() +
                    "\n";
            currentData.add(label);
        }
        currentData.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menuRealTime).setEnabled(false);
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
}
