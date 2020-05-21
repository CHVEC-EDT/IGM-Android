package com.bytemoe.parkingassistant;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    public String HOST = "igm-io.cedt.bytemoe.com";
    public int PORT = 8098;
//    public String HOST = "192.168.1.2";
//    public int PORT = 8098;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);

        SLog.setIsDebug(false);
        OkSocketOptions.setIsDebug(false);

        ConnectionInfo connectionInfo = new ConnectionInfo(HOST, PORT);
        final IConnectionManager connectionManager = OkSocket.open(connectionInfo);
        OkSocketOptions.Builder builder = new OkSocketOptions.Builder();
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                        String[] filename = ((String) msg.obj).split("/");
                        downLoadUpdate(getApplicationContext(), (String) msg.obj, filename[filename.length - 1]);
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                try {
                    String apkUrl = Utils.checkUpdate(getApplicationContext());
                    msg.obj = apkUrl;
                    Log.e("IGM", String.valueOf(apkUrl));
                    if (apkUrl != null) handler.sendMessage(msg);
                } catch (IOException e) {
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
                Utils.showSnackbar(findViewById(R.id.mainLayout), MainActivity.this, "连接服务器成功");
            }

            @Override
            public void onSocketDisconnection(ConnectionInfo info, String action, final Exception e) {
                super.onSocketDisconnection(info, action, e);
                Utils.showSnackbar(findViewById(R.id.mainLayout), MainActivity.this, "与服务器失去连接", e);
            }

            @Override
            public void onSocketConnectionFailed(ConnectionInfo info, String action, final Exception e) {
                super.onSocketConnectionFailed(info, action, e);
                Utils.showSnackbar(findViewById(R.id.mainLayout), MainActivity.this, "连接服务器失败", e);
            }

            @Override
            public void onSocketReadResponse(final ConnectionInfo info, final String action, final OriginalData data) {
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
    protected void onPostResume() {
        super.onPostResume();
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

    private void downLoadUpdate(Context context, String url, String filename) {
        try {
            Uri uri = Uri.parse(url);
            //得到系统的下载管理
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            //得到连接请求对象
            DownloadManager.Request request = new DownloadManager.Request(uri);
            //指定在什么网络下允许下载
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
            //指定下载文件的保存路径，缓存目录，用手机自带的文件管理器看不到（Android\data\项目包名\files\Download\subPath）
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
            request.setTitle("停车助手");
            request.setDescription("软件更新下载");
            //MIME_MapTable是所有文件的后缀名所对应的MIME类型的一个String数组  比如{".apk",    "application/vnd.android.package-archive"},
            request.setMimeType("application/vnd.android.package-archive");
            // 下载完成后该Notification才会被显示
            // Android 3.0版本 以后才有该方法
            //在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，直到用户点击该Notification或者消除该Notification
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //启动下载,该方法返回系统为当前下载请求分配的一个唯一的ID
            assert manager != null;
            long downLoadId = manager.enqueue(request);
        } catch (Exception e) {
            Uri uri = Uri.parse(url);
            //String android.intent.action.VIEW 比较通用，会根据用户的数据类型打开相应的Activity。如:浏览器,电话,播放器,地图
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    class RecvTask extends TimerTask {
        private IConnectionManager connectionManager;

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
