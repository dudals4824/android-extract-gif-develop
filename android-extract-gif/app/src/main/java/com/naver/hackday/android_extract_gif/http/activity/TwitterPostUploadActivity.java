package com.naver.hackday.android_extract_gif.http.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.RealPathUtil;
import com.naver.hackday.android_extract_gif.http.context.GlobalContext;
import com.naver.hackday.android_extract_gif.http.util.Utils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class TwitterPostUploadActivity extends Activity {
    private TwitterAuthClient twitterAuthClient;
    private byte[] imageFile;
    private String token, secret;
    private EditText contentEdit;
    private static boolean isLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(getApplicationContext());
        setContentView(R.layout.twitter_post);

        initView();

        twitterAuthClient = new TwitterAuthClient();
        twitterAuthClient.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.i("TwitterLogin", "Success");
                TwitterAuthToken authToken = result.data.getAuthToken();
                token = authToken.token;
                secret = authToken.secret;
                Log.i("token", token);
                Log.i("secret", secret);
                isLogin = true;
            }

            @Override
            public void failure(TwitterException exception) {
                Log.i("TwitterLogin", "failure");
            }
        });
    }

    private void initView() {
        ButterKnife.bind(this);
        contentEdit = (EditText) findViewById(R.id.twitter_post_content_edit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == twitterAuthClient.getRequestCode()) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }

        if (getIntent().getBooleanExtra("logout", false)) {
            twitterAuthClient.cancelAuthorize();
            isLogin = false;
            finish();
        } else {
            File file = new File(RealPathUtil.getRealPath(GlobalContext.getAppContext(), Uri.parse(getIntent().getStringExtra("image"))));
            imageFile = Utils.fileToByte(file);
        }
    }

    @OnClick(R.id.twitter_post_send_button)
    public void onClick() {
        if (contentEdit.getText() == null || contentEdit.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "내용을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        final StatusesService statusesService = twitterApiClient.getStatusesService();
        MediaService mediaService = twitterApiClient.getMediaService();

        String encodedImage = Base64.encodeToString(imageFile, Base64.DEFAULT);
//            Log.i("encodedImage", encodedImage);

        RequestBody media = RequestBody.create(MediaType.parse("image/gif"), imageFile);
        RequestBody mediaData = RequestBody.create(MediaType.parse("text/plain"), encodedImage);

        Call<Media> call = mediaService.upload(media, mediaData, null);
        call.enqueue(new Callback<Media>() {
            @Override
            public void success(Result<Media> result) {
                Log.i("success", String.valueOf(result.response.raw()));
                Log.i("success", result.data.mediaIdString);
                Call<Tweet> call = statusesService.update(contentEdit.getText().toString().trim(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        result.data.mediaIdString);
                call.enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        //Do something with result
                        Log.i("twitter", String.valueOf(result.response.raw()));
                    }

                    public void failure(TwitterException exception) {
                        //Do something on failure
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                Log.i("failure", "failure");
                Log.i("failure", exception.getMessage());
            }
        });

        finish();
    }

    public static boolean getTwitterState() {
        return isLogin;
    }
}
