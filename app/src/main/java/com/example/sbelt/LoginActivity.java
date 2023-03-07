package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LoginActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
    }

    public void loginPressed(View v){
        try {
            EditText username = (EditText) findViewById(R.id.editTextUsername);
            EditText password = (EditText) findViewById(R.id.editTextPassword);

            // After making the engine should be replaced to:
            // Engine.login(username.getText(),password.getText());
            System.out.println("Username: "+username.getText());
            System.out.println("Password: "+password.getText());


        }catch(Exception e){
            //TextView errorLabel = (TextView) findViewById(R.id.Error);
            //errorLabel.setText(String.format("Error: \"%s\"", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void switchLayers(View v){
        viewFlipper.showNext();
    }
}