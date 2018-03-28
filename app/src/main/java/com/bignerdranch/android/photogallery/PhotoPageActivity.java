package com.bignerdranch.android.photogallery;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by Ivo Georgiev(IfChyy)
 * <p>
 * PhotoPageActivity is a holder class to hold the fragment for the photo page
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    //init the fragment we want to create


    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }
    //call this method form other class to start our page activity
    public static Intent newIntent(Context context, Uri photoUri){
        Intent in = new Intent(context, PhotoPageActivity.class);
        in.setData(photoUri);
        return in;
    }

    @Override
    public void onBackPressed() {
        if(PhotoPageFragment.webView.canGoBack()){
            Toast.makeText(this, "CAN GO BACK", Toast.LENGTH_SHORT).show();
            PhotoPageFragment.webView.goBack();
        }else if(!PhotoPageFragment.webView.canGoBack()){
            super.onBackPressed();
        }

    }

    public void startIntent(Uri url){
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        startActivity(intent);
    }
}
