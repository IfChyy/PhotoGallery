package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Ivo Georgiev(IfChyy)
 * A shared preferences class
 * using
 * PreferenceManager.getdefaultShareDpreferences(context)
 * in this applicaiton we dont care to much about the specific instance
 * of the shared pref, just that it is shared accros our app.
 * In this case its better to use this type of sharedpreferences
 * which returns an instance with a default name and private permissions
 * so that the prefs are only available in our app
 */

public class QueryPreferences {

    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultid";
    private static final String PREF_IS_ALARM_ON = "isAlarmOne";

    //returns the value stored in the prefs
    //null specifies the default return value
    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    //writes the default query to the shared prefs
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }


    //get the last saved id prefference
    public static String getPrefLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }


    //set the shared prefs to the last catched photo
    public static void setLastResultId(Context context, String lastResultid) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultid)
                .apply();
    }

    public static void setAlarmOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }

    public static boolean isAlarmOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }
}
