package com.naver.hackday.android_extract_gif.http.provider;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.naver.hackday.android_extract_gif.http.util.Utils.createOkHttpClient;

public class GoogleUploadManager {
    private final String GOOGLE_BASE_URL = "https://www.googleapis.com/";
    private Retrofit googleClient;
    private APIInterface googleUploadService;

    public GoogleUploadManager() {
        googleClient = new Retrofit.Builder()
                .baseUrl(GOOGLE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(createOkHttpClient())
                .build();

        googleUploadService = googleClient.create(APIInterface.class);
    }

    private static class Singleton {
        private static final GoogleUploadManager INSTANCE = new GoogleUploadManager();
    }

    public static GoogleUploadManager getInstance() {
        return GoogleUploadManager.Singleton.INSTANCE;
    }

    public void googleDriveUpload(String token, byte[] file, final HttpCallback callback) {
        String header = "Bearer " + token;
        RequestBody image = RequestBody.create(MediaType.parse("image/gif"), file);

        Call<JSONObject> call = googleUploadService.googleDriveUpload(header, image);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                Log.i("googleDriveUpload", String.valueOf(response.raw()));
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.i("googleDriveUpload", t.getMessage());
                callback.onFail();
            }
        });
    }
}
