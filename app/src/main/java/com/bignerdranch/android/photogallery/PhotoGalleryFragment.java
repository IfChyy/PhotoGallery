package com.bignerdranch.android.photogallery;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivo Georgiev(IfChyy)
 * PhotoGalleryFragment is a fragment containing our gallery
 * which we pass to photoGalleryActivity with newInstance method
 * so it will be displayed in our frame layout
 */

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";


    private RecyclerView recyclerView;
    private List<GalleryItem> galleryItemArraList = new ArrayList<>();


    //int giving the count of photos page displayed by the app
    int pageCount = 1;
    boolean endHasBeenReached;

    //collumn widht of images
    private final int COLLUMN_WIDTH = 200;
    private int collumnsNumber;

    GridLayoutManager gridLayoutManager;

    //used to retain the fragment for a short period of time
    //while rotating the phone for not calling new async tasks
    //every time phone rotates
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //execute the asycn task
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //create and inflate our view
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        //init the recyclerview
        recyclerView = view.findViewById(R.id.gallery_recycler_view);
        //set recycler view with grid layout manager with 3 collumns
      //  recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        //CHALLENGE set the number of collumns dinamycally
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        //tree observer to know when the recycler view has loaded to set the number of collumns
        ViewTreeObserver viewTreeObserver = recyclerView.getViewTreeObserver();
        //wait for the observer to get the layout ready
        //then calculate the number of collumns
        //and set them
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
               collumnsNumber = recyclerView.getWidth()/COLLUMN_WIDTH;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setSpanCount(collumnsNumber);
                    }
                }, 100);

            }
        });



        //set recycler view with grid layout manager with each collumn equl to 200Pixels
        recyclerView.setLayoutManager(gridLayoutManager);




        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                endHasBeenReached = lastVisible + 5 >= totalItemCount;
                if (pageCount < 10) {
                    if (endHasBeenReached && totalItemCount > 0) {
                        pageCount++;

                        Toast.makeText(getActivity(), "POS" + pageCount, Toast.LENGTH_SHORT).show();

                        layoutManager.scrollToPosition(0);
                        new FetchItemsTask().execute();
                    }
                }
            }
        });


        setupAdapter();

        return view;

    }

    //init the instance of photoGalleryFragment in other class
    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    //called for every time a model of object changes
    //isAdded first check that the fragment is addedto the activity
    //
    private void setupAdapter() {
        if (isAdded()) {
            PhotoAdapter adapter = new PhotoAdapter(galleryItemArraList);
            recyclerView.setAdapter(adapter);
        }
    }

    //----------------------PHOTO HOLDER
    //VIEW HOLDER class to hold the infromation about each gallery item
    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView picTitle;

        public PhotoHolder(View itemView) {
            super(itemView);
            picTitle = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {

            picTitle.setText(item.toString());
        }
    }

    //-----------------------PHOTO ADAPTER
    // class to populate the Recyrcler view with items from a list
    //and specify how to display them by a layout
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> itemsList;


        public PhotoAdapter(List<GalleryItem> galleryItems) {
            this.itemsList = galleryItems;

        }

        //create hte view holder of eac h item
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView photoText = new TextView(getActivity());
            return new PhotoHolder(photoText);
        }

        //create a new Gallery Item object
        //bind it to the holder
        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = itemsList.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return itemsList.size();
        }
    }

    //---------------INIT ASYNC TASK
    //used to run some process in the backgorund thread
    //for this app it is used to download the images in the
    //background and then place them in the views specified

    //1st parameter specifies the data you pass to execute method
    //2nd parameter spcifies the type for sending progress updates
    //used for displaying progress updates on UI THREAD
    //3rd parameter is the type of result returned by Async TASK
    public class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        //does something in the backgorund
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            //fetch a string from url with json data
            return new FlickFetcher().fetchItems(String.valueOf(pageCount));

        }

        //after images downlaoded setup the adapter
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            galleryItemArraList = items;
            setupAdapter();

        }
    }
}
