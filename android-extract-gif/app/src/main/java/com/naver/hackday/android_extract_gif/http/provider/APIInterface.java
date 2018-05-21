package com.naver.hackday.android_extract_gif.http.provider;

import com.naver.hackday.android_extract_gif.http.model.HttpResponse;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {
    @GET("/blog/listCategory.json")
    Call<HttpResponse> naverBlogCategory(@Header("Authorization") String authorization);

    @Multipart
    @POST("/blog/writePost.json")
    Call<JSONObject> naverBlogSendPost(@Header("Authorization") String authorization,
                                       @Part("title") RequestBody title,
                                       @Part("contents") RequestBody contents,
                                       @Part("categoryNo") int categoryNo,
                                       @Part MultipartBody.Part image
    );

    @POST("/upload/drive/v3/files?uploadType=media")
    Call<JSONObject> googleDriveUpload(@Header("Authorization") String authorization,
                                       @Body RequestBody image);

    @FormUrlEncoded
    @POST("/1.1/statuses/update.json")
    Call<JSONObject> twitterTweet(@Header("Authorization") String authorization,
                                  @Field("status") String status,
                                  @Field("in_reply_to_status_id") Long inReplyToStatusId,
                                  @Field("possibly_sensitive") Boolean possiblySensitive,
                                  @Field("lat") Double latitude,
                                  @Field("long") Double longitude,
                                  @Field("place_id") String placeId,
                                  @Field("display_coordinates") Boolean displayCoordinates,
                                  @Field("trim_user") Boolean trimUser,
                                  @Field("media_ids") String mediaIds);

    @FormUrlEncoded
    @POST("oauth2/token")
    Call<Object> getToken(@Header("Authorization") String authorization,
                          @Field("grant_type") String grant_type);
}
