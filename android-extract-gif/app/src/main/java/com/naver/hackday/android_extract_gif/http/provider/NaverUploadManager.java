package com.naver.hackday.android_extract_gif.http.provider;

import android.util.Log;

import com.naver.hackday.android_extract_gif.http.model.HttpResponse;
import com.naver.hackday.android_extract_gif.http.util.Utils;

import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NaverUploadManager {
    private final String NAVER_BASE_URL = "https://openapi.naver.com/";
    private Retrofit naverClient;
    private APIInterface naverUploadService;

    public NaverUploadManager() {
        naverClient = new Retrofit.Builder()
                .baseUrl(NAVER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Utils.createOkHttpClient())
                .build();

        naverUploadService = naverClient.create(APIInterface.class);
    }

    private static class Singleton {
        private static final NaverUploadManager INSTANCE = new NaverUploadManager();
    }

    public static NaverUploadManager getInstance() {
        return Singleton.INSTANCE;
    }

    public void getNaverBlogCategory(String token, final HttpCallback callback) {
        String header = "Bearer " + token;

        Call<HttpResponse> call = naverUploadService.naverBlogCategory(header);
        call.enqueue(new Callback<HttpResponse>() {
            @Override
            public void onResponse(Call<HttpResponse> call, Response<HttpResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<HttpResponse> call, Throwable t) {
                Log.i("getNaverBlogCategory", t.getMessage());
                callback.onFail();
            }
        });
    }

    public void naverBlogPost(String token, String title, String content, int categoryNo, File file, final HttpCallback callback) {
        APIInterface service = naverClient.create(APIInterface.class);

        String header = "Bearer " + token;
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content + "<img src='#0' />");
        RequestBody image = RequestBody.create(MediaType.parse("image/gif"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), image);

        Call<JSONObject> call = service.naverBlogSendPost(header, titleBody, contentBody, categoryNo, part);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                Log.i("naverBlogPost", String.valueOf(response.raw()));
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.i("naverBlogPost", t.getMessage());
                callback.onFail();
            }
        });
    }
}
