package com.bignerdranch.android.photogallery;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Ivo Georgiev(IfChyy)
 * SingleFragmentActivity is a singleton abstract class with one method
 * oncreateFragment
 * which gets the fragment by a child class and represents it in
 * our frameLayout (fragmentContainer
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        //init the fragment manager to get our fragment in our container
        FragmentManager fm = getFragmentManager();
        //init the fragment where we are placing our view fragment activity
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        //check if fragment is null if yes make fragment get the instance of createFragment
        // use fragment manager to place the new fragment in our framelayout
        if(fragment == null){
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }


    private int getLayoutResId(){
        return R.layout.activity_fragment;
    }
}



