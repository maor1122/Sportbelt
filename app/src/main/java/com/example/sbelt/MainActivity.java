package com.example.sbelt;


import static com.example.sbelt.utils.utils.PREFS_NAME;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sbelt.utils.LoadingData;
import com.example.sbelt.utils.LoadingWIFI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MainActivity extends AppCompatActivity{

    private LoginEngine loginEngine;
    public static int BROADCAST_PORT = 5555;
    public static final int DELAY = 1000;
    private static boolean isRunning = false;
    private static boolean ready = false;
    private static boolean loading = false;
    private TextView title;
    public static DatagramSocket socket;
    private LoadingWIFI loadingWIFI;
    private LoadingData loadingData;

    public void startWIFILoadingAnimation(){
        loadingWIFI.show();
    }
    public void cancelWIFILoadingAnimation(){
        try {
            loadingWIFI.cancel();
        }catch (Exception ignored){}
    }

    public void startDataLoadingAnimation(){
        loadingData.show();
    }
    public void cancelDataLoadingAnimation(){
        try {
            loadingData.cancel();
        }catch (Exception ignored){}
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginEngine = new LoginEngine();
        loadingWIFI = new LoadingWIFI(this);
        loadingData = new LoadingData(this);
        title = (TextView) findViewById(R.id.menuTitle);
        try {
            String message = getOpeningMessage();
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
        //TODO: Fill function.
    }

    public void saveData(View view){
        if(loading)
            return;
        loading = true;
        startDataLoadingAnimation();
        CompletableFuture<Boolean> future = tryToSaveData();
        future.whenComplete((aBoolean, throwable) -> {
            cancelDataLoadingAnimation();
            if(aBoolean==null) {
                runOnUiThread(() -> Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show());
                throwable.printStackTrace();
            }
            loading = false;
        });
    }
    public CompletableFuture<Boolean> tryToSaveData(){
        String uid = loginEngine.getUser().getUid();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            List<GestureData> newData = getGestureDataList(uid);
            if(newData==null){
                future.completeExceptionally(new Exception("No data to save."));
                return;
            }
            System.out.println("Saving data of size: "+newData.size());
            try{
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                userRef.child("gestures").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<GestureData> existingData = new ArrayList<>();
                        for (DataSnapshot gestureSnapshot : snapshot.getChildren()) {
                            GestureData gestureData = gestureSnapshot.getValue(GestureData.class);
                            if (gestureData != null) {
                                existingData.add(gestureData);
                            }
                        }
                        existingData.addAll(newData);
                        removeGestureDataList(uid); //Delete all saved data.
                        userRef.child("gestures").setValue(existingData);
                        future.complete(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }catch (Exception e){
                future.completeExceptionally(e);
            }
        });
        thread.start();
        return future;
    }

    public List<GestureData> getGestureDataList(String uid) {
        SharedPreferences preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String gestureDataListJson = preferences.getString(uid, "");
        if(gestureDataListJson.equals(""))
            return null;
        return gson.fromJson(gestureDataListJson, new TypeToken<List<GestureData>>(){}.getType());
    }

    public void removeGestureDataList(String uid) {
        SharedPreferences preferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Remove the data associated with the user's Uid
        editor.remove(uid);
        editor.apply();
    }

    public void startSportbelt(View view) {
        if (loading) {
            return;
        }
        loading = true;
        isRunning = true;
        try {
            startWIFILoadingAnimation();
            if (!getPermissions()) {
                System.out.println("Error - permissions denied");
                cancelWIFILoadingAnimation();
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
                loading = false;
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            isRunning = false;
            ready = false;
            loading = false;
            cancelWIFILoadingAnimation();
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

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }
        return false;
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
        Intent switchToMainActivityIntent = new Intent(this,DataActivity.class);
        startActivity(switchToMainActivityIntent);
    }

    public String getOpeningMessage(){
        String name = loginEngine.getName();
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        if(hour>=6 && hour<12)
            return "Good Morning,\n"+name;
        else if (hour>=12 && hour<18)
            return "Good Afternoon,\n"+name;
        else if(hour>=18 && hour<21)
            return "Good Evening,\n"+name;
        else
            return "Good Night,\n"+name;
    }
}