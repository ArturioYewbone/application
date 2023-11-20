package com.example.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Objects;

public class MyThread extends Thread{
    private String data;
    private String command;
    private String resul;
    private Object lock = new Object();
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    public MyThread(String s){
        this.command = s;
    }
    public void UpdateData(String a){
        synchronized (lock) {
            this.data = a;
        }
    }
    public void UpdateCommand(String a){
        synchronized (lock) {
            this.command = a;
        }
    }
    public String getResul(){
        return resul;
    }
    @Override
    public void run() {
        String ip = "82.179.140.18";
        int port = 45127;
        String test = "до";
        try {
            Socket socket = new Socket(ip, port);
            test = "сокет работает";
            if (socket.isConnected()) {
                Log.d("ddw", "connect");
                resul = "suc";
            }else {
                Log.d("ddw", "dont connect");
                resul = "er";
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            resul = bufferedReader.readLine();
            while(true){
                synchronized (lock){
                    if(isInterrupted()){
                        bufferedWriter.write("quit");
                        bufferedWriter.flush();
                        socket.close();
                        return;
                    }
                    if(!("".equals(command))){
                        bufferedWriter.write(command + " " + data);
                        bufferedWriter.flush();
                        resul = bufferedReader.readLine();
                    }
//                    switch (command){
//                        case "add_favor":
//                        case "rem_favor":
//                            bufferedWriter.write(command + " " + data);
//                            bufferedWriter.flush();
//                            resul = bufferedReader.readLine();
//                            Log.d("ddw", "состояние add_favor : " + resul);
//                            break;
//                        case "input":
//                            bufferedWriter.write(command + " " + data);
//                            bufferedWriter.flush();
//                            resul = bufferedReader.readLine();
//                        default:
//                            break;
//                    }
//                    if(Objects.equals(command, "add_favor") || Objects.equals(command, "rem_favor")){
//
//                    }
                    command ="";
                }
            }
        } catch (IOException ex) {
            Log.e("ddw", "IOException при создании сокета: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }catch (Exception e) {
            Log.d("ddw", "test " + test);

            Log.e("ddw", e.getMessage());
            Log.e("ddw", Log.getStackTraceString(e));
            return;
        }
    }
    public void Stophread(){
        interrupt();
    }
}

