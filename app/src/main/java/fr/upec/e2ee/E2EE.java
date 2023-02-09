package fr.upec.e2ee;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class E2EE extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        E2EE.context = getApplicationContext();
    }
}
