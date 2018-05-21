package com.naver.hackday.android_extract_gif;

/**
 * Created by hanseungbeom on 2018. 5. 10..
 */

public class FFmpegUtils {

    static {
        System.loadLibrary("ffmpeg_wrapper");
    }

    public static native int runFFmpeg(String fps, String filepath , String outputPath);
}



