package com.example.myapplication;
import android.app.Application;
import android.content.ServiceConnection;

public class MyApp extends Application {
    public static ServiceConnection connection;
    public static CommandService commandService;
}
