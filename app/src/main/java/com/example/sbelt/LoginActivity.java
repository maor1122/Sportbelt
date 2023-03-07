package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void LoginPressed(View v){
        try {
            EditText username = (EditText) findViewById(R.id.editTextEmail);
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
}