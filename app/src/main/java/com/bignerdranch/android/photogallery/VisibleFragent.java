package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * Created by Ivo Georgiev(IfChyy)
 * VisibleFragmnet class is a class holding a dynamic broadcast receiver
 * its job is to listen whether our app has started polling or not
 */

public abstract class VisibleFragent extends Fragment{
    private static final String TAG = "VisibleFragment";

    //if polling is started register a broadcast receiver knowing that the polling is running
    //if permission passed in register receiver, we stop others from creating
    //their own broadcast intents and accessing our receiver.
    @Override
    public void onStart() {
        super.onStart();
        //create a new intent filter with our (Action) show notification
        //which filters if there is an Intent with that action
        IntentFilter filter = new IntentFilter(PollTestService.ACTION_SHOW_NOTIFICATION);
        //registerReceiver meaning that our app is in foreground
        getActivity().registerReceiver(onShowNotification, filter
        , PollTestService.PERMITION_RPIVATE, null);
    }

    //if polling is stoped then unregister our receiver
    @Override
    public void onStop() {
        super.onStop();
        //if our app goes in background  unregister our broadcast
        getActivity().unregisterReceiver(onShowNotification);
    }

    //create a broadcast receiver, when it receives that it was registered, show a toast
    private BroadcastReceiver onShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          //if received, meaning the app is visible = cancel thhe notification
            //here we set our broadcastreceiver a result code of RESULT_CANCELDED
            //meaning that our OrderedBroadCastReceiver in PollTestService will receive this negative code
            //and it will not show a notification
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
