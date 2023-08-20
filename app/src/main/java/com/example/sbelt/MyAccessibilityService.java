package com.example.sbelt;

import static com.example.sbelt.MainActivity.BROADCAST_PORT;
import static com.example.sbelt.MainActivity.DELAY;
import static com.example.sbelt.MainActivity.socket;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MyAccessibilityService extends AccessibilityService {
    private float screenHeight;
    private float screenWidth;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//        if(accessibilityEvent.getEventType()== AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ) {
//            System.out.println("Event - window content changed");
//            swipeDown();
//        }
    }

    @Override
    public void onInterrupt() {
        System.out.println("Something went wrong...");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        screenHeight = getScreenHeight();
        screenWidth = getScreenWidth();
        System.out.println("Starting Accessibility service");

        new Thread(() -> {
            System.out.println("UDP server started. Listening for broadcast packets...");
            try {
                byte[] buffer = new byte[255];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket = new DatagramSocket(BROADCAST_PORT);
                socket.setBroadcast(true);
                long time = System.currentTimeMillis() - DELAY;
                while (true) {
                    socket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    if (receivedData.length() <= 2) continue;
                    if (time + DELAY > System.currentTimeMillis()) continue;
                    time = System.currentTimeMillis();

                    String direction = "NULL";
                    String[] parameters = receivedData.split("&");
                    String[] data;
                    for (String param : parameters) {
                        data = param.split("=");
                        System.out.println("Received udp message: " + "Received data: " + receivedData);
                        if (data.length < 2) continue;

                        switch (data[0]) {
                            case "x":
                                if (Double.parseDouble(data[1]) > 0) {
                                    direction = "UP";
                                } else if (Double.parseDouble(data[1]) < 0) {
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
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                // TODO: check if its right.
                if (socket != null)
                    socket.close();
                stopSelf();
            }
        }).start();
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
                    System.out.println("LEFT");
                    //swipeLeft();
                    //customSwipeLeft();
                    break;
                case "RIGHT":
                    System.out.println("RIGHT");
                    //swipeRight();
                    break;
            }
        }).start();
    }

    public void swipe(Path path){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 50L));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    public void swipeUp(){
        Path swipePath = new Path();
 //       float y1 = (float)(screenHeight*0.75);
//        float y2 = (float)(screenHeight*0.25);
        float y1 = screenHeight;
        float y2 = 0;
        float x = screenWidth / 2;
        swipePath.moveTo(x, y1);
        swipePath.lineTo(x, y2);
        swipe(swipePath);
    }
    public void customSwipeLeft(){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path[] path = new Path[4];
        float x1 = (float)(screenWidth*0.75);
        float x2 = (float)(screenWidth*0.25);
        float curx1 = x1;
        float curx2 = x1+(x2-x1)/4;
        float y = screenHeight / 2;
        for(int i=0;i<4;i++){
            path[i] = new Path();
            path[i].moveTo(curx1,y);
            path[i].lineTo(curx2,y);
            curx1 = x1+(i+1)*(x2-x1)/4;
            curx2 = x1+(i+2)*(x2-x1)/4;
        }
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path[0], 0L, 10L,true);
        stroke.continueStroke(path[1],0L,10L,true);
        stroke.continueStroke(path[2],0L,10L,true);
        stroke.continueStroke(path[3],0L,10L,false);

        gestureBuilder.addStroke(stroke);
        dispatchGesture(gestureBuilder.build(), null, null);
    }
    public void swipeDown(){
        Path swipePath = new Path();
        //float y1 = (float)(screenHeight*0.25);
        //float y2 = (float)(screenHeight*0.75);
        float y1 = 0;
        float y2 = screenHeight;

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
