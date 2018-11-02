package com.bignerdranch.android.photogallery.receivers;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bignerdranch.android.photogallery.services.PollTestService;

/**
 * Created by Ivo Georgiev (IfChyy)
 * Notification receiver class is going to be our standalone receiver
 * to run after our dynamically registered receiver
 *
 * if broadcast with notification in it is registered
 * this on receive is going to fire
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received result " + getResultCode());

        //HERE WE GET OUR RESULT CODE FROM OUR OrderedBroadcast from PollTestService intent(action)
        // meaning we send a broadcast intent with our result code ok
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
