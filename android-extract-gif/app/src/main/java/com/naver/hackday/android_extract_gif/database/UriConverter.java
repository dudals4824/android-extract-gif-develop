package com.naver.hackday.android_extract_gif.database;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

public class UriConverter {
    @TypeConverter
    public static Uri toUri(String uri){
        return uri == null ? null : Uri.parse(uri);
    }

    @TypeConverter
    public static String toString(Uri uri){
        return uri == null ? null : uri.toString();
    }
}
