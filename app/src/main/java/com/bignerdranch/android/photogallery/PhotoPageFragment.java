package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Ivo Georgiev(IfChyy)
 * PhotoPageFragment
 * is a web view representing each Flickr picture's own photo page
 * extends VisibleFragment to know that the app si open and wont show notification
 */

public class PhotoPageFragment extends VisibleFragent {
    private static final String ARG_URI = "photo_page_url";
    //init uri adn webview
    private Uri uri;
    public static WebView webView;
    //init progress bar
    private ProgressBar progressBar;

    //create constructor with arguments passing the uri of the page
    public static PhotoPageFragment newInstance(Uri uri) {
        //create bundle to pass the ifno to the framgnet
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        //init our fragment and pass the arguments, then return the fragment to be created
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //after created get our uri
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uri = getArguments().getParcelable(ARG_URI);


    }

    //when view created inflate our layout
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

        //init the progressBar
        progressBar = view.findViewById(R.id.fragment_photo_page_progress_bar);
        progressBar.setMax(100); // webViewClient reports in range 0 -100

        //init our webview
        webView = view.findViewById(R.id.fragment_photo_page_web_view);
        //enable javascript (flickr needs it)
        webView.getSettings().setJavaScriptEnabled(true);

        //WEB CHROM CLIENT
        webView.setWebChromeClient(new WebChromeClient() {
            //set the progresbarr
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //if 100 hide progress bar
                    progressBar.setVisibility(View.GONE);
                } else {
                    //if loading show progressbar and update appropriately
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //get the activity
                AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
                //set the title with the gotten one
                if (webView.getVisibility() == View.VISIBLE) {
                    appCompatActivity.getSupportActionBar().setSubtitle(title);
                }
            }

        });
        //own implementation of the webViewClient (Gives more broad aspect of how to represent the web page)
        //CUSTOM WEB VIEW CLIENT
        //used to show webiew inside app( if code does not exists - web chrome client opens the browser
        webView.setWebViewClient(new WebViewClient() {
            //this method determines what happens when a url is loaded
            //false - load the url
            //true - dont handle the url, I will handle it alone
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                Log.d("PhotoPageFragment", "shouldOverrideUrlLoading: " + URLUtil.isValidUrl(url));
                if (!URLUtil.isValidUrl(url)) {
                 PhotoPageActivity activity = new PhotoPageActivity();
                 activity.startIntent(Uri.parse(url));
                }
                return false;
            }
        });

        //web view constructed then load the url onto it
        webView.loadUrl(this.uri.toString());
        return view;
    }


}
