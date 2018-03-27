package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.lang.reflect.AccessibleObject;

/**
 * Created by Ivo Georgiev (IfChyy)
 * Notificaiton receiver class is going to be our standaalone receiver
 * to run after our dynamicaly registered receiver
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received result " + getResultCode());

        //HERE WE GET OUR RESULT CODE FROM OUR OrderedBroadcast from PollTestService intent(action)
        // meaning we send a broadcast intnet with our result code ok
        //meaning that our app is in background and we can show our notification after polling
        if(getResultCode() != Activity.RESULT_OK){
            //meaning that a foreground activity cancled the broadcast
            return;
        }
        //get our request code
        int requestCode = intent.getIntExtra(PollTestService.REQUEST_CODE, 0);
        //get our notification code
        Notification notification = (Notification) intent.getParcelableExtra(PollTestService.NOTIFICATION);
        //create our notification manager which si going to show our notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        //show the notification using the request code and our notifciation
        notificationManager.notify(requestCode,notification);
    }
}
