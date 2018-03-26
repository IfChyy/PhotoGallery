package com.bignerdranch.android.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Ivo Georgiev(IfChyy)
 * PollService class is a class wich extends IntentService to create a background
 * process for search querys to be loaded in the bacgkroudn and if a new
 * picture is found or available the user will recieve a notification
 */

public class PollService extends IntentService {

    private static final String TAG = "PollSerivce";

    //Constructor
    public PollService(){
        super(TAG);
    }
    //intent class to start this intente
    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Received an Intent: " + intent);
    }
}
