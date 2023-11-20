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

public class Input extends AppCompatActivity {
    private Messenger messenger;
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
    //MyThread soc;
    private static final long INTERVAL = 15 * 60 * 1000; // 15 минут в миллисекундах
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            messenger = null;
            isBound = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
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

            Intent intent = new Intent(this, SocketService.class);
            messenger = new Messenger(new IncomingHandler());
            intent.putExtra("messenger", messenger);

            startService(intent);

            //bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            sendCommand(intent);
            //sendCommand();
//            Thread s = new Thread(socketThread);
//            s.start();
//            soc = new SocketOne();
//            soc.connect();
            //soc = new MyThread("");
            //Log.d("ddw","запуск потока");
            //soc.run();
            //Log.d("ddw","запущен поток");
            //while(!("suc".equals(soc.getResul())||"er".equals(soc.getResul()))){}
           // Log.d("ddw","после цикла");
//            if(!(soc.connect())){
//                createMsgbox("Сервер не доступен, попробуйте позже", false);
//                Button b = findViewById(R.id.input);
//                b.setEnabled(false);
//                soc.disconnect();
//                return;
//            }
            // Получение объекта SharedPreferences
            sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            // Попытка получения сохраненного логина и пароля
            savedLogin = sharedPreferences.getString("login", null);
            savedPassword = sharedPreferences.getString("password", null);
            Log.d("ddw", savedLogin+":"+savedPassword);
            //servInt = new Intent(this, SocketService.class);
            if (savedLogin != null && savedPassword != null) {
                // Автоматически заполните поля ввода
                if(false){
                    log.setText(savedLogin);
                    pas.setText(savedPassword);
                    checkLogPas();
                }


                //servInt.putExtra("log", savedLogin);
                //startService(servInt);
//            Thread thread = new Thread(socketThread);
//            thread.start();
                //OpenMain();
            }
        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }

        //servInt.putExtra("log", savedLogin);
        //startService(servInt);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0, servInt, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        long interval = 15 * 60 * 1000; // интервал в миллисекундах (например, 15 минут)
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
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
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    public void ClickB(View v){
        savedLogin = log.getText().toString();
        savedPassword = pas.getText().toString();
        if(!(Objects.equals(savedLogin, "") || Objects.equals(savedPassword, ""))) {
            if(!(checkLogPas())){
                if(createNewLog){
//                    soc.UpdateData(savedLogin+ " " + savedPassword);
//                    soc.UpdateCommand("createnewlog");

                    createNewLog = false;
                    //while (true){
                        //if("createlogsuc".equals(soc.getResul()))
                    //String s = soc.command("createnewlog", savedLogin+ " " + savedPassword);
//                        if(s.equals("createlogsuc")){
//                            SavePas(sharedPreferences.edit());
//                            OpenMain();
//                            //break;
//                        }else if ("createloger".equals(s)){
//                            createMsgbox("Произошла ошибка при создании нового пользоавтеля", false);
//                            //break;
//                        }
                    //}
                }
            }
            //AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //Intent intent = new Intent(this, SocketService.class);
            //startService(intent);
            //PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //long triggerTime = System.currentTimeMillis();
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, INTERVAL, pendingIntent);

//            Thread thread = new Thread(socketThread);
//            thread.start();
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
        //Intent in = new Intent(Input.this, MainActivity.class);
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
    // Отправка команды
    public void sendCommand(Intent intent) {
        try{
        // Получить Messenger из Service
        Messenger serviceMessenger = intent.getParcelableExtra("messenger");

// Создать Message
        Message msg = Message.obtain(null, 1);
        Bundle bundle = new Bundle();
        bundle.putString("command", "send");
        bundle.putString("data", "Hello");
        msg.setData(bundle);

            // Отправить Message в Service через Messenger
            serviceMessenger.send(msg);
        }catch (Exception e){
            Log.d("ddw", e.getMessage());
        }



    }
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String response = msg.getData().getString("response");
                    Log.d("ddw", response);
                    // Обработка ответа
                    break;
                default:
                    Log.d("ddw", "ошибка в handler в инпут");
                    break;
            }
        }
    }
}