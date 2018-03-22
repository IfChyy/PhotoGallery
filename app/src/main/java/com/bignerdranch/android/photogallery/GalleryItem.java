package com.bignerdranch.android.photogallery;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivo Georgiev(IfChyy)
 * GalleryItem class
 * holds information about each item picture
 * downloaded from flicker
 * eg
 * caption , id and url of picture
 *
 *
 * AFTER CHALLENGE TO EVERY VARIABLE ADDED SERIALZIABLE NAME
 * used for refering to the json returned from our api so
 * that we could reference the values we need
 *
 * eg. every variable need @SerializedName("name") above its initialisation
 * //this name is the value we search it by and the value it is inserted automaticaly by
 * GSON library using setter methods
 */

public class GalleryItem {

    @SerializedName("title")
    private String caption;
    @SerializedName("id")
    private String id;
    @SerializedName("url_s")
    private String url;

    @Override
    public String toString() {
        return caption;
    }


    //---------------------GETTERS AND SETTTERS


    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
