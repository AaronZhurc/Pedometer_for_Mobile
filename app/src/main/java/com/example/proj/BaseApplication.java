package com.example.proj;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;

public class BaseApplication extends Application {
    private static LinkedList<Activity> activityList= new LinkedList<>();
    public static void remeberActivity(Activity activity) {
        activityList.add(activity);
    }
    public static void clearActivityList() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }
}
