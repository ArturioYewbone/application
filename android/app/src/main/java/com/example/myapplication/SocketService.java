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
import java.util.concurrent.Exchanger;

public class SocketService extends Service implements IMessenger{
    private SocketOne socketOne;
    private SocketThread socketThread;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    private String cmd;
    private String data;
    private String resp;
    private final Object lock = new Object();
    Exchanger<String> exchanger;


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
        exchanger = new Exchanger<>();
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
                String recvMessage = msg.getData().getString("message");
                String[] mas = recvMessage.split(" ");
                Log.d("ddw","прием в сервисе:"  + recvMessage);
                setCmd(mas[0]);
                if (getCmd().equals("input")) {
                    setData(mas[1] + " " + mas[2]);
                    while (getResp() == null){};
                    Log.d("ddw", "ответ от сервера в сервисе" + getResp());
                    Message sendMsg = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", getResp());
                    sendMsg.setData(bundle);
                    sendMessage(sendMsg);
                    setResp(null);
                }
            }catch (Exception e){
                Log.d("ddw", e.getMessage());
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
            while(!isInterrupted()){
                synchronized(lock) {
                    while (getData() == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    Log.d("ddw", "данные в потоке перед отправкой:" + data);

                    switch (getCmd()) {
                        case "input":
                            setResp(socketOne.command(getCmd(), getData()));
                            setCmd(null);
                            setData(null);
                            synchronized(lock) {
                                lock.notify();
                            }
                            break;
                    }

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
    //region синхронизация
    public synchronized void setCmd(String cmd) {
        synchronized (lock) {
            this.cmd = cmd;
        }
    }
    public synchronized String getCmd() {
        synchronized (lock) {
            return cmd;
        }
    }
    public synchronized void setData(String data) {
        synchronized (lock) {
            this.data = data;
        }
    }
    public synchronized String getData() {
        synchronized (lock) {
            return data;
        }
    }
    public synchronized void setResp(String resp) {
        synchronized (lock) {
            this.resp = resp;
        }
    }
    public synchronized String getResp() {
        synchronized (lock) {
            return resp;
        }
    }
    //endregion
}
