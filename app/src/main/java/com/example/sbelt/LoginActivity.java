package com.example.sbelt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

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
    private CircularProgressButton loginButton;
    private CircularProgressButton registerButton;

    private void init(){
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        forgotPassViewFlipper = (ViewFlipper) findViewById(R.id.forgotPassViewFlipper);
        forgotPasswordEmail = (EditText) findViewById(R.id.forgotPassEmail);
        forgotPassErrorLabel = (TextView) findViewById(R.id.forgotPassErrorLabel);
        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginErrorLabel = (TextView) findViewById(R.id.forgotPassErrorLabel);
        loginButton = (CircularProgressButton) findViewById(R.id.cirLoginButton);
        registerName = (EditText) findViewById(R.id.registerName);
        registerEmail = (EditText) findViewById(R.id.registerEmail);
        registerPassword = (EditText) findViewById(R.id.registerPassword);
        registerPasswordConfirm = (EditText) findViewById(R.id.registerPasswordConfirm);
        registerErrorLabel = (TextView) findViewById(R.id.registerErrorLabel);
        registerButton = (CircularProgressButton) findViewById(R.id.cirRegisterButton);


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
        loginButton.startAnimation();
        CompletableFuture<FirebaseUser> future = loginEngine.login(loginEmail.getText().toString(),loginPassword.getText().toString());
        future.whenComplete(new BiConsumer<FirebaseUser, Throwable>() {
            @Override
            public void accept(FirebaseUser firebaseUser, Throwable throwable) {
                if (firebaseUser != null) {
                    loginButton.doneLoadingAnimation(0x00a5ff, defaultLoginDoneImage());
                    new Handler().postDelayed(() -> {
                        try {
                            loginButton.revertAnimation();
                            System.out.println("Logged in!, user uid: " + firebaseUser.getUid());
                            switchToMainActivity();
                        }catch (Exception e){
                            e.printStackTrace();
                            finish();}
                    },300);
                } else {
                    // this is the case we get an exception.
                    loginButton.doneLoadingAnimation(0x00a5ff, defaultLoginFailImage());
                    new Handler().postDelayed(() -> {
                        try {
                            System.out.println("Failed logging in.");
                            Thread.sleep(1000);
                            loginButton.revertAnimation();
                            loginErrorLabel.setText(throwable.getMessage());
                            //should be replaced with logs:
                            System.out.println("Something went wrong:");
                            throwable.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                            finish();}
                    },300);
                }
            }
        });
    }

    public void registerPressed(View v){
        registerButton.startAnimation();
        CompletableFuture<FirebaseUser> future = loginEngine.register(registerName.getText().toString(),registerEmail.getText().toString(),
                registerPassword.getText().toString(),registerPasswordConfirm.getText().toString());
        future.whenComplete(new BiConsumer<FirebaseUser, Throwable>() {
            @Override
            public void accept(FirebaseUser firebaseUser, Throwable throwable) {
                if (firebaseUser != null) {
                    registerButton.doneLoadingAnimation(0x00a5ff, defaultLoginDoneImage());
                    new Handler().postDelayed(() -> {
                        try {
                            registerButton.revertAnimation();
                            System.out.println("Registered!, user uid: " + firebaseUser.getUid());
                            switchToMainActivity();
                        }catch (Exception e){
                            e.printStackTrace();
                            finish();}
                    },300);
                } else {
                    // this is the case we get an exception.
                    registerButton.doneLoadingAnimation(0x00a5ff, defaultLoginFailImage());
                    new Handler().postDelayed(() -> {
                        try {
                            Thread.sleep(1000);
                            registerButton.revertAnimation();
                            registerErrorLabel.setText(throwable.getMessage());
                            //should be replaced with logs:
                            System.out.println("Something went wrong:");
                            throwable.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                            finish();}
                    },300);
                }
            }
        });
    }

    private Bitmap defaultLoginDoneImage(){
        return drawableToBitmap(getDrawable(R.drawable.baseline_check_24));
    }
    private Bitmap defaultLoginFailImage(){
        return drawableToBitmap(getDrawable(R.drawable.baseline_close_24));
    }


    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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
