package com.example.sbelt;

import static com.example.sbelt.MainActivity.DELAY;
import static com.example.sbelt.MainActivity.BROADCAST_PORT;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MainService extends Service {
    public static DatagramSocket socket;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                String[] data = receivedData.split("=");
                System.out.println("Received udp message: " + "Received data: " + receivedData);
                if(data.length<2) continue;

                switch (data[0]){
                    case "x":
                        if(Double.parseDouble(data[1])>0){
                            direction = "UP";
                        }
                        else if(Double.parseDouble(data[1])<0){
                            direction = "DOWN";
                        }
                        break;
                    case "y":
                        if(Double.parseDouble(data[1])>0){
                            direction = "LEFT";
                        }
                        else if(Double.parseDouble(data[1])<0){
                            direction = "RIGHT";
                        }
                }
                if(!direction.equals("NULL"))
                    swipe(direction);
            }
        }catch (IOException e) {
            e.printStackTrace();
            // TODO: check if its right.
            if (socket != null)
                socket.close();
            stopSelf();
        }
        }).start();

        final String CHANNEL_ID = "Sportbelt Service";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this,CHANNEL_ID)
                .setContentText("Sportbelt Service")
                .setContentTitle("Sportbelt");
        startForeground(1001,notification.build());

        return START_STICKY;
    }

    private void swipe(String direction){
        new Thread(() -> {
            switch(direction){
                case "UP":
                    swipeUP();
                    break;
                case "DOWN":
                    break;
                case "LEFT":
                    break;
                case "RIGHT":
                    break;
            }
        }).start();
    }

    private void swipeUP(){
        swipe(500,500,0,300,5);
    }
    /*
    private void swipe2(float fromX,float toX, float fromY,float toY, int stepCount){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

    }*/
    private void swipe(float fromX,float toX, float fromY,float toY, int stepCount){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        float x = fromX;
        float y = fromY;

        float yStep = (toY/fromY)/stepCount;
        float xStep = (toX/fromX)/stepCount;

        Instrumentation inst = new Instrumentation();

        MotionEvent event = MotionEvent.obtain(downTime,eventTime,MotionEvent.ACTION_DOWN,fromX,fromY,0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        inst.sendPointerSync(event);


        for(int i=0;i<stepCount;i++){
            x+=xStep;
            y+=yStep;
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(downTime,eventTime,MotionEvent.ACTION_MOVE,x,y,0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            inst.sendPointerSync(event);
        }

        eventTime = SystemClock.uptimeMillis()+2;
        event = MotionEvent.obtain(downTime,eventTime,MotionEvent.ACTION_UP,toX,toY,0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        inst.sendPointerSync(event);
    }
}
