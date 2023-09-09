package com.example.sbelt.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.sbelt.R;

import java.util.Objects;

public class LoadingData extends Dialog {
    public LoadingData(@NonNull Context context) {
        super(context);

        WindowManager.LayoutParams params = Objects.requireNonNull(getWindow()).getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.loading_data_layout,null);
        setContentView(view);
    }
    @Override
    public void cancel(){
        try {
            super.cancel();
        }catch (Exception ignored){}
    }
}
