package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.core.protocol.IReaderProtocol;
import com.xuhao.didi.core.utils.SLog;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    private Handler handler;

    public String HOST = "192.168.0.106";
    //    public String HOST = "10.1.1.4";
    public int PORT = 8080;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SLog.setIsDebug(true);
        OkSocketOptions.setIsDebug(true);

        ConnectionInfo connectionInfo = new ConnectionInfo(HOST, PORT);
        final IConnectionManager connectionManager = OkSocket.open(connectionInfo);
        OkSocketOptions.Builder builder = new OkSocketOptions.Builder();

        handler = new Handler();
        builder.setReaderProtocol(new IReaderProtocol() {
            @Override
            public int getHeaderLength() {
                return 4;
            }

            @Override
            public int getBodyLength(final byte[] header, final ByteOrder byteOrder) {
                final int length = byteArrayToInt(header);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, String.valueOf(length), Toast.LENGTH_SHORT).show();
                    }
                });
                return length;
            }
        });
        connectionManager.option(builder.build());
        connectionManager.registerReceiver(new SocketActionAdapter() {
            @Override
            public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(MainActivity.this, "Connected to server.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSocketReadResponse(final ConnectionInfo info, final String action, final OriginalData data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        OkSocket.open(info).send(new ISendable() {
                            @Override
                            public byte[] parse() {
                                return data.getBodyBytes();
                            }
                        });
                    }
                });
            }


        });
        connectionManager.connect();
    }

    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray.length != 4) return 0;
        return (byteArray[0] & 0xFF) << 24
                | (byteArray[1] & 0xFF) << 16
                | (byteArray[2] & 0xFF) << 8
                | (byteArray[3] & 0xFF);
    }
}
