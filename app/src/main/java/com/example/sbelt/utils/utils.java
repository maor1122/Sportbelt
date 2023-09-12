package com.example.sbelt.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class utils {

    public static final String PREFS_NAME = "GesturesData";
    public static final int BROADCAST_PORT = 5555;
    public static final int DELAY = 100;
    public static final LinkedList<String> WEEKDAYS = new LinkedList<>(Arrays.asList(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
    ));

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }
        return false;
    }
    public static String getOpeningMessage(String name){
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        if(hour>=6 && hour<12)
            return "Good Morning,\n"+name;
        else if (hour>=12 && hour<18)
            return "Good Afternoon,\n"+name;
        else if(hour>=18 && hour<21)
            return "Good Evening,\n"+name;
        else
            return "Good Night,\n"+name;
    }

}
