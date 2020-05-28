package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.core.protocol.IReaderProtocol;
import com.xuhao.didi.core.utils.SLog;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<GarageBean> garageBeanJsonAdapter = moshi.adapter(GarageBean.class);

    private final String HOST = "igm-io.cedt.bytemoe.com";
    private final int PORT = 32323;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);

        MaterialToolbar topAppbar = findViewById(R.id.topAppBar);
        topAppbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }
                return false;
            }
        });

        boolean isDebuggable = Utils.isDebuggable(getApplicationContext());

        SLog.setIsDebug(isDebuggable);
        OkSocketOptions.setIsDebug(isDebuggable);

        ConnectionInfo connectionInfo = new ConnectionInfo(HOST, PORT);
        final IConnectionManager connectionManager = OkSocket.open(connectionInfo);
        OkSocketOptions.Builder builder = new OkSocketOptions.Builder();
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    String[] filename = ((String) msg.obj).split("/");
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("软件更新中")
                            .setMessage("正在下载新的版本，请在通知栏查看下载进度")
                            .setCancelable(true)
                            .show();
                    new DownloadUtils(MainActivity.this, (String) msg.obj, filename[filename.length - 1]);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                try {
                    String apkUrl = Objects.requireNonNull(Utils.checkUpdate(getApplicationContext())).getDownloadUrl();
                    msg.obj = apkUrl;
                    if (apkUrl != null) handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        builder.setReaderProtocol(new IReaderProtocol() {
            @Override
            public int getHeaderLength() {
                return 4;
            }

            @Override
            public int getBodyLength(final byte[] header, final ByteOrder byteOrder) {
                return Utils.byteArrayToInt(header);
            }
        });

        connectionManager.option(builder.build());
        connectionManager.registerReceiver(new SocketActionAdapter() {
            @Override
            public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
                super.onSocketConnectionSuccess(info, action);
                Utils.showSnackbar(findViewById(R.id.mainLayout), getString(R.string.tip_connect_successful));
            }

            @Override
            public void onSocketDisconnection(ConnectionInfo info, String action, final Exception e) {
                super.onSocketDisconnection(info, action, e);
                Utils.showSnackbar(findViewById(R.id.mainLayout), MainActivity.this, getString(R.string.tip_connection_abort), e);
            }

            @Override
            public void onSocketConnectionFailed(ConnectionInfo info, String action, final Exception e) {
                super.onSocketConnectionFailed(info, action, e);
                Utils.showSnackbar(findViewById(R.id.mainLayout), MainActivity.this, getString(R.string.tip_connect_failed), e);
            }

            @Override
            public void onSocketReadResponse(final ConnectionInfo info, final String action, final OriginalData data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String dataStr = new String(data.getBodyBytes());
                        GarageBean garageBean = null;
                        try {
                            garageBean = garageBeanJsonAdapter.fromJson(dataStr);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert garageBean != null;
                        DataManager.getInstance().getGarageList().clear();
                        for (GarageBean.Data.Garage garage : garageBean.data.garages) {
                            DataManager.getInstance().getGarageList().add(
                                    new Garage(new String(garage.name.getBytes()), garage.distance / 100, garage.total, garage.remaining, garage.parkId)
                            );
                            ((GarageAdapter) Objects.requireNonNull(DataManager.getInstance().getStore().get("garageAdapter"))).notifyDataSetChanged();
                        }
                    }
                });
            }
        });

        connectionManager.connect();

        final GarageAdapter garageAdapter = new GarageAdapter(MainActivity.this, R.layout.garage_item, DataManager.getInstance().getGarageList());
        ListView listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, GarageDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        listView.setAdapter(garageAdapter);
        DataManager.getInstance().getStore().put("garageAdapter", garageAdapter);

        DataManager.getInstance().getStore().putIfAbsent("recvTimer", new Timer());
        Timer timer = (Timer) Objects.requireNonNull(DataManager.getInstance().getStore().get("recvTimer"));
        DataManager.getInstance().getStore().putIfAbsent("recvTimerRunning", false);
        if (!(boolean) DataManager.getInstance().getStore().get("recvTimerRunning")) {
            timer.schedule(new RecvTask(connectionManager), 0, 5000);
            DataManager.getInstance().getStore().put("recvTimerRunning", true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.getInstance().getGarageList().clear();
    }


    static class m2sPKG implements ISendable {
        private String message = "";

        m2sPKG() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cmd", "fetch");
                jsonObject.put("identifier", 0);
                jsonObject.put("quantity", 0);
                jsonObject.put("remain", 0);
                jsonObject.put("beingUsed", 0);
                jsonObject.put("info", 0);
                message = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] parse() {
            byte[] body = message.getBytes(Charset.defaultCharset());
            ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.put(body);
            return bb.array();
        }
    }

    class RecvTask extends TimerTask {
        private final IConnectionManager connectionManager;

        RecvTask(IConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (connectionManager.isConnect()) connectionManager.send(new m2sPKG());
                }
            });
        }
    }
}
