package com.example.myapplication;

import java.util.List;

public interface ResponseCallback {
    void onResponseReceivedL(List<String> response);
    void onResponseReceived(String response);


}
