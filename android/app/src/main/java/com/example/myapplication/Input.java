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

import java.util.List;
import java.util.Objects;

public class Input extends AppCompatActivity implements ResponseCallback{
    private EditText log;
    private EditText pas;
    private String savedLogin;
    private String savedPassword;
    boolean createNewLog = false;
    private SharedPreferences sharedPreferences;
    private static final long INTERVAL = 15 * 60 * 1000; // 15 минут в миллисекундах
    private CommandService mService;
    private boolean mBound = false;
    private Intent serviceIntent;
    boolean openMain = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.activity_input);
            log = findViewById(R.id.log);
            pas = findViewById(R.id.pas);
            ImageButton eye = findViewById(R.id.imageBut);
            eye.setImageResource(R.drawable.eye_selector);
            pas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eye.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            eye.setSelected(true);
                            pas.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            eye.setSelected(false);
                            pas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            break;
                    }
                    return false;
                }
            });
            sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            savedLogin = sharedPreferences.getString("login", null);// Попытка получения сохраненного логина и пароля
            savedPassword = sharedPreferences.getString("password", null);
            serviceIntent = new Intent(this, CommandService.class);
            startService(serviceIntent);
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            if (savedLogin != null && savedPassword != null) {// Автоматически заполните поля ввода
                log.setText(savedLogin);
                pas.setText(savedPassword);
                sendCommandToService("input " + savedLogin + " " + savedPassword);
            }

        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ddw", "onDestroy: input");
        if (mBound) {
            if(!openMain){
                Log.d("ddw", "close service in input");
                sendCommandToService("quit");
                mBound = false;
                stopService(serviceIntent);
            }
        }
    }
    public void sendCommandToService(String s) {
        if (mBound) {// отправки команды из активити в сервис
            mService.sendCommand(s);
        }
    }
    @Override
    public void onResponseReceivedL(List<String> response) {}
    @Override
    public void onResponseReceived(String response) {
        //Log.d("ddw", "Response from server received in activity: " + response);
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
        Intent intent = new Intent(Input.this, MainActivity.class);
        intent.putExtra("log", savedLogin);
        unbindService(mConnection);
        openMain = true;
        startActivity(intent);
        finish();
    }
    private void SavePas(SharedPreferences.Editor ed){//Сохранение логина и пароля
        ed.putString("login", savedLogin);
        ed.putString("password", savedPassword);
        ed.apply(); // Применить изменения
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.LocalBinder binder = (CommandService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setResponseCallback(Input.this);
            if (mService != null) {
                // Вызываем метод вашего сервиса
                Log.d("ddw", "сервис в инпут запущен");

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ddw", "onServiceDisconnected in input");
            mBound = false;
        }
    };
}