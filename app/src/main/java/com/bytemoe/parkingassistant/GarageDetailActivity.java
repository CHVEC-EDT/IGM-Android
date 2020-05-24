package com.bytemoe.parkingassistant;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GarageDetailActivity extends AppCompatActivity {

    private int position;
    private ArrayAdapter<String> arrayAdapter;
    private final List<String> remainingList = new ArrayList<>();

    private TextView tv_remaining, tv_total;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_detail);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        position = bundle.getInt("position");

        initView();
        updateData();
    }

    private void initView() {
        tv_remaining = findViewById(R.id.detail_tv_remaining);
        tv_total = findViewById(R.id.detail_tv_total);
        toolbar = findViewById(R.id.topAppBar);
        ListView listView = findViewById(R.id.detail_listView);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,
                remainingList);
        listView.setAdapter(arrayAdapter);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, 1000);
    }

    private void updateData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(DataManager.getInstance().getGarageList().get(position).getName());
                tv_remaining.setText(String.valueOf(DataManager.getInstance().getGarageList().get(position).getRemaining()));
                tv_total.setText(String.valueOf(DataManager.getInstance().getGarageList().get(position).getTotal()));
                remainingList.clear();
                remainingList.addAll(Arrays.asList(DataManager.getInstance().getGarageList().get(position).getRemainingList()));
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }
}
