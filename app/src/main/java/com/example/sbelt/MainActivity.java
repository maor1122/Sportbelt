package com.example.sbelt;


import android.Manifest;
import android.annotation.SuppressLint;
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

import java.util.Calendar;



public class MainActivity extends AppCompatActivity{

    private Toolbar mainToolbar;
    private LoginEngine loginEngine;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        loginEngine = new LoginEngine();
        try {
            String message = getOpeningMessage();
            mainToolbar.setTitle(message);
        }catch(Exception e){finish();}
    }

    public void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
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
        if (!getPremissions()) return;

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        try {
            String ssid = "Sportbelt", pass = "Dh821ADSSd";
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\"" + ssid + "\"";
            wifiConfiguration.preSharedKey = "\"" + pass + "\"";

            int netId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            System.out.println("Correct wifi is: " + wifiManager.getConnectionInfo().getSSID().toString());
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        try{
            Intent udpServerIntent = new Intent(this, ServerEngine.class);
            startService(udpServerIntent);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            startMainService();
        }catch (Exception e){
            //Print the message
        }
    }

    private void startMainService() throws Exception{

    }

    private Boolean getPremissions(){

        if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED))
           requestAccessWifiPermission();
        if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED))
            requestChangeWifiPermission();
        if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED))
            return false;
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestAccessWifiPermission(){
        System.out.println("Asking for first permission:");
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_WIFI_STATE},1);
    }
    private void requestChangeWifiPermission(){
        System.out.println("Asking for second permission:");
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CHANGE_WIFI_STATE},2);
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