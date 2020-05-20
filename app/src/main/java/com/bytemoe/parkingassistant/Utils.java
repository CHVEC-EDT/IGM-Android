package com.bytemoe.parkingassistant;

import android.content.Context;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

class Utils {

    static void showSnackbar(View view, final Context context, String title) {
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

    static int byteArrayToInt(byte[] byteArray) {
        if (byteArray.length != 4) return 0;
        return (byteArray[0] & 0xFF) << 24
                | (byteArray[1] & 0xFF) << 16
                | (byteArray[2] & 0xFF) << 8
                | (byteArray[3] & 0xFF);
    }
}
