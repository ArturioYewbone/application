package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandService extends Service {

    private static final String SERVER_IP = "82.179.140.18"; // Замените на ваш IP сервера
    private static final int SERVER_PORT = 45127; // Замените на ваш порт сервера
    private Socket socket;
    private BufferedReader inputStream;
    private BufferedWriter outputStream;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ResponseCallback responseCallback;
    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
            connectToServer();
        });
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //sendCommand("quit");
        disconnectFromServer();
    }

    private void connectToServer() {
        try {
            if(socket == null) {
                socket = new Socket(SERVER_IP, SERVER_PORT);
            }
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Log.d("ddw", "Connected to server");
            String response = "";
            if(inputStream != null){
                 response = inputStream.readLine();
            }
            Log.d("ddw", "Received response from server: " + response);
        }catch (IOException ex) {
            Log.d("ddw", "error create soc " + ex.getMessage());
            responseCallback.onResponseReceived("Сервер не отвечает, попробуйте позже");
        }catch(Exception e){
            Log.d("ddw", "error create soc " + e.getMessage());
            responseCallback.onResponseReceived("Сервер не отвечает, попробуйте позже");
        }

    }
    public void setResponseCallback(ResponseCallback callback) {
        this.responseCallback = callback;
    }
    public void sendCommand(String command) {
        if(executorService.isShutdown()){
            Log.d("ddw", "ExecutorService был остановлен");
        }
        if(executorService.isTerminated()){
            Log.d("ddw", "все задачи выполнены и ExecutorService остановлен");
        }
        executorService.execute(() -> {
            if (outputStream != null) {
                try {
                    outputStream.write(command);
                    outputStream.flush();
                    Log.d("ddw", "Sent command to server: " + command);
                    if(command.equals("every_open")){
                        String response = inputStream.readLine();
                        //response = inputStream.readLine();
                        String tempServ = response.substring(0, 1);
                        Log.d("ddw", "Received response from server: " + response);
                        List<String> temp = new ArrayList<>();
                        while (!tempServ.equals("*")) {
                            //Log.d("ddw", serverResponse);
                            temp.add(response);
                            response = inputStream.readLine();
                            tempServ = response.substring(0, 1);
                        }
                        Log.d("ddw", "list len:" + Integer.toString(temp.size()));
                        String s = temp.get(temp.size() - 2);
                        s = s.substring(0, s.length() - 2);
                        //Log.d("ddw", s);
                        temp.set(temp.size() - 2, s);
                        //response = inputStream.readLine();
                        //Log.d("ddw", response);
                        responseCallback.onResponseReceivedL(temp);
                    }else if(command.equals("get_favor")){
                        String response = inputStream.readLine();
                        List<String> temp = new ArrayList<>();
                        Log.d("ddw", response + " " + response.length());
                        while(!response.substring(response.length() - 1).equals("*")){
                            temp.add(response);
                            response = inputStream.readLine();
                            Log.d("ddw", response+ " " + response.length());
                        }
                        //Log.d("ddw", "favor:" + temp.size());
                        responseCallback.onResponseReceivedL(temp);

                    }else {
                        // Чтение ответа от сервера
                        String response = inputStream.readLine();
                        Log.d("ddw", "Received response from server: " + response);

                        // Передаем ответ в активити через callback
                        if (responseCallback != null) {
                            responseCallback.onResponseReceived(response);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("ddw", e.getMessage());
                }catch (Exception ee){
                    Log.d("ddw", ee.getMessage());
                }
            }
            else{
                Log.d("ddw", "outputStream == null");
            }
        });
    }

    private void disconnectFromServer() {
        try {

            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            Log.d("ddw", "Disconnected from server");
        }catch (IOException ee){
            Log.d("ddw", ee.getMessage());
        }catch (Exception e) {
            Log.d("ddw", e.getMessage());
            e.printStackTrace();
        }
    }
    public void stop(){
        stopSelf();
    }
}
