package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ViewFlipper;

public class LoginActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private EditText loginUsername;
    private EditText loginPassword;
    private EditText registerUsername;
    private EditText registerName;
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerPasswordConfirm;

    private void init(){
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        loginUsername = (EditText) findViewById(R.id.loginUsername);
        loginPassword = (EditText) findViewById(R.id.loginPassword);

        registerUsername = (EditText) findViewById(R.id.registerUsername);
        registerName = (EditText) findViewById(R.id.registerName);
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerPasswordConfirm = (EditText) findViewById(R.id.registerPasswordConfirm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    public void loginPressed(View v){
        try {
            // After making the engine should be replaced to:
            // Engine.login(loginUsername.getText(),loginPassword.getText());
            System.out.println("Username: "+loginUsername.getText());
            System.out.println("Password: "+loginPassword.getText());
        }catch(Exception e){
            //Need to add an error text label.
            //TextView errorLabel = (TextView) findViewById(R.id.errorLabel);
            //loginErrorLabel.setText(String.format(e.getMessage()));
            e.printStackTrace();
        }
    }

    public void registerPressed(View v){
        try {
            // After making the engine should be replaced to:
            // Engine.register(registerUsername.getText(),registerName.getText(),registerEmail.getText(),registerPassword.getText(),registerPasswordConfirm.getText());
            System.out.println("Username: "+registerUsername.getText());
            System.out.println("Name: "+registerName.getText());
            System.out.println("Email: "+registerEmail.getText());
            System.out.println("Password: "+registerPassword.getText());
            System.out.println("Password (Confirm): "+registerPasswordConfirm.getText());
        }catch(Exception e){
            //Need to add an error text label.
            //TextView errorLabel = (TextView) findViewById(R.id.errorLabel);
            //registerErrorLabel.setText(String.format(e.getMessage()));
            e.printStackTrace();
        }
    }

    public void switchLayers(View v){
        viewFlipper.showNext();
    }
}