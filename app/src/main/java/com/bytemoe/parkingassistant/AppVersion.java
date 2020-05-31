package com.bytemoe.parkingassistant;

class AppVersion {
    private final int versionCode;
    private final String versionName;
    private final String downloadUrl;

    AppVersion(int versionCode, String versionName, String downloadUrl) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.downloadUrl = downloadUrl;
    }

    int getVersionCode() {
        return versionCode;
    }

    String getVersionName() {
        return versionName;
    }

    String getDownloadUrl() {
        return downloadUrl;
    }
}
