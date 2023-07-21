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

    private boolean isRunning = false;
    private DatagramSocket socket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startUdpServer();
        }
        return START_STICKY;
    }

    private void startUdpServer() {
        isRunning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket(BROADCAST_PORT);
                    socket.setBroadcast(true);

                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    System.out.println(TAG+ "UDP server started. Listening for broadcast packets...");

                    while (isRunning) {
                        socket.receive(packet);
                        String receivedData = new String(packet.getData(), 0, packet.getLength());

                        System.out.println(TAG + "Received UDP packet: " + receivedData);

                        // TODO: Process the received data (variable and its value) here

                        // You can also send a response back to the client if needed
                        String responseMessage = "Received data: " + receivedData;
                        System.out.println("Received udp message: "+responseMessage);
                        //byte[] responseData = responseMessage.getBytes();
                        //DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
                        //socket.send(responsePacket);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
