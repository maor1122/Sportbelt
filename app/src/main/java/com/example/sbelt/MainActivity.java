package com.example.sbelt;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

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

    public void startSportbelt(View view){
        //Needs to be filled
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