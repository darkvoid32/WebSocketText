package com.sccl.nikonikonii.wstest;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private OkHttpClient client;
    private static MainActivity mA;
    private WebSocket ws;
    private static int request = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.textView);
        mA = MainActivity.this;
        client = new OkHttpClient();

        start();
    }

    private void start() {
        Request request = new Request.Builder().url("http://dcautomata.com/api/ws").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
         ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }


    private static void output(final String txt) {
        mA.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                mA.tv.setText(mA.tv.getText().toString() + "\n\n" + txt);
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                request = 1;
                EchoWebSocketListener.sendRequest2(ws);
                break;
            case R.id.button2:
                 request = 2;
                EchoWebSocketListener.sendRequest1(ws);
                break;
            case R.id.button3:
                request = 3;
                EchoWebSocketListener.sendRequest3(ws);
                break;
        }
    }


    private static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            mA.tv.setText("Ready!");
/*
            webSocket.close(NORMAL_CLOSURE_STATUS, "Closing OkHttp connection");
*/
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            //output("Receiving : " + text);
            Log.w("text", text);
            JSONObject jO = null;
            try {
                jO = new JSONObject(text);
                if (request == 2) {
                    output(jO.getJSONObject("body").toString());
                } else {
                    output(jO.getJSONArray("body").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
        }


        public static void sendRequest1(WebSocket ws){
            mA.tv.setText("");
            Log.w("SENDING REQUEST", "TASK 1");
            ws.send("{\"method\":\"GET\",\"url\":\"/api/tasks/1\"}");
        }

        public static void sendRequest2(WebSocket ws){
            mA.tv.setText("");
            Log.w("SENDING REQUEST", "TASK");
            ws.send("{\"method\":\"GET\",\"url\":\"/api/tasks\"}");
        }

        public static void sendRequest3(WebSocket ws){

            Bitmap ss = BitmapFactory.decodeResource(mA.getResources(), R.drawable.ss);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ss.compress(Bitmap.CompressFormat.JPEG,50,stream);

            byte[] byteArray = stream.toByteArray();
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);


            mA.tv.setText("");
            Log.w("SENDING REQUEST", "TASK");
            ws.send("{\"method\":\"GET\",\"url\":\"/api/tasks\"}");
        }
    }
}
