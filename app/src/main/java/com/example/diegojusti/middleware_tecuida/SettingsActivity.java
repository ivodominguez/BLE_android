package com.example.diegojusti.middleware_tecuida;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.List;

import entity.SensorNode;
import middlewareTECUIDA.TecuidaContainer;

public class SettingsActivity extends AppCompatActivity {

    private TecuidaContainer tecuidaContainer;
    private Button buttonApply;
    private EditText editTextScanTime;
    private EditText editTextDataExchangeTime;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
        tecuidaContainer = TecuidaContainer.getInstance(this);
        editTextDataExchangeTime = findViewById(R.id.editTextDataExchangeTime);
        editTextScanTime = findViewById(R.id.editTextScanTime);
        buttonApply = findViewById(R.id.buttonSave);
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int scanTime = Integer.parseInt(editTextScanTime.getText().toString());
                    int exchangeTime = Integer.parseInt(editTextDataExchangeTime.getText().toString());

                    tecuidaContainer.getConnectionModule().setScanTimeInterval_ms(scanTime);
                    List<SensorNode> nodes = tecuidaContainer.getSensorNodesManager().getSensorsNodes();
                    for(SensorNode node : nodes) {
                        tecuidaContainer.getSensorNodesManager().setSensorNodeDataExchangeInterval(node, exchangeTime);
                    }
                    Toast.makeText(context, getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menuSettings).setEnabled(false);
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
