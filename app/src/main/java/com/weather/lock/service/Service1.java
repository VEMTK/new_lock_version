package com.weather.lock.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.weather.lock.service.A;


/**
 * This Service is Persistent Service. Do some what you want to do here.<br/>
 *
 * Created by Mars on 12/24/15.
 */
public class Service1 extends Service{

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO do some thing what you want..

        startService(new Intent(this,A.class));

        Intent intent= new Intent(this, com.xml.library.services.A.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startService(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
