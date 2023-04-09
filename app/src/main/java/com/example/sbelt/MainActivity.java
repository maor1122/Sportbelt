package com.example.sbelt;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

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
            mainToolbar.setTitle("Hello, " + loginEngine.getName());
        }catch(Exception e){finish();}
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutButton) {
            logout(findViewById(R.id.logoutButton));
            return true;
        }
        if (id == R.id.howToUse) {
            showHowToUse(findViewById(R.id.howToUse));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.overflow_menu);
        popupMenu.show();
    }


    public void logout(View view){
        try {
            loginEngine.logout();
        }catch(Exception ignored){}
        finish();
    }

    public void showHowToUse(View view){
        //Needs to be filled
    }
}