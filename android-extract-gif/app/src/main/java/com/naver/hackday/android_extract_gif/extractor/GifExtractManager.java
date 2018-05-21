package com.naver.hackday.android_extract_gif.extractor;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.naver.hackday.android_extract_gif.database.GifEntry;
import com.naver.hackday.android_extract_gif.player.Utils;

/**
 * Created by hanseungbeom on 2018. 5. 18..
 */

public class GifExtractManager {
    private static final String TAG = GifExtractManager.class.getSimpleName();

    private Context mContext;
    private FrameExtractor frameExtractor;
    private FrameResizeHelper frameResizeHelper;
    private GifEncodeHelper encodeHelper;

    private GifEntry result;

    private GifExtractManagerListerner listerner;

    public interface GifExtractManagerListerner{
        public void onExtractStarted(int max,Uri thumbUri);
        public void onExtractFrame(int max, int progress);
        public void onEncodeStarted();
        public void onEncodeFinished();
        public void onExtractFinished(GifEntry gifEntry);
    }
    public void setListerner(GifExtractManagerListerner listerner){
        this.listerner = listerner;
    }

    public GifExtractManager(Context context,Bundle extractInfo){
        mContext = context;
        initFrameExtractor(context,extractInfo);
        initEncodeHelper(extractInfo);
        frameResizeHelper = new FrameResizeHelper(encodeHelper);

        result = new GifEntry(null,null,0);
    }
    public void initEncodeHelper(Bundle extractInfo){
        int fps = extractInfo.getInt(ExtractorUtils.GIF_EXTRACT_FPS);
        encodeHelper = new GifEncodeHelper(fps);
        encodeHelper.setOnEncodeListener(new GifEncodeHelper.EncodeListener() {
            @Override
            public void onEncodeStarted() {
                listerner.onEncodeStarted();
            }

            @Override
            public void onEncodeFinished(Uri gifUri) {
                Log.d(TAG,"onEncodeFinished..");
                result.setGifUri(gifUri);
                listerner.onExtractFinished(result);
                listerner.onEncodeFinished();
                Utils.addImageToGallery(mContext,gifUri);
            }
        });
    }
    public void initFrameExtractor(Context context, Bundle extractInfo){
        frameExtractor = new FrameExtractor(context,extractInfo);
        frameExtractor.setOnEventListener(new FrameExtractor.EventListener() {
            @Override
            public void onExtractStarted(int max, Uri thumbUri) {
                Log.d(TAG,"onExtractStarted..");
                encodeHelper.startThread();
                frameResizeHelper.startThread();
                result.setThumbUri(thumbUri);
                listerner.onExtractStarted(max,thumbUri);
            }

            @Override
            public void onExtractFrame(Bitmap frame,int max, int progress) {
                Log.d(TAG,"onExtractFrame..");
                frameResizeHelper.addFrame(frame);
                listerner.onExtractFrame(max,progress);
            }

            @Override
            public void onExtractFinished() {
                Log.d(TAG,"onExtractFinished..");
                frameResizeHelper.finish();
            }
        });
    }
    public void startExtract(){
        try{
            frameExtractor.ready();
            frameExtractor.extract();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
