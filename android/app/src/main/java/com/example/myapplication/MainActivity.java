package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import kotlin.jvm.internal.Ref;

public class MainActivity extends AppCompatActivity {
    private List<Item> favor_list = new ArrayList<Item>();
    private List<Item> every_list = new ArrayList<Item>();

    private String ip = "82.179.140.18";
    private int port = 45127;
    LinearLayout cont_s;
    EditText search_f = null;
    String stringSearch = "";
    private Adapter a;
    String command;
    Thread thread;
    RecyclerView rv;
    String login;
    Boolean flag = true;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        command = "every_open";

        Intent intent = getIntent();
        login = intent.getStringExtra("log"); // Извлечь переданные данные
        Log.d("ddw", login);

        cont_s = findViewById(R.id.cont_s);

        rv = findViewById(R.id.recV);
        thread = new Thread(socketThread);
        thread.start();
        while(flag){

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        a = new Adapter(this, favor_list);
        rv.setAdapter(a);
        CloseS(null);
        a.setAdapter(a);
    }
    public void Fav(View v){
        hideKeyboard(v);
        CloseS(v);
        updateFavorList();
    }
    public void updateFavorList(){
        favor_list.clear();
        for(int i = 0; i < every_list.size(); i++){
            if(every_list.get(i).getFavor()){
                favor_list.add(every_list.get(i));
                Log.d("ddw", "add favor:" + every_list.get(i).getshName());
            }
        }
        a.setData(favor_list);
        a.notifyDataSetChanged();
    }

    Thread socketThread = new Thread(new Runnable() {
        @Override
        public void run() {
            String test = "";
            try {
                Socket socket = new Socket(ip, port);
                if (socket.isConnected()) {
                    Log.d("ddw", "connect main");
                }else {
                    Log.d("ddw", "dont connect main");
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bufferedWriter.write(command);
                bufferedWriter.flush();
                String serverResponse = bufferedReader.readLine();
                serverResponse = bufferedReader.readLine();
                Log.d("ddw", serverResponse);
                String tempServ = serverResponse.substring(0, 1);
                List<String> temp = new ArrayList<>();
                List<String> temp_fav = new ArrayList<>();
                while (!tempServ.equals("*")) {
                    //Log.d("ddw", serverResponse);
                    temp.add(serverResponse);
                    serverResponse = bufferedReader.readLine();
                    tempServ = serverResponse.substring(0, 1);
                }
                Log.d("ddw", "list len:" + Integer.toString(temp.size()));
                String s = temp.get(temp.size() - 2);
                s = s.substring(0, s.length() - 2);
                temp.set(temp.size() - 3, s);
                serverResponse = bufferedReader.readLine();
                while (!serverResponse.equals("*")) {
                    //Log.d("ddw", serverResponse);
                   // Log.d("ddw", "bool(*) " + serverResponse);
                    temp_fav.add(serverResponse);
                    serverResponse = bufferedReader.readLine();
                }
                //Log.d("ddw", "temp fav len: " + temp_fav.size());
                for(int i = 0; i < temp.size()/2 - 1; i++){
                    boolean f = false;

                    String[] parts = temp.get(i).split(", ");
                    String firstWord = parts[0].replaceAll("\"", ""); // Удаляем кавычки
                    String secondWord = parts[1].replaceAll("\"", "");
                    String fp = temp.get(temp.size() / 2 + i);
                    if(fp.equals("null")){
                        continue;
                    }
                    for(int j = 0; j < temp_fav.size(); j++){
                        if(firstWord.equals(temp_fav.get(j))){
                            Log.d("ddw", firstWord);
                            f = true;
                            temp_fav.remove(j);
                        }
                    }
                    //Log.d("ddw", "s "+ secondWord + " f "+firstWord + " p " + fp);
                    test = "s "+ secondWord + " f "+firstWord + " p " + fp;
                    //Log.d("ddw", test);
                    float p = Float.parseFloat(fp);
                    Item t = new Item(secondWord, firstWord, p, f);
                    every_list.add(t);
                }
                flag = false;
                bufferedWriter.write("quit");
                bufferedWriter.flush();
                socket.close();
            } catch (Exception e) {
                Log.d("ddw", "test " + test);
                Log.d("ddw", e.getMessage());

            }
        }
        public void S(){

        }
    });
    public void soc(View v) {

        // Запустите поток
        socketThread.start();
    }
    public void Every_list(View v){
        OpenS();
        a.setData(every_list);
        a.notifyDataSetChanged();
    }
    private void CheckFavor(){

    }
    private void OpenS(){
        moveRecV(0);
        if(search_f == null){
            return;
        }
        search_f = new EditText(MainActivity.this);
        search_f.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        search_f.setHint("Search");
        cont_s.addView((search_f));
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("ddw", "i1:"+i1+"i2:"+i2);
                if(i1 > i2){
                    Log.d("ddw","del");
                    stringSearch = stringSearch.substring(0, stringSearch.length() - 1);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("ddw", "Import"+charSequence);
                stringSearch = charSequence.toString();
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        search_f.addTextChangedListener(textWatcher);
    }
    private void CloseS(View v){
        if(search_f != null){cont_s.removeView(search_f);}
        stringSearch = "";
        moveRecV(-150);
    }
    private void search(){
        Log.d("ddw", stringSearch);
        List<String> tempA = new ArrayList<String>();
        for(int i = 0; i< every_list.size(); i++){
            int len = stringSearch.length();
            String name = every_list.get(i).getName();
            String shName = every_list.get(i).getshName();
            if(name.length()<len && shName.length()<len){continue;}
            if(name.substring(0,len).equals(stringSearch) || shName.substring(0,len).equals(stringSearch)){
                tempA.add(shName);
            }
        }
        if(tempA.size()==0){
            List<Item> l = new ArrayList<>();
            l.add(new Item("Ничего не найдено"));
            a = new Adapter(this, l);
            rv.setAdapter(a);
        }else {

        }

    }


    private void hideKeyboard(View view) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void moveRecV(int ot){
        //RecyclerView rv = findViewById(R.id.recV);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) rv.getLayoutParams();
        p.setMargins(0,ot,0,0);
        rv.setLayoutParams(p);
    }
}

