package com.naver.hackday.android_extract_gif.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface GIFDao {

    @Query("SELECT * FROM gif")
    List<GifEntry> getAll();

    @Insert
    void insertGIF(GifEntry gifEntry);

    @Delete
    void deleteGIF(GifEntry gifEntry);
}
