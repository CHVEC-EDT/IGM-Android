package com.bytemoe.parkingassistant;

import java.util.List;

@SuppressWarnings("unused")
class VersionCheckNewBean {
    int version;
    String applicationId;
    String variantName;
    List<Element> elements;

    static class Element {
        String type;
        int versionCode;
        String versionName;
        boolean enabled;
        String outputFile;
    }
}
