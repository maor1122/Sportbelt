package com.example.sbelt;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CompletableFuture;

public class LoginEngine {
    private final FirebaseAuth mAuth;

    public LoginEngine(){
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isLoggedIn(){
        return mAuth.getCurrentUser()!=null;
    }
    public FirebaseUser getUser(){
        return mAuth.getCurrentUser();
    }

    public void logout(){
        mAuth.signOut();
    }

    public CompletableFuture<FirebaseUser> login(String email, String password){
        if(mAuth.getCurrentUser()!=null)
            logout();

        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        if(email.length()==0){future.completeExceptionally(new Exception("Please fill in Email")); return future;}
        if(password.length()==0){future.completeExceptionally(new Exception("Please fill in Name")); return future;}
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        future.complete(user);
                    } else {
                        Exception e = task.getException();
                        Exception newException = null;
                        if(e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException){
                            newException = new Exception("User doesn't exist or password doesn't match");
                        }
                        else if(e instanceof FirebaseNetworkException){
                            newException = new Exception("Please check your internet connection.");
                        }

                        if(newException==null)
                            future.completeExceptionally(e);
                        else
                            future.completeExceptionally(newException);
                    }
                });
        return future;
    }

    public CompletableFuture<FirebaseUser> register(String name, String email, String password, String passwordConfirm){
        if(mAuth.getCurrentUser()!=null)
            logout();
        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        if(!password.equals(passwordConfirm)) {future.completeExceptionally(new Exception("Passwords don't match")); return future;}
        if(password.length()<=6) {future.completeExceptionally(new Exception("Passwords has to be longer then 6")); return future;}
        if(name.length()==0){future.completeExceptionally(new Exception("Please fill in Name")); return future;}
        if(email.length()==0){future.completeExceptionally(new Exception("Please fill in Email")); return future;}

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
                        future.complete(user);
                    }else{
                        FirebaseException e = (FirebaseException) task1.getException();
                        user.delete().addOnCompleteListener(task11 -> {
                            if(!task11.isSuccessful()) System.out.println("FETAL ERROR: "+ task11.getException().getMessage());
                        });
                        future.completeExceptionally(e);
                    }
                });
            } else {
                FirebaseException e = (FirebaseException) task.getException();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Boolean> sendPasswordChangeEmail(String email){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(!task.isSuccessful()){
                future.completeExceptionally(task.getException());
            }else{
                future.complete(true);
            }
        });
        return future;
    }

    public String getName(){
        FirebaseUser user = mAuth.getCurrentUser();
        return user.getProviderData().get(0).getDisplayName();
    }
}

