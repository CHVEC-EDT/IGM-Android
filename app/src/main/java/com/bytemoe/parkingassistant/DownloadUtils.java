package com.bytemoe.parkingassistant;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

class DownloadUtils {
    private DownloadManager downloadManager;
    private final Context mContext;
    private long downloadId;
    private final String name;
    private String pathstr;

    DownloadUtils(Context context, String url, String name) {
        this.mContext = context;
        downloadAPK(url, name);
        this.name = name;
    }

    private void downloadAPK(String url, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("停车助手");
        request.setDescription("新版停车助手下载中...");
        request.setVisibleInDownloadsUi(true);

        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));
        pathstr = file.getAbsolutePath();
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }

        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    installAPK();
                    cursor.close();
                    break;
                case DownloadManager.STATUS_FAILED:
                    Toast.makeText(mContext, "下载更新失败", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    mContext.unregisterReceiver(receiver);
                    break;
            }
        }
    }

    private void installAPK() {
        setPermission(pathstr);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            File file = (new File(pathstr));
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.bytemoe.parkingassistant.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(Environment.DIRECTORY_DOWNLOADS, name)), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    private void setPermission(String absolutePath) {
        String command = "chmod " + "777" + " " + absolutePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}