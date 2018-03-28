package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Ivo Georgiev(IfChyy)
 * StartupReceiver class is going to be a Standalone receiver which is going to listen
 * for a broadcast by the system after a boot-up sequence is complete
 * and start or polling if it was on before the device shutting down
 *
 * this standalone receiver is always live and listening for broadcasts
 */

public class StartupReceiver extends BroadcastReceiver{
    private static final String TAG = "StartupReceiver";


    //when a intent is issued to StartupReceiver onReceive is going to be called
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        //check if our polling was on when our phone turns on
        boolean isOn = QueryPreferences.isAlarmOn(context);
        //if on start polling like before the phone shutting down
        PollTestService.setServiceSchedule(context, isOn);
    }
}
