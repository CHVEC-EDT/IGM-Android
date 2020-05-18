package com.bytemoe.parkingassistant;

class Utils {

    static int byteArrayToInt(byte[] byteArray) {
        if (byteArray.length != 4) return 0;
        return (byteArray[0] & 0xFF) << 24
                | (byteArray[1] & 0xFF) << 16
                | (byteArray[2] & 0xFF) << 8
                | (byteArray[3] & 0xFF);
    }
}
