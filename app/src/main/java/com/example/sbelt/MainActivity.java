package com.example.sbelt;


import static com.example.sbelt.utils.DataManager.getGestureDataOnline;
import static com.example.sbelt.utils.DataManager.tryToSaveDataOnline;
import static com.example.sbelt.utils.utils.BROADCAST_PORT;
import static com.example.sbelt.utils.utils.getOpeningMessage;
import static com.example.sbelt.utils.utils.isAccessibilityServiceEnabled;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sbelt.utils.GestureData;
import com.example.sbelt.utils.LoadingData;
import com.example.sbelt.utils.LoadingWIFI;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MainActivity extends AppCompatActivity{

    private LoginEngine loginEngine;
    private String uid;
    private static boolean isRunning = false;
    private static boolean ready = false;
    private static boolean loading = false;
    public static DatagramSocket socket;
    private LoadingWIFI loadingWIFI;
    private LoadingData loadingData;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginEngine = new LoginEngine();
        uid = loginEngine.getUser().getUid();
        loadingWIFI = new LoadingWIFI(this);
        loadingData = new LoadingData(this);
        TextView title = findViewById(R.id.menuTitle);
        try {
            String message = getOpeningMessage(loginEngine.getName());
            title.setText(message);
        }catch(Exception e){finish();}
    }


    public void logout(View view){
        try {
            loginEngine.logout();
        }catch(Exception ignored){}
        finish();
    }

    public void howToUse(View view){
        if (loading) {
            return;
        }
        loading = true;
        //TODO: Fill function.
        loading=false;
    }

    public void saveData(View view){
        if(loading)
            return;
        loading = true;
        loadingData.show();
        String uid = loginEngine.getUser().getUid();
        CompletableFuture<Boolean> future = tryToSaveDataOnline(uid,FirebaseDatabase.getInstance().getReference().child("users").child(uid),this);
        future.whenComplete((aBoolean, throwable) -> {
            loadingData.cancel();
            if(aBoolean==null) {
                runOnUiThread(() -> Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show());
                throwable.printStackTrace();
            }
            loading = false;
        });
    }

    public void startSportbelt(View view) {
        if (loading) {
            return;
        }
        loading = true;
        isRunning = true;
        try {
            loadingWIFI.show();
            if (!getPermissions()) {
                System.out.println("Error - permissions denied");
                loadingWIFI.cancel();
                return;
            }
            System.out.println("Passed permissions");
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
                loadingWIFI.cancel();
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
                loading = false;
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            isRunning = false;
            ready = false;
            loading = false;
            loadingWIFI.cancel();
        }
    }
    private CompletableFuture<Boolean> startUdpServer(){
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
            } catch (Exception e) {
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
        if (!isAccessibilityServiceEnabled(this, MyAccessibilityService.class)) {
            Intent serviceIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(serviceIntent);
        }
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

    public void switchToDataActivity(View view) {
        if (loading) {
            return;
        }
        loading = true;
        loadingData.show();
        Intent intent = new Intent(this, DataActivity.class);
        CompletableFuture<List<GestureData>> future = getGestureDataOnline(uid,FirebaseDatabase.getInstance().getReference().child("users").child(uid),this);
        future.whenComplete((lst, throwable) ->{
            loadingData.cancel();
            loading=false;
            if(lst!=null){
                intent.putParcelableArrayListExtra("Data", (ArrayList<? extends Parcelable>) lst);
                System.out.println("Saved data in the intent!");
                startActivity(intent);
            }
            else{
                System.out.println("something went wrong, ");
                throwable.printStackTrace();
                runOnUiThread(() ->Toast.makeText(this,throwable.getMessage(),Toast.LENGTH_LONG).show());
            }
        });
    }
}