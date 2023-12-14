package com.example.myapplication;

import static android.icu.lang.UCharacter.toLowerCase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.annotation.SuppressLint;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements ResponseCallback{
    private List<Item> favor_list = new ArrayList<Item>();

    private List<Item> every_list = new ArrayList<Item>();
    private CommandService mService;
    private boolean mBound = false;
    private Intent serviceIntent;
    boolean stat = true;
    LinearLayout cont_s;
    EditText search_f = null;
    String stringSearch = "";
    private Adapter a;
    RecyclerView rv;
    String login;
    boolean favor = true;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        closeS();
    }
    public void fav(View v){
        favor = true;
        hideKeyboard(v);
        closeS();
        updateFavorList();
    }
    private void updateFavorList(){
        favor_list.clear();
        for(int i = 0; i < every_list.size(); i++){
            if(every_list.get(i).getFavor()){
                favor_list.add(every_list.get(i));
            }
        }
        if(favor_list.size() == 0){
            Item it = new Item("Ничего не найдено");
            List<Item> l = new ArrayList<>();
            l.add(it);
            a.setData(l);
        }else{
            a.setData(favor_list);
        }
        rv.setAdapter(a);
    }
    public void everyList(View v){
        stat = false;
        favor = false;
        openS();
        a.setData(every_list);
        rv.setAdapter(a);
        sendCommandToService("every_open");
    }
    private void openS(){
        moveRecV(0);
        try{
            search_f = new EditText(MainActivity.this);
            search_f.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            search_f.setHint("Search");
            cont_s.addView((search_f));
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //Log.d("ddw", "i1:"+i1+"i2:"+i2);
                    if(i1 > i2){
                        //Log.d("ddw","del");
                        stringSearch = stringSearch.substring(0, stringSearch.length() - 1);
                    }
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //Log.d("ddw", "Import "+charSequence);
                    stringSearch = charSequence.toString();
                    search();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };
            search_f.addTextChangedListener(textWatcher);
        }catch (Exception e){
            log(e.getMessage());
        }

    }
    private void closeS(){
        if(search_f != null){
            cont_s.removeView(search_f);
        }
        stringSearch = "";
        moveRecV(-150);
    }
    private void search(){
        Log.d("ddw", stringSearch);
        List<Item> tempA = new ArrayList<>();
        log(""+every_list.size());
        for(int i = 0; i< every_list.size(); i++){
            int len = stringSearch.length();
            String name = every_list.get(i).getName();
            String shName = every_list.get(i).getshName();
            if(name.length()<len && shName.length()<len){
                continue;
            }else if(name.length() < len){
                if(toLowerCase(name).equals(toLowerCase(stringSearch)) || toLowerCase(shName).substring(0,len).equals(toLowerCase(stringSearch))){
                    tempA.add(every_list.get(i));
                }
            }else if(shName.length()<len){
                if(toLowerCase(name).substring(0,len).equals(toLowerCase(stringSearch)) || toLowerCase(shName).equals(toLowerCase(stringSearch))){
                    tempA.add(every_list.get(i));
                }
            }else{
                if(toLowerCase(name).substring(0,len).equals(toLowerCase(stringSearch)) || toLowerCase(shName).substring(0,len).equals(toLowerCase(stringSearch))){
                    tempA.add(every_list.get(i));
                }
            }
        }
        if(tempA.size()==0 && stringSearch.length() > 0){
            List<Item> l = new ArrayList<>();
            Item it = new Item("Ничего не найдено");
            l.add(it);
            a.setData(l);
            rv.setAdapter(a);
        }else {
            a.setData(tempA);
            rv.setAdapter(a);
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
            response.remove(0);
            //log("response.get(0) " + response.get(0));
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
            //log(response.get(1));
            //every_list.clear();
            List<Item> tempEveryList = new ArrayList<>();
            List<Item> temp = new ArrayList<>();
            temp.addAll(favor_list);
            favor_list.clear();
            //if(temp.size()> 0){log("temp.get(0).getshName() " + temp.get(0).getshName());}
            //if(every_list.size() > 0) {log( "every_list.get(0).getshName() " + every_list.get(0).getshName());}
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

                int j = temp.size();
                while(j > 0){
                    //log(temp.get(j-1).getshName()+ " " + temp.get(j-1).getshName().length() + ":" + parts[0]);
                    if(temp.get(j - 1).getshName().equals(parts[0])){
                        //log("add fav");
                        fav = true;
                        temp.remove(j - 1);
                        break;
                    }
                    j--;
                }
                //log(parts[0] + ":" + parts[1] + " " + price);
                Item it = new Item(parts[1], parts[0], Float.parseFloat(price), fav);
                tempEveryList.add(it);
                if(fav){
                    //log("fav " + parts[0] + parts[1]+ price +""+ fav);
                    favor_list.add(it);
                }
            }
            every_list.clear();
            every_list.addAll(tempEveryList);
            if(stat){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(favor_list.size() == 0){
                            Item it = new Item("Ничего не найдено");
                            List<Item> l = new ArrayList<>();
                            l.add(it);
                            a.setData(l);
                        }else{
                            a.setData(favor_list);
                        }
                        rv.setAdapter(a);
                    }
                });
            }
        }
    }
    @Override
    public void onResponseReceived(String response) {
        Log.d("ddw", "Response from server received in main activity: " + response);
        if(response.equals("suc del")){
            if(favor){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFavorList();
                    }
                });

            }
        }
    }
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
            sendCommandToService("quit");
            unbindService(mConnection);
            mBound = false;
            mService.stop();
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.LocalBinder binder = (CommandService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setResponseCallback(MainActivity.this);
            a = new Adapter(MainActivity.this, null, mService);
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

}

