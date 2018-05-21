package com.naver.hackday.android_extract_gif.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.database.GIFDataBase;
import com.naver.hackday.android_extract_gif.database.GifEntry;
import com.naver.hackday.android_extract_gif.extractor.GifEventPublisher;
import com.naver.hackday.android_extract_gif.extractor.FrameExtractor;
import com.naver.hackday.android_extract_gif.extractor.GifExtractManager;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class GifActivity extends Activity{

    private static final String TAG = GifActivity.class.getSimpleName();
    private GifImageView mGifImageView;
    private ProgressBar mProgressBar;
    private GIFDataBase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_view);
        init();

        GifEventPublisher.INSTANCE.addListener(new GifExtractManager.GifExtractManagerListerner(){
            @Override
            public void onExtractFinished(GifEntry gifEntry) {
                Log.d(TAG,"onExtractFinished..");
                setloadingBarVisibility(View.INVISIBLE);
                setGif(gifEntry.getGifUri());
                mDB.gifDao().insertGIF(gifEntry);
            }

            @Override
            public void onExtractFrame(int max, int progress) {
                Log.d(TAG,"onExtractFrame..("+max+","+progress+")");
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onEncodeStarted() {
                Log.d(TAG,"onEncodeStarted..");

            }

            @Override
            public void onEncodeFinished() {
                Log.d(TAG,"onEncodeFinished..");
            }

            @Override
            public void onExtractStarted(int max, Uri thumbUri) {
                Log.d(TAG,"onExtractedFirstTime..");
                setloadingBarVisibility(View.VISIBLE);
                mProgressBar.setMax(max);
                mProgressBar.setProgress(0);
            }
        });
    }

    private void init(){
        mDB = GIFDataBase.getInstance(getApplicationContext());
        mGifImageView = (GifImageView)findViewById(R.id.iv_gifImage);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void setGif(final Uri gifUri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    GifDrawable gifUriDrawable = new GifDrawable(null, gifUri);
                    gifUriDrawable.setLoopCount(0);
                    mGifImageView.setImageDrawable(gifUriDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setloadingBarVisibility(int visibility){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(visibility);
            }
        });
    }
}