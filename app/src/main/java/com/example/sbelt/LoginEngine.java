package com.example.sbelt;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    public void logout() throws Exception{
        mAuth.signOut();
    }

    public CompletableFuture<FirebaseUser> login(String email, String password){
        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            future.complete(user);
                        } else {
                            FirebaseException e = (FirebaseException) task.getException();
                            future.completeExceptionally(e);
                        }
                    }
                });
        return future;
    }

    public CompletableFuture<FirebaseUser> register(String name, String email, String password, String passwordConfirm){
        CompletableFuture<FirebaseUser> future = new CompletableFuture<>();
        if(!password.equals(passwordConfirm)) {future.completeExceptionally(new Exception("Passwords don't match")); return future;};
        if(password.length()<=6) {future.completeExceptionally(new Exception("Passwords has to be longer then 6")); return future;};

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference databaseEmailReference = FirebaseDatabase.getInstance().getReference();
                    databaseEmailReference.child("users").child(user.getUid()).setValue(name);
                    future.complete(user);
                } else {
                    //System.out.println("Exception in register function: ");
                    //task.getException().printStackTrace();
                    FirebaseException e = (FirebaseException) task.getException();
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }
}

