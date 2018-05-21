package com.naver.hackday.android_extract_gif.extractor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hanseungbeom on 2018. 5. 18..
 */

public class GifEncodeHelper {

    private static final String TAG = GifEncodeHelper.class.getSimpleName();

    private GifEncoder gifEncoder;
    private Queue<Bitmap> frameQueue;
    private ByteArrayOutputStream baos;
    private boolean isRunning;

    private EncodeListener listener;

    public interface EncodeListener {
        public void onEncodeStarted();
        public void onEncodeFinished(Uri gifUri);
    }
    public void setOnEncodeListener(EncodeListener listener){
        this.listener = listener;
    }

    public GifEncodeHelper(int fps){
        baos = new ByteArrayOutputStream();
        gifEncoder = new GifEncoder();
        gifEncoder.setDelay(ExtractorUtils.getDelayOfFrame(fps));
        gifEncoder.start(baos);
        frameQueue = new LinkedList<>();
        isRunning = false;
    }

    public void startThread(){
       isRunning = true;
       Thread queueCheckingThread = new Thread(new Runnable() {
           @Override
           public void run() {
               Log.d(TAG,"queue checking thread started..");
               while(isRunning || !frameQueue.isEmpty()){
                   if(!frameQueue.isEmpty()){
                       Bitmap frame = frameQueue.poll();
                       gifEncoder.addFrame(frame);
                       frame.recycle();
                   }
               }

               listener.onEncodeStarted();
               gifEncoder.finish();
               long createdTime = System.currentTimeMillis();
               Uri gifUri = ExtractorUtils.makeGifFile(baos,String.valueOf(createdTime));
               listener.onEncodeFinished(gifUri);

               Log.d(TAG,"queue checking thread ended..");
           }
       });
       queueCheckingThread.start();
    }

    public void addFrame(Bitmap bitmap){
        frameQueue.add(bitmap);
    }
    public void finish(){
        isRunning = false;
    }

}
