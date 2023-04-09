package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LoginActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private ViewFlipper forgotPassViewFlipper;
    private EditText loginEmail;
    private EditText loginPassword;
    private TextView loginErrorLabel;
    private EditText registerName;
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerPasswordConfirm;
    private TextView registerErrorLabel;
    private EditText forgotPasswordEmail;
    private TextView forgotPassErrorLabel;
    private LoginEngine loginEngine;

    private void init(){
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        forgotPassViewFlipper = (ViewFlipper) findViewById(R.id.forgotPassViewFlipper);
        forgotPasswordEmail = (EditText) findViewById(R.id.forgotPassEmail);
        forgotPassErrorLabel = (TextView) findViewById(R.id.forgotPassErrorLabel);
        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginErrorLabel = (TextView) findViewById(R.id.forgotPassErrorLabel);
        registerName = (EditText) findViewById(R.id.registerName);
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerPasswordConfirm = (EditText) findViewById(R.id.registerPasswordConfirm);
        registerErrorLabel = (TextView) findViewById(R.id.registerErrorLabel);

        //Checking if user is already logged in.
        loginEngine = new LoginEngine();
        if(loginEngine.isLoggedIn()){
            switchToMainActivity();
            try{
                loginEngine.logout();
            }catch(Exception e){
                System.out.println("error: "+e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    public void loginPressed(View v){
        CompletableFuture<FirebaseUser> future = loginEngine.login(loginEmail.getText().toString(),loginPassword.getText().toString());
        future.whenComplete(new BiConsumer<FirebaseUser, Throwable>() {
            @Override
            public void accept(FirebaseUser firebaseUser, Throwable throwable) {
                if (firebaseUser != null) {
                    System.out.println("Logged in!, user uid: " + firebaseUser.getUid());
                    switchToMainActivity();
                } else {
                    loginErrorLabel.setText(throwable.getMessage());
                    //should be replaced with logs:
                    System.out.println("Something went wrong:");
                    throwable.printStackTrace();
                }
            }
        });
    }

    public void registerPressed(View v){
        CompletableFuture<FirebaseUser> future = loginEngine.register(registerName.getText().toString(),registerEmail.getText().toString(),
                registerPassword.getText().toString(),registerPasswordConfirm.getText().toString());
        future.whenComplete(new BiConsumer<FirebaseUser, Throwable>() {
            @Override
            public void accept(FirebaseUser firebaseUser, Throwable throwable) {
                if (firebaseUser != null) {
                    System.out.println("Registered!, user uid: " + firebaseUser.getUid());
                    switchToMainActivity();
                } else {
                    registerErrorLabel.setText(throwable.getMessage());
                    //should be replaced with logs:
                    System.out.println("Something went wrong:");
                    throwable.printStackTrace();
                }
            }
        });
    }

    public void exitForgotPassword(View v){
        forgotPassViewFlipper.setDisplayedChild(0);
    }

    public void forgotPasswordPressed(View v){
        forgotPassViewFlipper.showNext();
    }

    @SuppressLint("SetTextI18n")
    public void sendEmailButtonPressed(View v){
        if(forgotPasswordEmail.getText().length()==0){
            forgotPassErrorLabel.setText("Please insert email address");
            return;
        }
        CompletableFuture<Boolean> future = loginEngine.sendPasswordChangeEmail(forgotPasswordEmail.getText().toString());
        future.whenComplete(new BiConsumer<Boolean, Throwable>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void accept(Boolean done, Throwable throwable) {
                if(done!=null){
                    forgotPassErrorLabel.setText("Error: "+throwable.getMessage());
                }else{
                    forgotPassErrorLabel.setText("Email sent.");
                }
            }
        });
    }

    public void switchToMainActivity(){
        Intent switchToMainActivityIntent = new Intent(this,MainActivity.class);
        startActivity(switchToMainActivityIntent);
    }

    public void switchLoginLayers(View v){
        viewFlipper.showNext();
    }
}
