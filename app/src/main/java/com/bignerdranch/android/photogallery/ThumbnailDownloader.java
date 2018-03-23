package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Ivo Georgiev(IfChyy)
 * ThumbnailDownloader class consist of two items
 * Handler and a looper,
 * the ideq is to create a queue called "message loop"
 * which loops again and again waiting for new task to come
 * so the handler can execute
 * **THE MAIN Thread does the same thing
 * it is a combination of a thread and a looper
 * looper looks for things that need done,
 * and tells the main trhead that what it has to do
 * <p>
 * <p>
 * our part is going to be to implement
 * a looper looking for images on our recycler
 * that need to be displayed
 * and tell our handler to download just those images
 * so we wont run out of memory
 * <p>
 * <p>
 * Async task not used because if there is a slwo connection
 * or a needed loop , or a very big set of data to be downloaded
 * the async task would fall behind and load times would be huge
 * <p>
 * <p>
 * <p>
 * this is why we are creating a background thread
 * HanlderTHread that prepears A Looper for us
 * which is also going to be our message loop
 * <p>
 * using argument "<T>" WE are not telling the user of a single
 * type of item to look for. This way he can call
 * any type of item he wants( used for flexibility)
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    //constant for message downloading from flickr
    //identifies messages ans download request
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    //get max available VM memory for LruCache, more than that will throw exception
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    //use 1/8th of the memory for the cache
    private final int cacheSize = maxMemory / 8;

    //stores a reference to the Handler responsible
    //for queueing download requests as mesages onto ThumbnailDownloader
    //also in charge of processing download request the pulled of the queue
    private static Handler requestHandler;
    //ConcuretntHashMap is thread save version of HashMap
    //using request to identifty T as a key
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();

    //handler to handle the responce after image downloaded from requestHanlder
    //this handler loads the image into the view
    private Handler responceHanlder;
    //separetees the work of  ThumbnalDownloader to new handler to update the UI
    //so the first handler (Reqeust handler) can download other objects when needed
    private ThumbnailDownloadListener<T> thumnailDownloadListener;


    //LRUCACHE to cache some downloade images in cache so they can bre preloaded faster
    private LruCache<String, Bitmap> memoryCache;

    //init constructor which takes a parameter handler
    //which is responsible for handling the updates of the UI
    public ThumbnailDownloader(Handler responceHandler) {
        super(TAG);
        this.responceHanlder = responceHandler;

        //CHALLENGE PRELOAD CACHE
        memoryCache = new LruCache<String, Bitmap>(16384);
    }

    //we pass here an object of type T as an id for th download
    //and a url string
    public void queueThumbnail(T target, String url) {
        //check if url is null remove request
        if (url == null) {
            requestMap.remove(target);
        } else {
            // inf not null  update the request HashMap with the url and key
            requestMap.put(target, url);
            //post the new message to the background thread message queue
            //represents a download request
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
        Log.d(TAG, "Got a URL: " + url);
    }


    //init request handler and define what handler does
    //when images are downloaded and pulled of the queue
    //called before looper chesk the queue for the first time
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        requestHandler = new  Handler() {
            @Override
            public void handleMessage(Message msg) {
                //what - a user defined int that describes the message
                if (msg.what == MESSAGE_DOWNLOAD) {
                    //obj - user specified obj to be sent with the message
                    //target = the nahdler that will handle the message
                    T target = (T) msg.obj;
                    Log.d(TAG, "Got a request for URL: " + requestMap.get(target));
                    //handle the message
                    handleRequest(target);
                    return;
                }

                //CHALLENGE PRELOAD CACHE
                if (msg.what == MESSAGE_PRELOAD) {
                    String url = (String) msg.obj;
                    downloadImage(url);
                    return;

                }
            }
        };
    }

    //helper method to download
    //first check if url is not null
    //then pass the url to get the url bytesArray
    //finally used bitmap to cosntruct the byteArray into image
    private void handleRequest(final T target) {
        try {
            final String url = requestMap.get(target);
            //CHALLENGE PRELOAD CACHE
            final Bitmap bitmap;

            if (url == null) {
                return;
            }
            //CHALLENGE PRELOAD CACHE code commented moved to method downloadImage bellow
         /*   byte[] bitmapBytes = new FlickFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0
                    , bitmapBytes.length);*/

            bitmap = downloadImage(url);


            //check the requestMap, necessary becuase recycler recycles views
            //the view may be recycled before being downloaded
            //finaly remove the ulr from the map and set it onto the bitmap holder
            responceHanlder.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(target) != url) {
                        return;
                    }
                    requestMap.remove(target);
                    thumnailDownloadListener.onThumbnalDownloaded(target, bitmap);
                }
            });


        } finally {

        }
    }

    //used for rotating the screen so the images requested are stopped for downloading
    public void clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    //CHALLENGE PRELOAD CACHE
    //preload image if downloaded in cache
    public void preloadImage(String url) {
        requestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
    }

    //CHALLENGE PRELOAD CACHE
    //get the cached image
    public Bitmap getCachedImage(String url) {
        return memoryCache.get(url);
    }

    //CHALLENGE PRELOAD CACHE
    //clear the cache
    public void clearCache() {
        memoryCache.evictAll();
    }

    //CHALLENGE PRELOAD CACHE
    public Bitmap downloadImage(String url) {
        Bitmap bitmap;

        if (url == null) {
            return null;
        }

        //if the image is already in cache. dont download just return it
        bitmap = memoryCache.get(url);
        if (bitmap != null) {
            return bitmap;
        }

        //downaload and cached the image, then return it
        try {
            byte[] bitmapBytes = new FlickFetcher().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0
                    , bitmapBytes.length);
            //put it in cache
            memoryCache.put(url, bitmap);
            return bitmap;
        } catch (IOException ioe) {
            return null;
        }
    }


    //*--------------THUMBNAILDOWLOADLISTENER INTERFACE
    //with this interface we separate the downloading task from the UI update taks
    //so that ThumbnailDOwnloader could be used for downloaidn other view objects as needed
    public interface ThumbnailDownloadListener<T> {
        //called when image fully downloaded and ready to be added to UI
        //
        void onThumbnalDownloaded(T target, Bitmap thumbnail);
    }

    //sets the thumbnail downloader
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        thumnailDownloadListener = listener;
    }
}
