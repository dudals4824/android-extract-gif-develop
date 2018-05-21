package com.naver.hackday.android_extract_gif.http.provider;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TwitterUploadManager {
    private final String TWITTER_BASE_URL = "https://api.twitter.com/";

    private Retrofit twitterClient;

    private static class Singleton {
        private static final TwitterUploadManager INSTANCE = new TwitterUploadManager();
    }

    public static TwitterUploadManager getInstance() {
        return TwitterUploadManager.Singleton.INSTANCE;
    }

    public void twitterGetToken(String token, final HttpCallback callback) {
        APIInterface service = twitterClient.create(APIInterface.class);

        Call<Object> call = service.getToken(token, "client_credentials");
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.i("twitterGetToken", String.valueOf(response.body()));
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                JsonObject jsonObject = gson.toJsonTree(response.body()).getAsJsonObject();
                String token = gson.fromJson(jsonObject.get("access_token"), String.class);
                callback.onSuccess(token);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.i("twitterGetToken", t.getMessage());
                callback.onFail();
            }
        });
    }
}
