package com.example.sbelt;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sbelt.utils.Slide;
import com.example.sbelt.utils.SliderAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class HowToUseActivity extends AppCompatActivity {
    private ViewPager2 sliderViewPager;
    private LinearLayout indicatorsContainer;
    private Button nextButton;
    private SliderAdapter sliderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);
        sliderViewPager = findViewById(R.id.sliderViewPager);
        indicatorsContainer = findViewById(R.id.indicatorsContainer);
        nextButton = findViewById(R.id.nextButton);
        sliderAdapter = new SliderAdapter(
                new ArrayList<>(Arrays.asList(
                        new Slide("Press \"Start\" button", "", R.drawable.press_start),
                        new Slide("Press \"Installed services\"", "", R.drawable.press_installed_services),
                        new Slide("Press \"Sportbelt service\"", "", R.drawable.press_sportbelt_service),
                        new Slide("Turn the service on", "", R.drawable.turn_the_service_on),
                        new Slide("Open the application of your choice", "You can use any application, although sportbelt is best used on swiping games", R.drawable.open_game),
                        new Slide("Move To Trigger Swipes","Jump to trigger an upper swipe, crouch to trigger down swipe, and move left or right to trigger left/right swipes correspondingly",R.drawable.move_to_swipe),
                        new Slide("Saving Data", "After finishing using the service, you can save the data by pressing the \"Save Data\" button.", R.drawable.save_data),
                        new Slide("Browsing The Data", "Later, you can browse the data of previous usage by pressing the \"Data\" button", R.drawable.browse_data)
                ))
        );

        sliderViewPager.setAdapter(sliderAdapter);
        setupIndicators();
        setCurrentIndicator(0);
        sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });
    }
    private void setupIndicators() {
        ImageView[] indicators = new ImageView[sliderAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,0,8,0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            indicatorsContainer.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = indicatorsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorsContainer.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_inactive));
            }
        }
    }

    public void exit(View view){
        finish();
    }
    public void nextImage(View view){
        if (sliderViewPager.getCurrentItem() + 1 < sliderAdapter.getItemCount()) {
            sliderViewPager.setCurrentItem(sliderViewPager.getCurrentItem() + 1);
        } else {
            finish();
        }
    }
}
