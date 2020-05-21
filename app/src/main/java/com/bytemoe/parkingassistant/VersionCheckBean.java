package com.bytemoe.parkingassistant;

public class VersionCheckBean {
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
