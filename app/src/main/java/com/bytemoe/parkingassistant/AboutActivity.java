package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final Button btn_update_now = findViewById(R.id.btn_update_now);
        btn_update_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.restartApplication(getApplicationContext());
            }
        });

        TextView tv_version = findViewById(R.id.tv_version);
        final TextView tv_latest = findViewById(R.id.tv_latest);
        tv_version.setText(Utils.getCurrentAppVersion(getApplicationContext()));

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    tv_latest.setVisibility(View.VISIBLE);
                    tv_latest.setText(getString(R.string.about_tip_new_version, msg.obj));
                    btn_update_now.setVisibility(View.VISIBLE);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                try {
                    String latest_version = Objects.requireNonNull(Utils.checkUpdate(getApplicationContext())).getVersionName();
                    msg.obj = latest_version;
                    if (latest_version != null) handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        findViewById(R.id.btn_docs).setOnClickListener(this);
        findViewById(R.id.btn_github).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_docs:
                openUrl("https://chvec-edt.bytemoe.com/IGM-Docs/");
                break;
            case R.id.btn_github:
                openUrl("https://github.com/CHVEC-EDT/IGM-Android");
                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    private void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
