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

import java.util.List;

import entity.SensorNode;
import middlewareTECUIDA.SensorNodesManager;
import middlewareTECUIDA.TecuidaContainer;

public class NodesActivity extends AppCompatActivity {

    private ArrayAdapter<String> nodes;
    private ListView listViewNodes;
    private TecuidaContainer tecuidaContainer;
    private Button buttonRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodes);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        tecuidaContainer = TecuidaContainer.getInstance(this);
        nodes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewNodes = findViewById(R.id.listViewNodes);
        listViewNodes.setAdapter(nodes);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillNodes();
            }
        });

        fillNodes();
    }

    private void fillNodes() {
        List<SensorNode> sensorNodeList = SensorNodesManager.getInstance().getSensorsNodes();
        nodes.clear();
        for(SensorNode node : sensorNodeList) {
            String label = "\n" + getString(R.string.data_id) + ": " + node.getId() +
                    "\n";
            nodes.add(label);
        }
        nodes.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menuNodes).setEnabled(false);
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
