package com.bytemoe.parkingassistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class Utils {

    static void showSnackbar(View view, String title) {
        Snackbar.make(view, title, Snackbar.LENGTH_LONG)
                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                .show();
    }

    static void showSnackbar(View view, final Context context, String title, final Exception e) {
        Snackbar.make(view, title, Snackbar.LENGTH_INDEFINITE)
                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                .setAction("详细信息", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialAlertDialogBuilder(context)
                                .setTitle("异常")
                                .setMessage(e.getMessage())
                                .setNegativeButton("确定", null)
                                .show();
                    }
                })
                .show();
    }

    /**
     * 获取APP当前版本号
     *
     * @param context 上下文
     * @return APP当前版本号
     */
    static String getCurrentAppVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查更新并返回安装包下载地址
     *
     * @param context 上下文
     * @return 有更新版本则返回安装包下载地址，否则返回null
     * @throws IOException balabala...
     */
    static AppVersion checkUpdate(Context context) throws IOException {
        Moshi moshi = new Moshi.Builder().build();
        String cosBase = "https://cedt-ussv-1253315888.file.myqcloud.com/igm/release/android/";

        int currentBuild = Integer.parseInt(Objects.requireNonNull(getCurrentAppVersion(context)).split("-")[1]);
        Response response = httpGet(cosBase + "latest.json");
        JsonAdapter<VersionCheckNewBean> versionCheckNewBeanJsonAdapter = moshi.adapter(VersionCheckNewBean.class);
        String responseStr = Objects.requireNonNull(response.body()).string();
        VersionCheckNewBean versionCheckNewData = versionCheckNewBeanJsonAdapter.fromJson(responseStr);
        assert versionCheckNewData != null;
        response.close();
        AppVersion appVersion = new AppVersion(versionCheckNewData.elements.get(0).versionCode, "build#" + versionCheckNewData.elements.get(0).versionName, cosBase + versionCheckNewData.elements.get(0).outputFile);
        if (isDebuggable(context)) {
            return appVersion;
        }
        int latestBuild = appVersion.getVersionCode();
        return latestBuild > currentBuild ? appVersion : null;
    }

    private static Response httpGet(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Response result = null;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        if (response.isSuccessful()) {
            result = response;
        }
        return result;
    }


    public static boolean isDebuggable(Context context) {
        boolean debuggable = false;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException ignored) {
            /* ignored */
        }
        return debuggable;
    }

    public static void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    static int byteArrayToInt(byte[] byteArray) {
        if (byteArray.length != 4) return 0;
        return (byteArray[0] & 0xFF) << 24
                | (byteArray[1] & 0xFF) << 16
                | (byteArray[2] & 0xFF) << 8
                | (byteArray[3] & 0xFF);
    }
}
