package com.abhisek.mapprr_github;

import android.app.Application;
import android.content.Context;


/**
 * Created by bapu on 3/23/2017.
 */

public class MyApplication extends Application {

    private static MyApplication mInstance;


    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
       // MultiDex.install(this);
    }


    public static synchronized MyApplication getInstance() {

        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener= listener;
    }
}
