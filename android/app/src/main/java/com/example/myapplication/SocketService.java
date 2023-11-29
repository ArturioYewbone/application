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
import java.io.PrintStream;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class SocketService extends Service implements IMessenger{
    private SocketOne socketOne;
    private SocketThread socketThread;
    String in = null;
    String out = null;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    dataString dString = new dataString();

    @Override
    public void sendMessage(String message) {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        msg.setData(bundle);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        socketOne = new SocketOne();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        socketThread = new SocketThread();
        socketThread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                synchronized (dString){
                    in = msg.getData().getString("message");
                    Log.d("ddw","прием в сервисе:"  + in);
                    dString.setString(in);
                    Log.d("ddw","данные записали");
                    dString.notify();
                    dString.wait();
                    out = dString.getString();
                    String cmd;
                    String data;
                    Log.d("ddw", "ответ от сервера в сервисе " + out);
                    Message sendMsg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", out);
                    sendMsg.setData(bundle);
                    sendMessage(sendMsg);
                }


            }catch (Exception e){
                Log.d("ddw","mesenger: " + e.getMessage());
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
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
            String cmd;
            String data;
            //String resp;
            while(!isInterrupted()){
                try {
                    synchronized (dString) {
                        Log.d("ddw","в ожидании");
                        dString.wait();
                        TimeUnit.SECONDS.sleep(1);
                        cmd = dString.getString();
                        Log.d("ddw", "данные в потоке перед отправкой:" + cmd);
                        switch (in) {
                            case "input":
                                out = socketOne.command(in, in);
                                cmd = null;
                                data= null;
                                in = null;
                                break;
                            default:
                                break;
                        }
                    }
                }catch (Exception e){
                    Log.d("ddw", "socket: " + e.getMessage());
                }
            }
            // Выполнение действий при прерывании
            socketOne.disconnect();
            Log.d("ddw", "Thread interrupted");

        }
    }

    public String sendCommand(String command, String data) {
        return socketOne.command(command, data);
    }
}
class dataString{
    private String dataString;
    public synchronized void setString(String data) {
        this.dataString = data;
    }
    public synchronized String getString() {
        return dataString;
    }
}
