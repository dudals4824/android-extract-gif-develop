package com.naver.hackday.android_extract_gif.extractor;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hanseungbeom on 2018. 5. 21..
 */

public class FrameResizeHelper {

    private static final String TAG = FrameResizeHelper.class.getSimpleName();
    private static final int RESIZE_WIDTH = 400;

    private GifEncodeHelper gifEncodeHelper;
    private Queue<Bitmap> frameQueue;
    private boolean isRunning;

    public FrameResizeHelper(GifEncodeHelper gifEncodeHelper){
        this.gifEncodeHelper = gifEncodeHelper;
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
                        Bitmap resizedFrame = resizeBitmap(frame);
                        gifEncodeHelper.addFrame(resizedFrame);
                        frame.recycle();
                    }
                }

                gifEncodeHelper.finish();

                Log.d(TAG,"queue checking thread ended..");
            }
        });
        queueCheckingThread.start();
    }

    public Bitmap resizeBitmap(Bitmap source){
        float aspectRatio = source.getWidth() / (float) source.getHeight();
        int width = Math.min(source.getWidth(),RESIZE_WIDTH);
        int height = Math.round(width / aspectRatio);

        Bitmap resizedFrame = Bitmap.createScaledBitmap(
                source, width, height, false);

        return resizedFrame;
    }
    public void addFrame(Bitmap bitmap){
        frameQueue.add(bitmap);
    }
    public void finish(){
        isRunning = false;
    }



}
