package com.naver.hackday.android_extract_gif.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {GifEntry.class}, version = 1, exportSchema = false)
public abstract class GIFDataBase extends RoomDatabase{

    private static final String LOG_TAG = GIFDataBase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "giflist";
    private static GIFDataBase mInstance;

    public static GIFDataBase getInstance(Context context){
        if(mInstance == null)
        {
            synchronized (LOCK){
                Log.d(LOG_TAG, "Create new DB instance");
                mInstance = Room.databaseBuilder(context.getApplicationContext(),
                        GIFDataBase.class, GIFDataBase.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the DB instance");
        return mInstance;
    }
    public abstract GIFDao gifDao();
}