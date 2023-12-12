package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.annotation.SuppressLint;
import android.content.ServiceConnection;
import android.os.IBinder;
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
import java.util.Objects;

import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import kotlin.jvm.internal.Ref;

public class MainActivity extends AppCompatActivity implements ResponseCallback{
    private List<Item> favor_list = new ArrayList<Item>();

    private List<Item> every_list = new ArrayList<Item>();
    private CommandService mService;
    private boolean mBound = false;
    private Intent serviceIntent;

    LinearLayout cont_s;
    EditText search_f = null;
    String stringSearch = "";
    private Adapter a;
    String command;
    RecyclerView rv;
    String login;
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
        serviceIntent = new Intent(this, CommandService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    protected void onStart() {
        super.onStart();
        //sendCommandToService("bhb");
        //sendCommandToService("every_open");
        //a = new Adapter(this, favor_list, mService);
        //rv.setAdapter(a);
        CloseS(null);
        //a.setAdapter(a);
    }
    public void Fav(View v){
        hideKeyboard(v);
        CloseS(v);
        sendCommandToService("get_favor");
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
    public void Every_list(View v){
        sendCommandToService("every_open");
        OpenS();
        a.setData(every_list);
        a.notifyDataSetChanged();
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
            a = new Adapter(this, l, mService);
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
    @Override
    public void onResponseReceivedL(List<String> response) {
        Log.d("ddw", "Response from server received in main activity: " + response.size());
        if(Objects.equals(response.get(0), "get_favor")){
            //log(response.get(0));
            response.remove(0);
            //log("res len" + response.size());
            for(int i = 0; i < response.size(); i++){
                String s = response.get(i);
                //log("favor in main " + s);
                Item it = new Item(s);
                favor_list.add(it);
            }
            sendCommandToService("every_open");
        }else{
            //log(response.get(0));
            for(int i = 0; i < (response.size() / 2); i++){
                boolean fav = false;
                String full = response.get(i);
                String price = response.get((response.size()/2)+i);
                if(price.equals("null")){
                    continue;
                }
                String[] parts = full.split(", ");
                parts[0] = parts[0].substring(1, parts[0].length() - 1);
                parts[1] = parts[1].substring(1, parts[1].length() - 1);
                int j = favor_list.size();
                while(j != 0){
                    if(favor_list.get(j).getshName().equals(parts[0])){
                        fav = true;
                        favor_list.remove(j);
                        break;
                    }
                    j--;
                }
                //log(parts[0] + ":" + parts[1] + " " + price);
                Item it = new Item(parts[0], parts[1], Float.parseFloat(price), fav);
                every_list.add(it);
            }
        }

    }
    @Override
    public void onResponseReceived(String response) {}
    public void sendCommandToService(String s) {
        if (mBound) {// отправки команды из активити в сервис
            mService.sendCommand(s);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("destroy in main activity");
        if (mBound) {
            log("stop service in main activity");
            unbindService(mConnection);
            mBound = false;
            stopService(serviceIntent);
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.LocalBinder binder = (CommandService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setResponseCallback(MainActivity.this);
            mService.sendCommand("get_favor");
            if (mService != null) {
                log("сервис бинд мэйн");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            log("serv disconnect in main activity in mConnection");
            mBound = false;
        }
    };
    private void log(String s){
        Log.d("ddw", s);
    }

    public void test(View v){
        log("click");
        if(mService == null){
            log("serbice d main");
        }
        log("mbound " + (mBound));
        mService.sendCommand("asdsa");
    }
}

