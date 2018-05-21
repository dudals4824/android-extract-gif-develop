package com.naver.hackday.android_extract_gif.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.net.Uri;

/**
 * Created by hanseungbeom on 2018. 5. 15..
 */
@TypeConverters({UriConverter.class})
@Entity(tableName = "gif")
public class GifEntry {
    @PrimaryKey (autoGenerate = true)
    private int id;
    private Uri gifUri;
    private Uri thumbUri;
    private long createdTime;

    @Ignore
    public GifEntry(Uri gifUri, Uri thumbUri, long createdTime) {
        this.gifUri = gifUri;
        this.thumbUri = thumbUri;
        this.createdTime = createdTime;
    }

    public GifEntry(int id, Uri gifUri, Uri thumbUri, long createdTime){
        this.id = id;
        this.gifUri = gifUri;
        this.thumbUri = thumbUri;
        this.createdTime = createdTime;
    }

    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }

    public Uri getGifUri() {
        return gifUri;
    }

    public void setGifUri(Uri gifUri) {
        this.gifUri = gifUri;
    }

    public Uri getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(Uri thumbUri) {
        this.thumbUri = thumbUri;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
