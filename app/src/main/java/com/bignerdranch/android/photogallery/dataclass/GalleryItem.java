package com.bignerdranch.android.photogallery.dataclass;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivo Georgiev(IfChyy)
 * GalleryItem class is a dataclass holding information about each picture taken from Flicker
 *
 * Challenge to use GSON LIBRARY
 * @serializaleName says that this field is going to get that JSON value
 */

public class GalleryItem {

    @SerializedName("title")
    private String caption;
    @SerializedName("id")
    private String id;
    @SerializedName("url_s")
    private String url;
    @SerializedName("owner")
    private String owner;

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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    //method to get the photoPageUri
    public Uri getPhotoPageUri(){
        return Uri.parse("http://www.flickr.com/photos")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build();

    }
}
