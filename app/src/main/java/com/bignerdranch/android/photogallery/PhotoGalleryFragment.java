package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;

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
        setHasOptionsMenu(true);
        //execute the asycn task
        // new FetchItemsTask().execute();

        //CHAPTER25
        updateItems();

        //-------------Pass the handler which is attached to the main thread
        //so it updates the UI
        //----------set ThumbnailDownloaderListener to handle the downloaded image
        Handler responceHandler = new Handler();
        //init our thumbnail downlaoder
        thumbnailDownloader = new ThumbnailDownloader<>(responceHandler);
        //init the listner to know when the image has been downloaded
        //and passed to responcehandler to update UI
        thumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnalDownloaded(PhotoHolder holder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                //-------------USING PICASSO
                //holder.bindGalleryItem(drawable);
            }
        });

        //start the downloade hanlder thread
        thumbnailDownloader.start();
        //get the looper
        thumbnailDownloader.getLooper();
        Log.d(TAG, "Background thread started@!");
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
                collumnsNumber = recyclerView.getWidth() / COLLUMN_WIDTH;
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
                        updateItems();
                    }
                }
            }
        });


        setupAdapter();

        return view;

    }

    //CHAPTER25
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        //represents searchbox
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        //get the search view
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //executed when query is submited
            public boolean onQueryTextSubmit(String s) {

                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = getActivity().getCurrentFocus();
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                return true;
            }

            //executed each time a the text in sarchView chagnes
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //set on click listener for the search view
        //to populate the search bar with the most recent
        //query saved in shared prefs
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    //CHAPTER 25
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //CHAPTER25
    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    //if the screen is rotated stop the handler from downloading images
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

    //stop the downloading if acitivty destoryed
    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();
        Log.d(TAG, "Background thread destoroyed!");
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
        private ImageView photoView;

        public PhotoHolder(View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.gallery_image_view);
        }

        /* public void bindGalleryItem(Drawable drawable) {
             photoView.setImageDrawable(drawable);
         }*/
        //-------------USING PICASSO
        public void bindGalleryItem(GalleryItem galleryItem) {
            Picasso.get().load(galleryItem.getUrl())
                    .placeholder(R.drawable.bill_up_close)
                    .into(photoView);
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
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        //create a new Gallery Item object
        //get a drawlable res
        //bind it to the holder
        //set the downloader to download images
      /*  @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = itemsList.get(position);
            //get cached image


            Bitmap bitmap = thumbnailDownloader.getCachedImage(galleryItem.getUrl());

            if (bitmap == null) {
                Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
                holder.bindGalleryItem(drawable);
                thumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
            } else {
                holder.bindGalleryItem(new BitmapDrawable(getResources(), bitmap));
            }


            //preload previous and adjecent images
            preloadAdjecentImages(position);


        }*/

        //-------------USING PICASSO
        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = itemsList.get(position);
            holder.bindGalleryItem(galleryItem);

        }

        @Override
        public int getItemCount() {
            return itemsList.size();
        }


        //CHALLENGE Cached previous and next 10 images
        private void preloadAdjecentImages(int position) {
            //number of images to preload
            final int imageBufferSize = 10;

            //set the indexes for the images to preload
            //start must be greater or equal to 0 >=0
            int startIndex = Math.max(position - imageBufferSize, 0);
            //last indext must be less or equal to the items list minus 1  <= 1
            int endIndex = Math.min(position + imageBufferSize, itemsList.size() - 1);


            //loop over gallery items and our bounds
            for (int i = startIndex; i < endIndex; i++) {
                //dont preload current
                if (i == position) {
                    continue;
                }

                String url = itemsList.get(i).getUrl();
                thumbnailDownloader.preloadImage(url);
            }
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
        //CHAPTER 25
        private String query;
        // progress dialog to show user that the images are loading
        ProgressDialog dialog;

        //CHAPTER 25 CONSTRUCTOR
        public FetchItemsTask(String query) {
            this.query = query;
        }

        //does something in the backgorund
        //CHAPTER 25 CHALLENGE
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("loading");
            dialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            //fetch a string from url with json data
            //return new FlickFetcher().fetchItems(String.valueOf(pageCount));

            //Chapter 25
            if (query == null) {
                return new FlickFetcher().fetchRecentPhotos();
            } else {
                return new FlickFetcher().searchPhotos(query);
            }
        }

        //after images downlaoded setup the adapter
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if(dialog != null){
                dialog.dismiss();
            }
            galleryItemArraList = items;
            setupAdapter();

        }
    }
}
