package com.bignerdranch.android.photogallery;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import com.bignerdranch.android.photogallery.utilities.SingleFragmentActivity;

/**
 * Created by Ivo Georgiev (IfChyy)
 * PhotoGalleryActivity
 * is the activity which is resposnible for showing our PhotoGalleryFragment
 * in our framelayout
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    //method used to start PhotoGalleryActivity called from somewhere else

    public static Intent newIntent(Context context){
        return  new Intent(context, PhotoGalleryActivity.class);
    }
}
