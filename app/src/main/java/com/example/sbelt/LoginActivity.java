package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
    private LoginEngine loginEngine;

    private void init(){
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        forgotPassViewFlipper = (ViewFlipper) findViewById(R.id.forgotPassViewFlipper);
        forgotPasswordEmail = (EditText) findViewById(R.id.forgotPassEmail);
        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginErrorLabel = (TextView) findViewById(R.id.loginErrorLabel);
        registerName = (EditText) findViewById(R.id.registerName);
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerPasswordConfirm = (EditText) findViewById(R.id.registerPasswordConfirm);
        registerErrorLabel = (TextView) findViewById(R.id.registerErrorLabel);

        //Checking if user is already logged in.
        loginEngine = new LoginEngine();
        if(loginEngine.isLoggedIn()){
            switchToMainActivity(loginEngine.getUser());
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
                    CompletableFuture<String> nameFuture = loginEngine.getName(firebaseUser);
                    nameFuture.whenComplete(new BiConsumer<String, Throwable>() {
                        @Override
                        public void accept(String name, Throwable throwable) {
                            if (name != null) {
                                System.out.println("User name: " + name);
                                switchToMainActivity(firebaseUser);
                            }
                            else{
                                System.out.println("ERROR: "+throwable.getMessage());
                                throwable.printStackTrace();
                                loginErrorLabel.setText(throwable.getMessage());
                            }
                        }
                    });
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
                    switchToMainActivity(firebaseUser);
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

    public void sendEmailButtonPressed(View v){
        try{
            loginEngine.sendPasswordChangeEmail(forgotPasswordEmail.getText().toString());
            forgotPassViewFlipper.showNext();
        }catch(Exception ignored){}
    }

    public void switchToMainActivity(FirebaseUser user){
        //Need to fill a function to switch to the main activity, after adding a main activity.
    }

    public void switchLoginLayers(View v){
        viewFlipper.showNext();
    }
}
