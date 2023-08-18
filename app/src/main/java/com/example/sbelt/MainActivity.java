package com.example.sbelt;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;


public class MainActivity extends AppCompatActivity{

    private LoginEngine loginEngine;
    public static final int BROADCAST_PORT = 5555;
    public static final int DELAY = 1000;
    private static boolean isRunning = false;
    private static boolean ready = false;
    public static DatagramSocket socket;
    private LoadingWIFI loadingWIFI;

    public void startWIFILoadingAnimation() throws Exception{
        loadingWIFI.show();
    }
    public void cancelWIFILoadingAnimation(){
        try {
            loadingWIFI.cancel();
        }catch (Exception ignored){}
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        loginEngine = new LoginEngine();
        loadingWIFI = new LoadingWIFI(this);
        try {
            String message = getOpeningMessage();
            mainToolbar.setTitle(message);
        }catch(Exception e){finish();}
    }

    public void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.inflate(R.menu.overflow_menu);
        popupMenu.show();
    }


    public void logout(MenuItem item){
        try {
            loginEngine.logout();
        }catch(Exception ignored){}
        finish();
    }

    public void showHowToUse(MenuItem item){
        //Needs to be filled
    }



    public void startSportbelt(View view) {
        try {
            startWIFILoadingAnimation();
            if (isRunning) {
                System.out.println("Error - sportbelt already running");
                cancelWIFILoadingAnimation();
                return;
            }
            isRunning = true;
            if (!getPermissions()) {
                System.out.println("Error - permissions denied");
                cancelWIFILoadingAnimation();
                return;
            }
            System.out.println("Passed permissions");
            startWIFILoadingAnimation();
            ready = false;

            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            String ssid = "Sportbelt", pass = "Dh821ADSSd";
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            wifiConfiguration.preSharedKey = "\"" + pass + "\"";

            int netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            CompletableFuture<Boolean> future = startUdpServer();
            future.whenComplete((aBoolean, throwable) -> {
                cancelWIFILoadingAnimation();
                if(aBoolean==null){
                    isRunning = false;
                    ready = false;
                    Context context = this;
                    runOnUiThread(() -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show());
                    throwable.printStackTrace();
                }
                else{
                    try {
                        startMainService();
                    }catch (IOException e){
                        e.printStackTrace();
                        isRunning = false;
                        ready = false;
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            isRunning = false;
            ready = false;
            cancelWIFILoadingAnimation();
        }
    }
    private CompletableFuture<Boolean> startUdpServer() throws Exception{
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            try {
                byte[] buffer = new byte[255];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket = new DatagramSocket(BROADCAST_PORT);
                socket.setBroadcast(true);
                while (isRunning && !ready){
                    socket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    String data = "Received data: " + receivedData;
                    System.out.println("Received udp message: " + data + " from: " + packet.getAddress() + " port: " + packet.getPort());
                    if (receivedData.equals("R")) ready=true;
                }
            } catch (Exception ignored) {
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        });
        thread.start();
        new Thread(() -> {
            long time = System.currentTimeMillis();
            System.out.println("Time: "+time+". Searching for Sportbelt...");
            while(!ready && isRunning)
                if(System.currentTimeMillis() - time > 7000) {
                    System.out.println("UDP timed out");
                    thread.interrupt();
                    future.completeExceptionally(new Exception("Couldn't connect to Sportbelt, make sure Sportbelt wifi is available"));
                    socket.close();
                    return;
                }
            future.complete(true);
            socket.close();
        }).start();
        return future;
    }

    private void startMainService() throws IOException{
        Intent serviceIntent = new Intent(this,MainService.class);
        startForegroundService(serviceIntent);
    }

    private Boolean getPermissions(){
        if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED))
           requestAccessWifiPermission();
        if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            requestInternetPermission();
        return true;
    }
    private void requestAccessWifiPermission(){
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_WIFI_STATE},1);
    }
    private void requestInternetPermission(){
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.INTERNET},2);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                System.out.println("Permission granted(1)");
            System.out.println(grantResults.length);
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                System.out.println("Permission granted(2)");
            System.out.println(grantResults.length);
        }
    }

    public void switchToDataActivity(View view){
        //Needs to be filled
    }
    public String getOpeningMessage(){
        String name = loginEngine.getName();
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        if(hour>6 && hour<12)
            return "Good Morning, "+name;
        else if (hour>12 && hour<18)
            return "Good Afternoon, "+name;
        else if(hour>18 && hour<21)
            return "Good Evening, "+name;
        else
            return "Good Night, "+name;
    }
}