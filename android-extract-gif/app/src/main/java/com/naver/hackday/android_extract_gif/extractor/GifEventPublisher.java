package com.naver.hackday.android_extract_gif.extractor;

import android.net.Uri;

import com.naver.hackday.android_extract_gif.database.GifEntry;

import java.util.ArrayList;

/**
 * Created by hanseungbeom on 2018. 5. 18..
 */

public enum GifEventPublisher {

    INSTANCE;

    private ArrayList<GifExtractManager.GifExtractManagerListerner> extractListeners = new ArrayList<>();

    public void addListener(GifExtractManager.GifExtractManagerListerner listener){
        extractListeners.add(listener);
    }

    public void notifyFirstExtracted(int max, Uri thumbUri){
        for(GifExtractManager.GifExtractManagerListerner listener : extractListeners)
            listener.onExtractStarted(max,thumbUri);
    }
    public void notifyFrameExtracted(int max, int progress){
        for(GifExtractManager.GifExtractManagerListerner listener : extractListeners)
            listener.onExtractFrame(max,progress);
    }

    public void notifyEncodeStarted(){
        for(GifExtractManager.GifExtractManagerListerner listener : extractListeners)
            listener.onEncodeStarted();
    }

    public void notifyEncodeFinished(){
        for(GifExtractManager.GifExtractManagerListerner listener : extractListeners)
            listener.onEncodeFinished();
    }

    public void notiyExtractFinished(GifEntry gifEntry){
        for(GifExtractManager.GifExtractManagerListerner listener : extractListeners)
            listener.onExtractFinished(gifEntry);
    }



}
