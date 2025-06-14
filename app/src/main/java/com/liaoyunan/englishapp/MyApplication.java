package com.liaoyunan.englishapp;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

/**
 * Created by Quhaofeng on 16-4-29.
 */
public class MyApplication extends MultiDexApplication {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Context getContext() {
        return sContext;
    }
}
