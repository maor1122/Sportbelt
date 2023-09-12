package com.example.sbelt.utils;

import static com.example.sbelt.utils.utils.PREFS_NAME;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    public static CompletableFuture<Boolean> tryToSaveDataOnline(String uid, DatabaseReference userRef, Context context) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        new Thread(() -> {
            List<GestureData> newData = getLocalGestureDataList(uid,context);
            System.out.println("userRef = "+userRef);
            if (newData == null) {
                future.completeExceptionally(new Exception("No data to save."));
                return;
            }
            System.out.println("Saving data of size: " + newData.size());
            try {
                DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        if (!connected)
                            future.completeExceptionally(new Exception("Error - check internet connection"));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });
                if(future.isCompletedExceptionally())
                    return;
                userRef.child("gestures").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<GestureData> existingData = new ArrayList<>();
                        for (DataSnapshot gestureSnapshot : snapshot.getChildren()) {
                            GestureData gestureData = gestureSnapshot.getValue(GestureData.class);
                            if (gestureData != null) {
                                existingData.add(gestureData);
                            }
                        }
                        existingData.addAll(newData);
                        removeLocalGestureDataList(uid,context); //Delete all saved data.
                        userRef.child("gestures").setValue(existingData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }

                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }).start();
        return future;
    }

    public static CompletableFuture<List<GestureData>> getGestureDataOnline(DatabaseReference userRef) {
        CompletableFuture<List<GestureData>> future = new CompletableFuture<>();
        try {
            userRef.child("gestures").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<GestureData> existingData = new ArrayList<>();
                    for (DataSnapshot gestureSnapshot : snapshot.getChildren()) {
                        GestureData gestureData = gestureSnapshot.getValue(GestureData.class);
                        if (gestureData != null) {
                            existingData.add(gestureData);
                        }
                    }
                    future.complete(existingData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    future.completeExceptionally(error.toException());
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public static List<GestureData> getLocalGestureDataList(String uid, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String gestureDataListJson = preferences.getString(uid, "");
        if(gestureDataListJson.equals(""))
            return null;
        return gson.fromJson(gestureDataListJson, new TypeToken<List<GestureData>>(){}.getType());
    }
    public static void removeLocalGestureDataList(String uid,Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Remove the data associated with the user's Uid
        editor.remove(uid);
        editor.apply();
    }
    public static void saveUserDataLocally(List<GestureData> gestureDataList,String Uid,Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String gestureDataListJson = gson.toJson(gestureDataList);
        editor.putString(Uid,gestureDataListJson);
        editor.apply();
    }
}
