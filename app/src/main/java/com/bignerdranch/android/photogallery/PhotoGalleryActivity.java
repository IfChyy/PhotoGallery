package com.bignerdranch.android.photogallery;

import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

}
