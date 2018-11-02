package com.bignerdranch.android.photogallery.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bignerdranch.android.photogallery.PhotoGalleryActivity;
import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.dataclass.GalleryItem;
import com.bignerdranch.android.photogallery.utilities.FlickFetcher;
import com.bignerdranch.android.photogallery.utilities.QueryPreferences;

import java.util.List;

/**
 * Created by Ivo Georgiev(IfChyy)
 * PollService extends IntentService to create a service which is going to periodically
 * start up a service responsible for making a notification if there are new photos in Flickr
 * <p>
 * Using Alarm Manager for api's < 19 we set a interval of 1 minute
 * then creating a async task in a background thread to look for new photos
 * if there are new photos send a notification to the user
 */

public class PollService extends IntentService {

    private static final String TAG = "PollSerivce";
    private static final String CHANNEL_ID = "PollSerivce";
    private static int POLL_INTERVAL = 1000 * 60; // 5 seconds

    //Constructor
    public PollService() {
        super(TAG);
    }

    //intent class to start this intent
    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    //method to run our Flicker Fetch class which looks for a new photo
    //and creates a push notification
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //check if network is available and connected
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        //method to create a notification channel for api > 27
        createNotificationChannel();

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

        //get the first item id
        String resultId = items.get(0).getId();
        //if it is equal to our old or new result log message
        if (resultId.equals(lastResultId)) {
            Log.d(TAG, "Got an old result " + resultId);
        } else {
            Log.d(TAG, "Got a new result " + resultId);


            //CHAPTER 26
            //create a notification that a new photo has been uploaded

            //init resources
            Resources resources = getResources();
            //create a new intent pointing to PhotoGalleryActivity
            Intent in = PhotoGalleryActivity.newIntent(this);
            //create a PendingIntent for our notification to start our app from it
            //get activity - a pending intent that is going to start a activity
            PendingIntent pi = PendingIntent.getActivity(this, 0, in, 0);

            //create our notification ( api < 19)
            //setting our tick text, icon, title, text, pendingIntnet,to start our app if user presses
            //the notification, auto cancel will also delete the notification if pressed
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(R.drawable.bill_up_close)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

            //api > 19 requires notification channel
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.bill_up_close)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pi)
                    .setAutoCancel(true);


            //init notification manager with id of notification and the notification
            //if id is the same it will replace the previous one ( used for progressbar)
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);
            notificationManager.notify(15, mBuilder.build());
        }

        //save our last flicker image id to our shared prefs
        QueryPreferences.setLastResultId(this, resultId);

    }

    //check if network connections is up and running
    private boolean isNetworkAvailableAndConnected() {
        //init connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //if manager network info not null - available network
        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        //check if available and connected
        boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo().isConnected();

        //log data nad return if connected
        Log.d(TAG, "isNetworkAvailableAndConnected: " + isNetworkAvailable + "  " + isNetworkConnected);
        return isNetworkConnected;
    }


    //method to check if service alarm is on or off
    //creating a pending intent with our Class intent
    //if you call the same pending intent twice you will get the same pending intent
    //this way if we get the same intent (alarm is on)
    //if we get different response alarm is of
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        //a pending intent which is going to start a service
        //in this case check if a pending intent is already running
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, i, PendingIntent.FLAG_NO_CREATE);

        //also available is getBoradcast - which is going to perform a broadcast (sendBroadcast)
        //also available is getForegroundService - which is going to start a foreground service

        return pendingIntent != null;
    }

    //method to start our alarmManager to fire a action in a specific interval
    public static void setServiceAlarm(Context context, boolean isOn) {
        //create the intent we want to invoke at that specific time
        Intent i = PollService.newIntent(context);
        //pass the intent to our PendingIntent service ( context, request code to distinguish between intents, the intent(service we want to run), and flags
        //it is like calling (context.startService()
        //this way we are going to start our intent(poll service class) if alarm manager goes of
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);

        //init our alarm manager service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //if set to on ( true)
        //start the alarm manager waiting exactly 1 minute before calling our
        //pending intent to wake up our service
        if (isOn) {
            //Elapsed_REALTIME - wont fire the alarm if device is in sleep mode
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);


        } else {
            //if boolean is false stop the service and clear our pending intent
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }


    //android 8.0 and higher wee needed to register our chanel with our app
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "This is the default notificaiton channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
