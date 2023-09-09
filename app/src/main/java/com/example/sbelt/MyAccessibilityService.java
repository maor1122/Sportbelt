package com.example.sbelt;

import static com.example.sbelt.MainActivity.socket;
import static com.example.sbelt.utils.DataManager.saveUserDataLocally;
import static com.example.sbelt.utils.utils.BROADCAST_PORT;
import static com.example.sbelt.utils.utils.DELAY;
import static com.example.sbelt.utils.utils.PREFS_NAME;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

import com.example.sbelt.utils.GestureData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private float screenHeight;
    private float screenWidth;
    private long lastSignalTime;
    private String Uid;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {
        System.out.println("Something went wrong...");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LoginEngine loginEngine = new LoginEngine();
        Uid = loginEngine.getUser().getUid();
        screenHeight = getScreenHeight();
        screenWidth = getScreenWidth();
        System.out.println("Starting Accessibility service");
        new Thread(this::mainFunction).start();
    }


    private void mainFunction(){
        System.out.println("UDP server started. Listening for broadcast packets...");
        int gestures=0;
        Date startDate = Calendar.getInstance().getTime();
        try {
            byte[] buffer = new byte[255];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket = new DatagramSocket(BROADCAST_PORT);
            socket.setBroadcast(true);
            long time = System.currentTimeMillis() - DELAY;
            lastSignalTime = System.currentTimeMillis();
            new Thread(this::checkLastSignalTime).start();
            while (true) {
                socket.receive(packet);
                String receivedData = new String(packet.getData(), 0, packet.getLength());
                if (receivedData.equals("R")) {
                    lastSignalTime = System.currentTimeMillis();
                    continue;
                }
                if (time + DELAY > System.currentTimeMillis()) continue;
                time = System.currentTimeMillis();

                String direction = "NULL";
                String[] parameters = receivedData.split("&");
                String[] data;
                for (String param : parameters) {
                    data = param.split("=");
                    System.out.println("Received udp message: " + "Received data: " + receivedData);
                    if (data.length < 2) continue;
                    try {
                        gestures++;
                        switch (data[0]) {
                            case "x":
                                if (Double.parseDouble(data[1]) < 0) {
                                    direction = "UP";
                                } else if (Double.parseDouble(data[1]) > 0) {
                                    direction = "DOWN";
                                }
                                break;
                            case "y":
                                if (Double.parseDouble(data[1]) > 0) {
                                    direction = "LEFT";
                                } else if (Double.parseDouble(data[1]) < 0) {
                                    direction = "RIGHT";
                                }
                        }
                        if (!direction.equals("NULL"))
                            swipe(direction);
                        else
                            gestures--;
                    }catch(NumberFormatException ignored){}
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            if (socket != null)
                socket.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            if(gestures>0) {
                GestureData newGestureData = new GestureData(gestures, startDate, Calendar.getInstance().getTime());
                List<GestureData> lst = getGestureDataList(Uid);
                if (lst == null)
                    lst = new ArrayList<>();
                lst.add(newGestureData);
                System.out.println("Saving locally data of size: "+lst.size());
                saveUserDataLocally(lst, Uid,getApplicationContext());
            }
            disableSelf();
        }
    }



    public List<GestureData> getGestureDataList(String uid) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String gestureDataListJson = preferences.getString(uid, "");
        return gson.fromJson(gestureDataListJson, new TypeToken<List<GestureData>>(){}.getType());
    }

    private void checkLastSignalTime(){
        while(true){
            if(System.currentTimeMillis()-lastSignalTime > 5000){
                if (socket != null) {
                    socket.close();
                    return;
                }
            }
        }
    }

    private void swipe(String direction){
        System.out.println("Swiping:");
        new Thread(() -> {
            switch(direction){
                case "UP":
                    swipeUp();
                    System.out.println("UP");
                    break;
                case "DOWN":
                    swipeDown();
                    System.out.println("DOWN");
                    break;
                case "LEFT":
                    swipeLeft();
                    System.out.println("LEFT");
                    //customSwipeLeft();
                    break;
                case "RIGHT":
                    swipeRight();
                    System.out.println("RIGHT");
                    break;
            }
        }).start();
    }

    public void swipe(Path path){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    public void swipeUp(){
        Path swipePath = new Path();
        float y1 = (float)(screenHeight*0.75);
        float y2 = (float)(screenHeight*0.25);
        float x = screenWidth / 2;
        swipePath.moveTo(x, y1);
        swipePath.lineTo(x, y2);
        swipe(swipePath);
    }

    public void swipeDown(){
        Path swipePath = new Path();
        float y1 = (float)(screenHeight*0.25);
        float y2 = (float)(screenHeight*0.75);

        float x = screenWidth / 2;
        swipePath.moveTo(x, y1);
        swipePath.lineTo(x, y2);
        swipe(swipePath);
}

    public void swipeLeft(){
        Path swipePath = new Path();
        float x1 = (float)(screenWidth*0.75);
        float x2 = (float)(screenWidth*0.25);
        float y = screenHeight / 2;
        swipePath.moveTo(x1, y);
        swipePath.lineTo(x2, y);
        swipe(swipePath);
    }
    public void swipeRight(){
        Path swipePath = new Path();
        float x1 = (float)(screenWidth*0.25);
        float x2 = (float)(screenWidth*0.75);
        float y = screenHeight / 2;
        swipePath.moveTo(x1, y);
        swipePath.lineTo(x2, y);
        swipe(swipePath);
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
}
