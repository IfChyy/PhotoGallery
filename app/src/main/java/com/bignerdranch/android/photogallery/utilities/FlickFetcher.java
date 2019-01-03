package com.bignerdranch.android.photogallery.utilities;

import android.net.Uri;
import android.util.Log;

import com.bignerdranch.android.photogallery.dataclass.GalleryItem;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivo Georgiev (IfChyy)
 * FlickerFetcher is a java class responsible for the networking done
 * by our Gallery app
 */

public class FlickFetcher {

    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY = "9e314633e0f176cb654d9d6f96b99e00";

    //Chapter 25
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();
    private String url;


    //list holding each GalleryItem
    List<GalleryItem> galleryItems = new ArrayList<>();

    //method getUrlBytes  fetches raw data from URL and returns it
    //as an array of bytes
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        //init the url
        URL url = new URL(urlSpec);
        //init the connection to the HttpServer
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            //iit the bytearrayoutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //init input strean from HttpConnection
            //used for POST calls ( if not invoked you cant get a valid responce from HTTP
            InputStream inputStream = connection.getInputStream();

            //check if connection response code is OK responce code
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with" +
                        urlSpec);
            }


            //get bytes red
            int bytesRead = 0;
            //init the max number of buffered bytes per time
            byte[] buffer = new byte[1024];

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            //finaly disconnect the connection for security and interent issues
            connection.disconnect();
        }
    }


    //getUrlString method converst the result from GetUrlBytes
    // to a preresentable format (String);
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
    //-------CHAPTER 25 created new method to save this one
    //method to build an URL request and fetch contents
    //CHALLENGE ADDED A PAGE STRING to the method to pass a reference to which page
    // of pictures to return from flicker
    public List<GalleryItem> fetchItems(String page) {
        try {
            //build the url get request ( REST request)
            //use method of flicker to get most recent photos
            //use our api key we specified and got from flicker
            //format the data passed in json
            // no json call back 1 = used for exlcuding enclosed method name nad parnthesses from responce
          /*  String url = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
               //    .appendQueryParameter("method", "flickr.photos.getRecent")
                   //.appendQueryParameter("method", "flickr.photos.search")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("per_page", "300")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    //.appendQueryParameter("text", "")
                    .build().toString();*/


            //get the responce in a string format
            String jsonString = getUrlString(page);

            //format the outptu json string into a big json object
            JSONObject jsonObject = new JSONObject(jsonString);
            //pass the object to the method which goes into the json
            //to populate our list of items (gallery pics) ;
            //  parseItems(galleryItems, jsonObject);
            //CHALLENGE USE GSON TO go through the JSON and update our list of GAllery items
            parseItemsGson(galleryItems, jsonObject);


            Log.d(TAG, "fetchItems: " + jsonString);

        } catch (JSONException je) {
            Log.d(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.d(TAG, "Failed to fetch items", ioe);
        }

        return galleryItems;
    }

    //CHAPTER 25
    //method to download the gallery items with given url
    //basically the same method as fetchItems but with Gson Library
    public List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            //get the responce in a string format
            String jsonString = getUrlString(url);
            //format the outptu json string into a big json object
            JSONObject jsonObject = new JSONObject(jsonString);
            //pass the object to the method which goes into the json
            //to populate our list of items (gallery pics) ;
            parseItems(items, jsonObject);


        } catch (JSONException je) {
            Log.d(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.d(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    //CHAPTER 25
    //method to dynamically fill the method parameter value for the request
    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", "flickr.photos.getRecent");
        //if method is search append the searched value
        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("method", method).appendQueryParameter("text", query);
        }

        Log.d(TAG, "buildUrl: " + method);
        return uriBuilder.build().toString();
    }

    //CHAPTER 25
    //fetch the recent photos
    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        Log.d(TAG, "fetchRecentPhotos: " + url);
        return fetchItems(url);
    }
    //CHAPTER 25
    //fetch the recent photos
    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD, query);
        return fetchItems(url);
    }


    //method to parse each item from the jsonBody to Each GalleryItem
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {

        //get the first object of the JSON eg photos
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");

        //get the photo array
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        //iterate throught all the array of photo objects
        for (int i = 0; i < photoJsonArray.length(); i++) {

            //get each jsonObject in the array
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            //create new Gallery item object and set its id and title from
            //each JSON object form the json array
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            item.setOwner(photoJsonObject.getString("owner"));
            Log.d(TAG, "Received JSON " + item.getOwner());
            //check if jsobObject (photo) has url
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            //if yes set Item url
            item.setUrl(photoJsonObject.getString("url_s"));
            //populate list of items

            items.add(item);
        }
    }


    //CHALLENGE USE GSON LIBRARY TO SIMPLIFY THE REQUEST
    //method to parse each item from the jsonBody to Each GalleryItem USING GSON LIBRARY
    private void parseItemsGson(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        //init gson
        Gson gson = new Gson();

        //use gallery item to get json
        //1st get gson.fromjsonArray - takse 2 arguments
        //1st argument is the jsonobject and array you are looking for
        //this exaple is with
        // jsonobject - photos > json array photos[] > and each array has our itesm
        // more about these items in GALLERY ITEM CLASS,
        //2nd argument we pass the same class where we want the data inputed

        /* TODO GalleryItem item = gson.fromJson(jsonBody.getJSONObject("photos").getJSONArray("photo").getJSONObject(1).toString(), GalleryItem.class);*/

        //size variable is to get the size of the json array we want to use in our app
        //so that we can iterate throught all gallery items and add tem in our list of gallery items
        int size = jsonBody.getJSONObject("photos").getJSONArray("photo").length();

        for (int i = 0; i < size; i++) {
            GalleryItem item = gson.fromJson(jsonBody.getJSONObject("photos")
                    .getJSONArray("photo").getJSONObject(i).toString(), GalleryItem.class);

            items.add(item);
        }

    }


}
