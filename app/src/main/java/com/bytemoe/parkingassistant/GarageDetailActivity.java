package com.bytemoe.parkingassistant;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GarageDetailActivity extends AppCompatActivity {

    private TextView tv_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_detail);
        Bundle bundle = getIntent().getExtras();
        tv_name = findViewById(R.id.detail_tv_name);
        assert bundle != null;
        tv_name.setText(bundle.getString("name"));
        GarageAdapter garageAdapter = (GarageAdapter) DataManager.getInstance().getStore().get("garageAdapter");
        DataManager.getInstance().getGarageList().add(new Garage("来自" + bundle.getString("name"), 0, 10, 1));
        assert garageAdapter != null;
        garageAdapter.notifyDataSetChanged();
    }
}
