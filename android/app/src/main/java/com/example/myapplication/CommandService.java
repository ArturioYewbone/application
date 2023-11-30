package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandService extends Service {

    private static final String SERVER_IP = "82.179.140.18"; // Замените на ваш IP сервера
    private static final int SERVER_PORT = 45127; // Замените на ваш порт сервера
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ResponseCallback responseCallback;
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
    public class LocalBinder extends Binder {
        CommandService getService() {
            return CommandService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ddw", "start service");
        executorService.execute(() -> {
            try {
                connectToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFromServer();
    }

    private void connectToServer() throws IOException {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            Log.d("ddw", "Connected to server");
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            Log.d("ddw", "Received response from server: " + response);
        }catch (Exception e){
            Log.d("ddw", "error create soc" + e.getMessage());
        }

    }
    public void setResponseCallback(ResponseCallback callback) {
        this.responseCallback = callback;
    }
    public void sendCommand(String command) {
        executorService.execute(() -> {
            if (outputStream != null) {
                try {
                    outputStream.write(command.getBytes());
                    outputStream.flush();
                    Log.d("ddw", "Sent command to server: " + command);
                    // Чтение ответа от сервера
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);
                    String response = new String(buffer, 0, bytesRead);
                    Log.d("ddw", "Received response from server: " + response);

                    // Передаем ответ в активити через callback
                    if (responseCallback != null) {
                        responseCallback.onResponseReceived(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void disconnectFromServer() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            Log.d("ddw", "Disconnected from server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
