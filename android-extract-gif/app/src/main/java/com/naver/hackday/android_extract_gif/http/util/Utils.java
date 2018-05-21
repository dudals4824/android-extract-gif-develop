package com.naver.hackday.android_extract_gif.http.util;

import com.google.android.gms.common.util.IOUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class Utils {
    public static byte[] fileToByte(File file) {
        try {
            return IOUtils.toByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }
}
