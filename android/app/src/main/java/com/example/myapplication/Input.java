package com.example.myapplication;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.spec.ECField;
import java.util.Objects;

public class Input extends AppCompatActivity implements ResponseCallback{
    IMessenger messenger;
    private Handler handler = new Handler();
    private boolean isBound;
    private static final int COMMAND = 1;

    private String ip = "82.179.140.18";
    private int port = 45127;
    EditText log;
    EditText pas;
    String savedLogin;
    String savedPassword;
    boolean createNewLog = false;
    boolean wait;
    SharedPreferences sharedPreferences;
    Intent servInt;
    String command;
    Socket socket;
    private static final long INTERVAL = 15 * 60 * 1000; // 15 минут в миллисекундах
    private CommandService mService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CommandService.LocalBinder binder = (CommandService.LocalBinder) service;
            mService = binder.getService();
            Log.d("ddw", "сервис подключен");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

   // @SuppressLint("ClickableViewAccessibility")
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
            Log.d("ddw","запуск сокета");
            Intent intent = new Intent(this, CommandService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
            //messenger = new SocketService();


            sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            // Попытка получения сохраненного логина и пароля
            savedLogin = sharedPreferences.getString("login", null);
            savedPassword = sharedPreferences.getString("password", null);
            //Log.d("ddw", savedLogin+":"+savedPassword);
            if (savedLogin != null && savedPassword != null) {
                // Автоматически заполните поля ввода
                if(false){
                    log.setText(savedLogin);
                    pas.setText(savedPassword);
                    messenger.sendMessage("input "+ savedLogin + " " + savedPassword);
                }
            }
        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }
    }
    private boolean checkLogPas(){
//        soc.UpdateData(savedLogin+ " " + savedPassword);
//        soc.UpdateCommand("input");
//        String s = soc.command("input", savedLogin+ " " + savedPassword);
//        switch (s){
//            case "true":
//                SavePas(sharedPreferences.edit());
//                OpenMain();
//                return true;
//            case "falselog":
//                createMsgbox("Пользователь не найден. Вы хотите создать нового?", true);
//                return false;
//            case "falsepas":
//                createMsgbox("Неверный пароль", false);
//                return false;
//            default:
//                return false;
//        }
//        while(true){
//            if("true".equals(soc.getResul())){
//                SavePas(sharedPreferences.edit());
//                OpenMain();
//                return true;
//            } else if ("falselog".equals(soc.getResul())) {
//                createMsgbox("Пользователь не найден. Вы хотите создать нового?", true);
//                return false;
//            } else if ("falsepas".equals(soc.getResul())) {
//                createMsgbox("Неверный пароль", false);
//                return false;
//            }
//        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    public void sendCommandToService(View view) {
        if (mBound) {
            // Пример отправки команды из активити в сервис
            mService.sendCommand("Your command here");
        }
    }

    @Override
    public void onResponseReceived(String response) {
        Log.d("ddw", "Response from server received in activity: " + response);

        // Обработка полученного ответа от сервера в активити
        // Здесь вы можете обновить пользовательский интерфейс или выполнить другие действия с ответом от сервера
    }
    public void ClickB(View v){
        savedLogin = log.getText().toString();
        savedPassword = pas.getText().toString();
        if(!(Objects.equals(savedLogin, "") || Objects.equals(savedPassword, ""))) {
            if (mBound) {
                // Пример отправки команды из активити в сервис
                Log.d("ddw", savedLogin + " " + savedPassword);
                mService.sendCommand("input " + savedLogin + " " + savedPassword);
            }
            //messenger.sendMessage("input "+ savedLogin + " " + savedPassword);
        }else{
            createMsgbox("Не все поля заполнены.", false);
        }
    }
    private void createMsgbox(String s, boolean No){

        // Создание диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(Input.this);
        //builder.setTitle("");
        builder.setMessage(s);
        // Кнопка "OK" для закрытия диалогового окна
        String ok = "OK";
        if(No){
            ok = "Да";
            // Кнопка "Нет"
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Действия при нажатии "Нет"
                    dialog.dismiss(); // Закрыть диалоговое окно
                    wait = false;
                }
            });
        }
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewLog = true;
                dialog.dismiss(); // Закрыть диалоговое окно
                wait = false;
            }
        });
        // Показать диалоговое окно
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void OpenMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("log", savedLogin);
        startActivity(intent);
    }

    Thread socketThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                if (socket.isConnected()) {
                    Log.d("ddw", "connect_input");
                }else {
                    Log.d("ddw", "dont connect");
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // потоки для ввода и вывода данных
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String serverResponse = bufferedReader.readLine();
                Log.d("ddw", serverResponse);
                bufferedWriter.write("input "+ savedLogin +" " + savedPassword);
                bufferedWriter.flush();
//                do{
                    serverResponse = bufferedReader.readLine();
//                    socketThread.sleep(10);
//                }while (serverResponse == null);
                Log.d("ddw", serverResponse);
                if(serverResponse.equals("true")){
                    bufferedWriter.write("quit");
                    bufferedWriter.flush();
                    Log.d("ddw", serverResponse);
                    SavePas(sharedPreferences.edit());
                    OpenMain();
                }else if(serverResponse.equals("falselog")){
                    createNewLog=false;
                    wait = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createMsgbox("Пользователь не найден. Вы хотите создать нового?", true);
                        }
                    });
                    while (wait){
                        socketThread.sleep(10);
                    }
                    if(createNewLog){
                        Log.d("ddw", "create");
                        bufferedWriter.write("createnewlog "+ savedLogin +" " + savedPassword);
                        bufferedWriter.flush();
                        serverResponse = bufferedReader.readLine();
                        Log.d("ddw", serverResponse);
                        if(!(serverResponse.equals("suc"))){

                        }
                        bufferedWriter.write("quit");
                        bufferedWriter.flush();
                        OpenMain();
                    }
                }else{
                    Log.d("ddw", "falsepas2");
                    bufferedWriter.write("quit");
                    bufferedWriter.flush();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createMsgbox("Неверный пароль", false);
                        }
                    });
                }
                // Закройте соксет, когда он больше не нужен
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ddw", e.getMessage());
            } catch (InterruptedException e) {
                Log.d("ddw", e.getMessage());
                throw new RuntimeException(e);
            }
            Log.d("ddw", "exit thead");
        }
    });
    private void SavePas(SharedPreferences.Editor ed){
        //Сохранение логина и пароля
        ed.putString("login", savedLogin);
        ed.putString("password", savedPassword);
        ed.apply(); // Применить изменения
    }
    public void PasStar(View v){
//        boolean isChecked = (pas.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//        if(isChecked){
//            pas.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//            v.setSelected(true);
//        }else{
//            pas.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//            v.setSelected(false);
//        }
    }
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String text = msg.getData().getString("message");
            Log.d("ddw", "input.java" + text);
            switch (text){
                case "falselog":
                    break;
                default:
                    break;
            }
        }
    }
}