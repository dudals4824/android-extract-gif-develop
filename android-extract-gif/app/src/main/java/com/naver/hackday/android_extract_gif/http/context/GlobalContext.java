package com.naver.hackday.android_extract_gif.http.context;

import android.app.Application;
import android.content.Context;

public class GlobalContext extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        GlobalContext.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GlobalContext.context;
    }
}
