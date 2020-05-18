package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.core.protocol.IReaderProtocol;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Handler handler;

    //    public String HOST = "192.168.0.106";
    public String HOST = "10.1.1.4";
    public int PORT = 8080;

    private List<Garage> garageList = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SLog.setIsDebug(true);
//        OkSocketOptions.setIsDebug(true);

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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, Arrays.toString(data.getBodyBytes()), Toast.LENGTH_SHORT).show();
                        connectionManager.send(new ISendable() {
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

        final GarageAdapter garageAdapter = new GarageAdapter(MainActivity.this, R.layout.garage_item, DataManager.getInstance().getGarageList());
        ListView listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, GarageDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", DataManager.getInstance().getGarageList().get(position).getName());
                bundle.putInt("distance", DataManager.getInstance().getGarageList().get(position).getDistance());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        listView.setAdapter(garageAdapter);
        DataManager.getInstance().getStore().put("garageAdapter", garageAdapter);

        DataManager.getInstance().getGarageList().add(new Garage("花滩大道二号育才职教中心车库", 1, 100, 5));
        DataManager.getInstance().getGarageList().add(new Garage("丁香郡车库", 3, 300, 62));
        DataManager.getInstance().getGarageList().add(new Garage("马赛庄园车库", 4, 350, 47));
        DataManager.getInstance().getGarageList().add(new Garage("东海滨江城车库", 6, 250, 29));
        DataManager.getInstance().getGarageList().add(new Garage("盒子萌总部", 327, 500, 237));
        garageList = DataManager.getInstance().getGarageList();
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
        this.garageList = null;
        this.garageList = DataManager.getInstance().getGarageList();
        ((GarageAdapter) Objects.requireNonNull(DataManager.getInstance().getStore().get("garageAdapter"))).notifyDataSetChanged();
        Log.e("IGM", "Resume");
    }

    public static class m2sPKG implements ISendable {
        private String str = "";

        m2sPKG() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cmd", "fetch");
                str = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] parse() {
            byte[] body = str.getBytes(Charset.defaultCharset());
            ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
            bb.order(ByteOrder.BIG_ENDIAN);
//            bb.putInt(body.length);
            bb.put(body);
            return bb.array();
        }
    }
}
