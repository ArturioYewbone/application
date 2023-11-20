package com.example.myapplication;

import static android.content.Intent.getIntent;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;

public class SocketService extends Service{
    private SocketOne socketOne;
    private SocketThread socketThread;
    private Messenger messenger = new Messenger(new IncomingHandler());


    @Override
    public void onCreate() {
        super.onCreate();
        socketOne = new SocketOne();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messenger = intent.getParcelableExtra("messenger");
        if(messenger == null){
            Log.d("ddw", "mes null");
        }else{Log.d("ddw", "mes suc");}
        socketThread = new SocketThread();
        socketThread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
    private static final int COMMAND = 1;
    public int getCommand() {
        return COMMAND;
    }
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COMMAND:
                    String command = msg.getData().getString("command");
                    String data = msg.getData().getString("data");
                    Log.d("ddw", "command:" + command);
                    String response = sendCommand(command, data);

                    // Отправляем ответ обратно в Activity
                    Message reply = Message.obtain(null, COMMAND, 0, 0);
                    Bundle bundle = new Bundle();
                    bundle.putString("response", response);
                    reply.setData(bundle);
                    try {
                        msg.replyTo.send(reply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Log.d("ddw", "ошибка в handler в service");
                    break;
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        socketOne.disconnect();
        socketThread.interrupt();
    }

    private class SocketThread extends Thread {
        @Override
        public void run() {
            if (socketOne.connect()) {
                Log.d("ddw", "Socket connected");
            } else {
                Log.d("ddw", "Socket connection failed");
            }
        }
    }
    public String sendCommand(String command, String data) {
        return socketOne.command(command, data);
    }

}