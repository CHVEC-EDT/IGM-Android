package com.bytemoe.parkingassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(Utils.getCurrentAppVersion(getApplicationContext()));

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
