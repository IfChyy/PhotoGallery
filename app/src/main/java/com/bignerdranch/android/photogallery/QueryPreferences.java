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

    //returns the value stored in the prefs
    //null specifies the default return value
    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    //writes the default query to the shared prefs
    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
