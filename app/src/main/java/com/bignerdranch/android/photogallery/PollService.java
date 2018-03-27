package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Ivo Georgiev(IfChyy)
 * PollService class is a class wich extends IntentService to create a background
 * process for search querys to be loaded in the bacgkroudn and if a new
 * picture is found or available the user will recieve a notification
 */

public class PollService extends IntentService {

    private static final String TAG = "PollSerivce";
    private static int POLL_INTERVAL = 1000 * 60;

    //Constructor
    public PollService() {
        super(TAG);
    }

    //intent class to start this intente
    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    //pulls the current query and the last result Id from
    //Shared Prefferences
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        //gets the last stored query searched by the user
        String query = QueryPreferences.getStoredQuery(this);
        //gets the last stored pref id
        String lastResultId = QueryPreferences.getPrefLastResultId(this);
        List<GalleryItem> items;
        //if query is null then load recentPhotos
        if (query == null) {
            items = new FlickFetcher().fetchRecentPhotos();
        } else {
            //else populate with search results by the query
            items = new FlickFetcher().searchPhotos(query);
        }
        //if no images found return
        if (items.size() == 0) {
            return;
        }
        //else if the first item is the result we are lokking for
        //display a message in the log
        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.d(TAG, "Got an old result " + resultId);
        } else {
            Log.d(TAG, "Got a new result " + resultId);

            //CHAPTER 26
            //used to notify the user that a new result is ready

            //get the system resources so we can acces them here
            Resources resources = getResources();
            //create a new intent pointing to PhotoGalleryActivity
            Intent in = PhotoGalleryActivity.newIntent(this);
            //create a PendingIntent with our Intent so we can fire it any time
            PendingIntent pi = PendingIntent.getActivity(this, 0, in, 0);

            //create our notificaiton
            //setting our tick text, icon, title, text, pendingIntnet,to start our app if user presses
            //the notification, autocancel will also delete the notificaiton if pressed
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(R.drawable.bill_up_close)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            //instance of notificaiton manager
            //with id of our notificaiton 0
            //if we pass a second notifification with the same id
            // it will replace the first one
            //this is how we make a progressbar
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);
        }

        QueryPreferences.setLastResultId(this, resultId);

    }

    //check if network connections is up and running
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }


    //method to wake up the service every 60 seconds
    //using AlarmManager to wake up the process
    public static void setServiceAlarm(Context context, boolean isOn) {
        //create the intent we want to invoke every minute
        Intent i = PollService.newIntent(context);
        //pass the intent to our PendingIntent service
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        //init our alarm manager service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //if set to on ( true)
        //start the alarmmanager waiting exaclty 1 minute before calling our
        //pending intent to wake up our service
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }


    //uses PendingIntent.FLAG_NO_CREATE
    //to check if the alarm is on or not
    //flag used says that if the aleready exist, return null instead of creating it.
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, i, PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;
    }
}
