package com.example.myapplication;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Objects;

public class Input extends AppCompatActivity implements ResponseCallback{
    private String ip = "82.179.140.18";
    private int port = 45127;
    private EditText log;
    private EditText pas;
    private String savedLogin;
    private String savedPassword;
    boolean createNewLog = false;
    boolean wait;
    private SharedPreferences sharedPreferences;
    private Socket socket;
    private static final long INTERVAL = 15 * 60 * 1000; // 15 минут в миллисекундах
    private CommandService mService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.LocalBinder binder = (CommandService.LocalBinder) service;
            mService = binder.getService();
            mService.setResponseCallback(Input.this);
            Log.d("ddw", "сервис подключен");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.activity_input);
            log = findViewById(R.id.log);
            pas = findViewById(R.id.pas);
            final ImageButton eye = findViewById(R.id.imageBut);
            pas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eye.setSelected(true);
            eye.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        eye.setSelected(true);
                        pas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        pas.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        eye.setSelected(false);
                    }
                    return false;
                }
            });
            Intent intent = new Intent(this, CommandService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
            sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            savedLogin = sharedPreferences.getString("login", null);// Попытка получения сохраненного логина и пароля
            savedPassword = sharedPreferences.getString("password", null);
            if (savedLogin != null && savedPassword != null) {// Автоматически заполните поля ввода
                if(false){
                    log.setText(savedLogin);
                    pas.setText(savedPassword);
                    openMain();
                }
            }
        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            sendCommandToService("quit");
            unbindService(mConnection);
            mBound = false;
        }
    }
    public void sendCommandToService(String s) {
        if (mBound) {// отправки команды из активити в сервис
            mService.sendCommand(s);
        }
    }

    @Override
    public void onResponseReceived(String response) {
        Log.d("ddw", "Response from server received in activity: " + response);
        switch (response) {// Обработка полученного ответа от сервера в активити
            case "Сервер не отвечает, попробуйте позже":
                createMsgbox(response, false);
                break;
            case "true":
                SavePas(sharedPreferences.edit());
                openMain();
                break;
            case "falselog":
                createNewLog = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createMsgbox("Пользователь не найден. Вы хотите создать нового?", true);
                    }
                });
                break;
            case "falsepas":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createMsgbox("Неверный пароль", false);
                    }
                });
                break;
        }
    }
    public void clickB(View v){
        savedLogin = log.getText().toString();
        savedPassword = pas.getText().toString();
        if(!(Objects.equals(savedLogin, "") || Objects.equals(savedPassword, ""))) {
            sendCommandToService("input " + savedLogin + " " + savedPassword);
        }else{
            createMsgbox("Не все поля заполнены.", false);
        }
    }

    private void createMsgbox(String s, boolean No){// Создание диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(Input.this);
        builder.setTitle("");
        builder.setMessage(s);
        String ok = "OK";// Кнопка "OK" для закрытия диалогового окна
        if(No){
            ok = "Да";
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override// Кнопка "Нет"
                public void onClick(DialogInterface dialog, int which) {// Действия при нажатии "Нет"
                    dialog.dismiss(); // Закрыть диалоговое окно
                }
            });
        }
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override// Действия при нажатии "да\ок"
            public void onClick(DialogInterface dialog, int which) {
                if(createNewLog == true){
                    sendCommandToService("createnewlog " + savedLogin + " " + savedPassword);
                    createNewLog = false;
                }
                dialog.dismiss(); // Закрыть диалоговое окно
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();// Показать диалоговое окно
    }
    private void openMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("log", savedLogin);
        unbindService(mConnection);
        startActivity(intent);
    }
    private void SavePas(SharedPreferences.Editor ed){//Сохранение логина и пароля
        ed.putString("login", savedLogin);
        ed.putString("password", savedPassword);
        ed.apply(); // Применить изменения
    }
}