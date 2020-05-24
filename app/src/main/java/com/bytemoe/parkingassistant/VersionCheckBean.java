package com.bytemoe.parkingassistant;

@SuppressWarnings("unused")
class VersionCheckBean {
    ApkData apkData;

    static class ApkData {
        int versionCode;
        String versionName;
        boolean enabled;
        String outputFile;
        String fullName;
        String baseName;
    }
}
