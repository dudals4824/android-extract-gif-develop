package com.naver.hackday.android_extract_gif.extractor;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.naver.hackday.android_extract_gif.RealPathUtil;
import com.naver.hackday.android_extract_gif.database.GifEntry;
import com.naver.hackday.android_extract_gif.player.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_END;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_FPS;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_START;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_VIDEO_URI;

/**
 * Created by hanseungbeom on 2018. 5. 17..
 */

public class FrameExtractor {
    public static final String TAG = FrameExtractor.class.getSimpleName();

    private final String VIDEO = "video/";
    private final int TIME_OUT_US = 10000;

    //extractInfo
    private Bundle mExtractInfo;

    //context
    private Context mContext;

    //about gif encoder
    private ArrayList<Long> mFramePos;


    //about mediacodec
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    //for pick range
    private long mAddRate;

    //listener
    private EventListener mEventListener;

    public interface EventListener{
        public void onExtractStarted(int max,Uri thumbUri);
        public void onExtractFrame(Bitmap frame,int max, int progress);
        public void onExtractFinished();
    }


    public FrameExtractor(Context context, Bundle extractInfo){
        mContext = context;
        mExtractInfo = extractInfo;
    }
    public void setOnEventListener(EventListener eventListener){
        mEventListener = eventListener;
    }


    public void ready() throws Exception {

        Uri videoUri = Uri.parse(mExtractInfo.getString(GIF_EXTRACT_VIDEO_URI));
        long startPos = mExtractInfo.getLong(GIF_EXTRACT_START);
        long endPos = mExtractInfo.getLong(GIF_EXTRACT_END);
        int fps = mExtractInfo.getInt(GIF_EXTRACT_FPS);

        /* setting MediaCodec */
        mExtractor = new MediaExtractor();
        String path = RealPathUtil.getRealPath(mContext, videoUri);

        mExtractor.setDataSource(path);

            /* get viedo track */
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(VIDEO)) {
                mExtractor.selectTrack(i);
                mDecoder = MediaCodec.createDecoderByType(mime);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

                try {
                    Log.d(TAG, "format : " + format);
                    mDecoder.configure(format, null, null, 0);

                } catch (IllegalStateException e) {
                    Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                }
                mDecoder.start();
                break;
            }
        }


        long totalTimeMs = endPos - startPos;

        //calculate total count of frames we need and delay between frames.
        int neededFrame = (int) (fps * (ExtractorUtils.getSecFromMs(totalTimeMs)));
        long addRate = totalTimeMs / neededFrame;
        mAddRate = addRate;

        mFramePos = new ArrayList<>();

        //calculate frame Position needed
        for (long pos = startPos; pos < endPos; pos += addRate) {
            mFramePos.add(pos);
        }

        //move to the start position.
        mExtractor.seekTo(startPos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

    }

    public void extract() {

        long endPos = mExtractInfo.getLong(GIF_EXTRACT_END);
        int fps = mExtractInfo.getInt(GIF_EXTRACT_FPS);

        GifEntry gifEntry = null;
        try {
            Log.d(TAG, "startExtract..");


            long start = System.currentTimeMillis();

            /* for fileName */
            long createdTime = start;

            //extract with mediacodec and save Frame
            Uri thumbUri = ExtractAndSaveFrames(createdTime);

            long end = System.currentTimeMillis();
            Log.d(TAG, "total frame size:" + mFramePos.size());
            Log.d(TAG, "total time :" + String.valueOf(end - start));
            Log.d(TAG, "1 frame extraction = :" + ((double) (end - start) / mFramePos.size()));

            //encode bitmap frames
            Log.d(TAG, "encodeGif..");

            //start encode
            String imagePath = ExtractorUtils.getImagePath(mContext,createdTime);
            String outputPath = ExtractorUtils.getOutputPath(createdTime);

            //FFmpegUtils.runFFmpeg(String.valueOf(fps), imagePath, outputPath);

     /*         Log.d(TAG, "encodeGif Finished..");

            int deletedNum = ExtractorUtils.deleteDir(ExtractorUtils.getDeletePath(mContext,createdTime));

            Log.d(TAG, "frame image files Deleted :"+ deletedNum);

            Uri gifUri = Uri.fromFile(new File(outputPath));
            gifEntry = new GifEntry(gifUri,thumbUri,createdTime);*/
            mEventListener.onExtractFinished();


        } catch (Exception e) {
            Log.e(TAG, "extract failed..");
            e.printStackTrace();
        }

        mDecoder.stop();
        mDecoder.release();
        mExtractor.release();
        //Utils.addImageToGallery(mContext,gifEntry.getGifUri());

        //return gifEntry;
    }

    public Uri ExtractAndSaveFrames(long createdTime) throws Exception {
        int fps = mExtractInfo.getInt(GIF_EXTRACT_FPS);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GifEncoder gifEncoder = new GifEncoder();
        gifEncoder.setDelay(ExtractorUtils.getDelayOfFrame(fps));
        gifEncoder.start(baos);

        long endPos = mExtractInfo.getLong(GIF_EXTRACT_END);

        //set file name createdTime for the unique name.
        int nextPosPointer = 0;
        boolean isInput = true;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean isThumbnailSaved = false;
        Uri thumbUri = null;

        while (nextPosPointer < mFramePos.size()) {
            if (isInput) {
                int inputIndex = mDecoder.dequeueInputBuffer(TIME_OUT_US);

                /* put data to inputBuffer using MediaExtractor */
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputIndex);

                    int sampleSize = mExtractor.readSampleData(inputBuffer, 0);

                    if (mExtractor.advance() && sampleSize > 0) {
                        mDecoder.queueInputBuffer(inputIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);

                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isInput = false;
                    }
                }
            }

            /* get data from outputBuffer */
            int outIndex = mDecoder.dequeueOutputBuffer(info, TIME_OUT_US);
            if (outIndex >= 0) {

                long presentationTimeUs = info.presentationTimeUs;
                //Log.d(TAG, "presentationTimeUs:" + String.valueOf(presentationTimeUs));

                /* break when presentationTimeUs > end Position */
                if (presentationTimeUs > ExtractorUtils.msToUs(endPos)) {
                    break;
                }

                ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outIndex);
                MediaFormat format = mDecoder.getOutputFormat(outIndex);

                try {
                    long nextPos = mFramePos.get(nextPosPointer);
                    if (ExtractorUtils.isRange(nextPos, presentationTimeUs,mAddRate)) {
                        //this is the TargetPossibleZone, extract frame at this position.

                        Image outputImage = mDecoder.getOutputImage(outIndex);
                        Bitmap frame = ExtractorUtils.outputBufferToFrame(outputImage, format);
                        outputImage.close();


                        if(!isThumbnailSaved) {
                            thumbUri = ExtractorUtils.saveThumbnail(mContext, frame, String.valueOf(createdTime));
                            mEventListener.onExtractStarted(mFramePos.size(),thumbUri);
                            isThumbnailSaved = true;
                        }

                        gifEncoder.addFrame(frame);
                        //ExtractorUtils.saveBitmaptoJpeg(mContext, frame, String.valueOf(createdTime), nextPosPointer);
                        mEventListener.onExtractFrame(frame,mFramePos.size(),(nextPosPointer+1));
                        nextPosPointer++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mDecoder.releaseOutputBuffer(outIndex, false);

            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mDecoder.getOutputFormat();
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        return thumbUri;
    }

}
