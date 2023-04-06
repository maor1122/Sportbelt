package com.example.sbelt;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private LoginEngine loginEngine;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = (TextView) findViewById(R.id.welcomeText);
        loginEngine = new LoginEngine();
        try {
            welcomeText.setText("Hello, " + loginEngine.getName());
        }catch(Exception e){finish();}


    }
    public void logout(){
        try {
            loginEngine.logout();
        }catch(Exception ignored){}
        finish();
    }
}