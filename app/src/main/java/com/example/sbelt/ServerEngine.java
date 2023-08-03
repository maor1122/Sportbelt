package com.example.sbelt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerEngine extends Service {

    private static final String TAG = "MyUdpServerService";
    private static final int BROADCAST_PORT = 5555;
    private static final int DELAY = 1000;
    private static boolean isRunning = false;
    private static boolean ready = false;
    private DatagramSocket socket;

    public static boolean isRunning(){
        return isRunning;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startUdpServer();
        }
        return START_STICKY;
    }

    private void startUdpServer() {
        isRunning = true;
        ready = false;



        Thread thread = new Thread(() -> {
            try {

                byte[] buffer = new byte[255];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket = new DatagramSocket(BROADCAST_PORT);
                socket.setBroadcast(true);

                long time = System.currentTimeMillis();
                while (isRunning && !ready){
                    socket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    String data = "Received data: " + receivedData;
                    System.out.println("Received udp message: " + data + " from: " + packet.getAddress() + " port: " + packet.getPort());
                    if (receivedData.equals("R")) ready=true;
                }

                System.out.println(TAG+ "UDP server started. Listening for broadcast packets...");
                while (isRunning && ready) {
                    socket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    if(receivedData.length()<=2) continue;
                    if(time+DELAY >System.currentTimeMillis()) continue;
                    time = System.currentTimeMillis();

                    // TODO: Process the received data (variable and its value) here

                    String data = "Received data: " + receivedData;
                    System.out.println("Received udp message: "+data);
                }

            } catch (IOException e) {
                System.out.println("Something went wrong: ");
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        });
        thread.start();
        new Thread(() -> {
            long time = System.currentTimeMillis();
            while(!ready && isRunning)
                if(System.currentTimeMillis() - time > 5000) {
                    System.out.println("UDP timed out");
                    thread.interrupt();
                    socket.close();
                    return;
                }
        }).start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        ready = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
