package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Ivo Georgiev(IfChyy)
 * TO DO
 */

@SuppressLint("NewApi")
public class PollTestService extends JobService {
    private static final int JOB_ID = 5;
    private static final long TIME_INTERVAL = 1000 * 5;
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERMITION_RPIVATE = "com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    private PollTask pollTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("thos", "onStartJob: PollTestService");
        pollTask = new PollTask();
        pollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("thos", "onStopJob: PollTestService");

        pollTask.cancel(true);
        Log.d("thos", "polltask canceled");

        return true;
    }

    public static void setServiceSchedule(Context context, boolean isOn) {

        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

        if (isOn) {

            JobInfo info;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                info = new JobInfo.Builder(JOB_ID,
                        new ComponentName(context, PollTestService.class))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                        .setMinimumLatency(TIME_INTERVAL)
                        .setPersisted(true)
                        .build();
            } else {
                info = new JobInfo.Builder(JOB_ID,
                        new ComponentName(context, PollTestService.class))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_METERED)
                        .setPeriodic(TIME_INTERVAL)
                        .setPersisted(true)
                        .build();
            }

            scheduler.schedule(info);
        } else {
            scheduler.cancel(JOB_ID);
        }

        QueryPreferences.setAlarmOn(context, isOn);

    }

    public static boolean isServiceScheduleOn(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;

            }
        }
        return hasBeenScheduled;
    }


    private class PollTask extends AsyncTask<JobParameters, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(JobParameters... params) {
            if (!isNetworkAvailableAndConnected()) {
                return null;
            }

            JobParameters jobParameters = params[0];

            //gets the last stored query searched by the user
            String query = QueryPreferences.getStoredQuery(getApplicationContext());
            List<GalleryItem> items;
            //if query is null then load recentPhotos
            if (query == null) {
                items = new FlickFetcher().fetchRecentPhotos();
            } else {
                //else populate with search results by the query
                items = new FlickFetcher().searchPhotos(query);
            }
            jobFinished(jobParameters, true);
            return items;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            Log.d("thos", "onPostExecute: ");

            //gets the last stored pref id
            String lastResultId = QueryPreferences.getPrefLastResultId(getApplicationContext());
            //if no images found return
            if (galleryItems.size() == 0) {
                return;
            }
            //else if the first item is the result we are lokking for
            //display a message in the log
            String resultId = galleryItems.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.d("thos", "Got an old result " + resultId);
            } else {
                Log.d("thos", "Got a new result " + resultId);


                QueryPreferences.setLastResultId(getApplicationContext(), resultId);


                //get the system resources so we can acces them here
                Resources resources = getResources();
                //create a new intent pointing to PhotoGalleryActivity
                Intent in = PhotoGalleryActivity.newIntent(getApplicationContext());
                //create a PendingIntent with our Intent so we can fire it any time
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, in, 0);

                //create our notificaiton
                //setting our tick text, icon, title, text, pendingIntnet,to start our app if user presses
                //the notification, autocancel will also delete the notificaiton if pressed
                Notification notification = new NotificationCompat.Builder(getApplicationContext())
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(R.drawable.bill_up_close)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                //chapter 27, code excluded for use of broadcast receivers
                //notificaiton will only be shown if app is in background or closed
                //if in foreground or user can see it no notification would be displayed

              /*  NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(0, notification);

                //send a broadcast intent using intent and a value
                //it is send everytime a new search is available
                //set its permision to be private for the app only
                //any app must use the same permission to receive our broadcast intent
                sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERMITION_RPIVATE);*/


                showBackgroundNotification(0, notification);
            }
        }


        //check if network connections is up and running
        private boolean isNetworkAvailableAndConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
            boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo().isConnected();

            return isNetworkConnected;
        }

        private void showBackgroundNotification(int request_Code, Notification notification){
            //create a new intent and set its action - eg string show notificaiton to pass
            Intent in = new Intent(ACTION_SHOW_NOTIFICATION);
            //put our extra request code (String value)
            in.putExtra(REQUEST_CODE, request_Code);
            //and extra notification (String value)
            in.putExtra(NOTIFICATION, notification);
            ////usually resultrReceiver is used to receive the broadcast and post the notificaiton
            //here this is not possible because pollTestService could be long dead, meaning the receiver also will be dead
            //so our receiver will be a standalone receiver, and we are going to enfroce it to run
            //afet rhe dynamicaly registered receiver
            //HERE WE SEND OUR INTENT(ACTION FOR NOTIFICIATON) as orderedBroadcast, meaning it has an order
            //and it is broadcastet with PERMISSION_PRIVATE - within our app only
            //and we are seding out the result code _ ACTIVITY_RESULT_OK
            //meaning we are sending an broadcast intent
            sendOrderedBroadcast(in, PERMITION_RPIVATE, null, null, Activity.RESULT_OK, null, null);
        }

    }
}