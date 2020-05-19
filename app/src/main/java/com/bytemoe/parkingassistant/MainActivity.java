package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Moshi moshi = new Moshi.Builder().build();
    private JsonAdapter<GarageBean> garageBeanJsonAdapter = moshi.adapter(GarageBean.class);

    public String HOST = "192.168.1.2";
    public int PORT = 8098;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
                return Utils.byteArrayToInt(header);
            }
        });
        connectionManager.option(builder.build());
        connectionManager.registerReceiver(new SocketActionAdapter() {

            @Override
            public void onSocketReadResponse(final ConnectionInfo info, final String action, final OriginalData data) {
//                Log.e("IGM", Arrays.toString(data.getBodyBytes()));
                Log.e("IGM", Arrays.toString(data.getHeadBytes()));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String dataStr = new String(data.getBodyBytes());
                        Log.e("IGM", dataStr);
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
                                    new Garage(new String(garage.name.getBytes()), garage.distance, garage.total, garage.remaining, garage.parkId)
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

        String[] data = {"A1001", "A1002", "A1010", "B1009", "B1016", "A1001", "A1002", "A1010", "B1009", "B1016", "A1001", "A1002", "A1010", "B1009", "B1016",};

        DataManager.getInstance().getGarageList().add(new Garage("花滩大道二号育才职教中心车库", 1, 100, 5, data));
        DataManager.getInstance().getGarageList().add(new Garage("丁香郡车库", 3, 300, 62, data));
        DataManager.getInstance().getGarageList().add(new Garage("马赛庄园车库", 4, 350, 47, data));
        DataManager.getInstance().getGarageList().add(new Garage("东海滨江城车库", 6, 250, 29, data));
        DataManager.getInstance().getGarageList().add(new Garage("盒子萌总部", 327, 500, 237, data));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionManager.isConnect()) connectionManager.send(new m2sPKG());
                    }
                });
            }
        }, 0, 5000);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        this.garageList = null;
//        this.garageList = DataManager.getInstance().getGarageList();
//        ((GarageAdapter) Objects.requireNonNull(DataManager.getInstance().getStore().get("garageAdapter"))).notifyDataSetChanged();
        Log.e("IGM", "Resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.getInstance().getGarageList().clear();
    }

    public static class m2sPKG implements ISendable {
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
}
