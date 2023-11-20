package com.example.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketOne {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String ip = "82.179.140.18";
    private int port = 45127;
    public SocketOne(){

    }
    public boolean connect() {
        try {
            socket = new Socket(ip, port);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader.readLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (Exception ex){
            Log.d("ddw", ex.getMessage());
            return false;
        }
    }
    public String command(String cmd, String data){
        try {
            bufferedWriter.write(cmd + " " + data);
            bufferedWriter.flush();
            return bufferedReader.readLine();
        }catch (IOException e){
            Log.d("ddw", e.getMessage());
            return "createloger";
        }catch (Exception ex){
            Log.d("ddw", ex.getMessage());
            return "createloger";
        }

    }
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
