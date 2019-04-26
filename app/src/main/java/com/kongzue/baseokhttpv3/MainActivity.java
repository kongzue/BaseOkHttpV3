package com.kongzue.baseokhttpv3;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kongzue.baseokhttp.BaseWebSocket;
import com.kongzue.baseokhttp.HttpRequest;
import com.kongzue.baseokhttp.listener.ResponseInterceptListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.listener.WebSocketStatusListener;
import com.kongzue.baseokhttp.util.BaseOkHttp;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.util.JsonUtil;
import com.kongzue.baseokhttp.util.Parameter;

import baseokhttp3.Response;
import baseokio.ByteString;

public class MainActivity extends AppCompatActivity {
    
    private Context context;
    private ProgressDialog progressDialog;
    
    private Button btnHttp;
    private TextView resultHttp;
    private Button btnConnect;
    private Button btnDisconnect;
    private EditText editSend;
    private Button btnSend;
    private TextView resultWebsocket;
    
    private BaseWebSocket baseWebSocket;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        context = this;
    
        btnHttp = findViewById(R.id.btn_http);
        resultHttp = findViewById(R.id.result_http);
        btnConnect = findViewById(R.id.btn_connect);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        editSend = findViewById(R.id.edit_send);
        btnSend = findViewById(R.id.btn_send);
        resultWebsocket = findViewById(R.id.result_websocket);
        
        BaseOkHttp.DEBUGMODE = true;
        BaseOkHttp.serviceUrl = "https://www.apiopen.top";
        BaseOkHttp.overallHeader = new Parameter()
                .add("Charset", "UTF-8")
                .add("Content-Type", "application/json")
                .add("Accept-Encoding", "gzip,deflate")
        ;
        BaseOkHttp.responseInterceptListener = new ResponseInterceptListener() {
            @Override
            public boolean onResponse(Context context, String url, String response, Exception error) {
                if (error != null) {
                    return true;
                } else {
                    Log.i("!!!", "onResponse: " + response);
                    return true;
                }
            }
        };
        
        btnHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
                HttpRequest.POST(context, "/femaleNameApi", new Parameter().add("page", "1"), new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        progressDialog.dismiss();
                        if (error == null) {
                            resultHttp.setText(response);
                        } else {
                            resultHttp.setText("请求失败");
                            Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

//                HttpRequest.build(context,"/femaleNameApi")
//                        .addHeaders("Charset", "UTF-8")
//                        .addParameter("page", "1")
//                        .setResponseListener(new ResponseListener() {
//                            @Override
//                            public void onResponse(String response, Exception error) {
//                                progressDialog.dismiss();
//                                if (error == null) {
//                                    resultHttp.setText(response);
//                                } else {
//                                    resultHttp.setText("请求失败");
//                                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        })
//                        .doPost();


//                progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
//                HttpRequest.JSONPOST(context, "/femaleNameApi", "{\"key\":\"DFG1H56EH5JN3DFA\",\"token\":\"124ASFD53SDF65aSF47fgT211\"}", new ResponseListener() {
//                    @Override
//                    public void onResponse(String response, Exception error) {
//                        progressDialog.dismiss();
//                        if (error == null) {
//                            resultHttp.setText(response);
//                        } else {
//                            resultHttp.setText("请求失败");
//                            Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

//                HttpRequest.build(context,"/femaleNameApi")
//                        .setJsonParameter("{\"key\":\"DFG1H56EH5JN3DFA\",\"token\":\"124ASFD53SDF65aSF47fgT211\"}")
//                        .setResponseListener(new ResponseListener() {
//                            @Override
//                            public void onResponse(String response, Exception error) {
//                                progressDialog.dismiss();
//                                if (error == null) {
//                                    resultHttp.setText(response);
//                                } else {
//                                    resultHttp.setText("请求失败");
//                                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        })
//                        .doPost();

//                File file1 = new File("");
//                File file2 = new File("");

//                progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
//                HttpRequest.POST(context, "/femaleNameApi", new Parameter()
//                                         .add("key", "DFG1H56EH5JN3DFA")
//                                         .add("imageFile1", file1)
//                                         .add("imageFile2", file2)
//                        , new ResponseListener() {
//                            @Override
//                            public void onResponse(String response, Exception error) {
//                                progressDialog.dismiss();
//                                if (error == null) {
//                                    resultHttp.setText(response);
//                                } else {
//                                    resultHttp.setText("请求失败");
//                                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });

//                HttpRequest.build(context,"/femaleNameApi")
//                        .addHeaders("Charset", "UTF-8")
//                        .addParameter("page", "1")
//                        .addParameter("imageFile1", file1)
//                        .addParameter("imageFile2", file2)
//                        .setResponseListener(new ResponseListener() {
//                            @Override
//                            public void onResponse(String response, Exception error) {
//                                progressDialog.dismiss();
//                                if (error == null) {
//                                    resultHttp.setText(response);
//                                } else {
//                                    resultHttp.setText("请求失败");
//                                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        })
//                        .doPost();
            
            }
        });
        
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultWebsocket.setText("开始连接...");
                btnConnect.setEnabled(false);
                baseWebSocket = BaseWebSocket.BUILD(MainActivity.this,"http://fs.fast.im:9508")
                        .setWebSocketStatusListener(new WebSocketStatusListener() {
                            @Override
                            public void connected(Response response) {
                                resultWebsocket.setText("已连接");
                                btnDisconnect.setEnabled(true);
                                btnConnect.setEnabled(false);
                                editSend.setEnabled(true);
                                btnSend.setEnabled(true);
                            }
                
                            @Override
                            public void onMessage(String message) {
                                resultWebsocket.setText("收到返回消息："+message);
                            }
                
                            @Override
                            public void onMessage(ByteString message) {
                    
                            }
                
                            @Override
                            public void onReconnect() {
                                resultWebsocket.setText("正在重连");
                                btnDisconnect.setEnabled(true);
                                btnConnect.setEnabled(false);
                                editSend.setEnabled(false);
                                btnSend.setEnabled(false);
                            }
                
                            @Override
                            public void onDisconnected(int breakStatus) {
                                resultWebsocket.setText("已断开连接");
                                btnDisconnect.setEnabled(false);
                                btnConnect.setEnabled(true);
                                editSend.setEnabled(false);
                                btnSend.setEnabled(false);
                            }
                
                            @Override
                            public void onConnectionFailed(Throwable t) {
                                resultWebsocket.setText("连接失败");
                                btnDisconnect.setEnabled(false);
                                btnConnect.setEnabled(true);
                                editSend.setEnabled(false);
                                btnSend.setEnabled(false);
                                t.printStackTrace();
                            }
                        })
                        .startConnect();
            }
        });
    
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (baseWebSocket!=null)baseWebSocket.disConnect();
            }
        });
        
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editSend.getText().toString().trim();
                if (!s.isEmpty()){
                    if (baseWebSocket!=null)baseWebSocket.send(s);
                }
            }
        });
        
        JsonMap map = JsonUtil.deCodeJsonObject("{\n" +
                                   "    \"status\": 0,\n" +
                                   "    \"msg\": \"\",\n" +
                                   "    \"data\": [\n" +
                                   "        {\n" +
                                   "            \"id\": 1,\n" +
                                   "            \"uid\": 0,\n" +
                                   "            \"news_type\": 1,\n" +
                                   "            \"title\": \"2018全国年月季展\",\n" +
                                   "            \"cover\": [\n" +
                                   "                \"http:\\/\\/cdn.fast.im\\/976e82634164f6d4c154aba1b70694fc.jpg?v=417002\",\n" +
                                   "                \"http:\\/\\/cdn.fast.im\\/7c3a47753f3fce9adcbb485b8910bd6d.jpg?v=160138\",\n" +
                                   "                \"http:\\/\\/cdn.fast.im\\/fbac7e70e4537daf745b47d57efbfd16.jpg?v=537642\"\n" +
                                   "            ],\n" +
                                   "            \"author\": \"莫凡\",\n" +
                                   "            \"original\": \"https:\\/\\/baike.baidu.com\\/item\\/2019%E5%B9%B4%E4%B8%96%E7%95%8C%E6%9C%88%E5%AD%A3%E6%B4%B2%E9%99%85%E5%A4%A7%E4%BC%9A\\/20130890?fr=aladdin\",\n" +
                                   "            \"video\": \"\\/static\\/admin\\/img\\/none.png\",\n" +
                                   "            \"read_num\": 0,\n" +
                                   "            \"share_num\": 0,\n" +
                                   "            \"open_money\": 0,\n" +
                                   "            \"is_money\": 1,\n" +
                                   "            \"type\": 0,\n" +
                                   "            \"create_time\": 1545361503,\n" +
                                   "            \"update_time\": 1545749596,\n" +
                                   "            \"status\": 1,\n" +
                                   "            \"is_top\": 1,\n" +
                                   "            \"sort\": 99,\n" +
                                   "            \"share_point\": \"1\"\n" +
                                   "        },\n" +
                                   "        {\n" +
                                   "            \"id\": 2,\n" +
                                   "            \"uid\": 0,\n" +
                                   "            \"news_type\": 1,\n" +
                                   "            \"title\": \"二月河，走好\",\n" +
                                   "            \"cover\": [\n" +
                                   "                \"http:\\/\\/cdn.fast.im\\/cef46baff49f4430bbc50fbfce422653.jpg?v=178402\"\n" +
                                   "            ],\n" +
                                   "            \"author\": \"舞动华夏\",\n" +
                                   "            \"original\": \"\",\n" +
                                   "            \"video\": \"http:\\/\\/cdn.fast.im\\/6377476da96b78be6529fdd4de4e6320.mp4?v=309054\",\n" +
                                   "            \"read_num\": 0,\n" +
                                   "            \"share_num\": 0,\n" +
                                   "            \"open_money\": 0,\n" +
                                   "            \"is_money\": 1,\n" +
                                   "            \"type\": 1,\n" +
                                   "            \"create_time\": 1545361503,\n" +
                                   "            \"update_time\": 1545749567,\n" +
                                   "            \"status\": 1,\n" +
                                   "            \"is_top\": 0,\n" +
                                   "            \"sort\": 100,\n" +
                                   "            \"share_point\": \"1\"\n" +
                                   "        }\n" +
                                   "    ],\n" +
                                   "    \"page\": {\n" +
                                   "        \"total\": 2,\n" +
                                   "        \"per_page\": 25,\n" +
                                   "        \"current_page\": 1,\n" +
                                   "        \"last_page\": 1\n" +
                                   "    }\n" +
                                   "}");
        Log.i(">>>", "json->map: "+map);
        Log.i(">>>", "getTest.per_page: "+map.getJsonMap("page").getString("per_page"));
        Log.i(">>>", "getTest.title: "+map.getList("data").getJsonMap(1).getString("title"));
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (baseWebSocket!=null)baseWebSocket.disConnect();
    }
}
