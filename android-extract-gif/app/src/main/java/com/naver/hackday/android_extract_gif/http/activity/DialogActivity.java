package com.naver.hackday.android_extract_gif.http.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.naver.hackday.android_extract_gif.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogActivity extends Activity {
    private String gifPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        ButterKnife.bind(this);

        gifPath = getIntent().getStringExtra("imageURI");
    }

    @OnClick({R.id.upload_naver_blog, R.id.upload_google_drive, R.id.upload_twitter_post, R.id.share_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upload_naver_blog:
                Intent naverBlog = new Intent(this, NaverBlogUploadActivity.class);
                naverBlog.putExtra("image", gifPath);
                startActivity(naverBlog);
                break;
            case R.id.upload_google_drive:
                Intent googleDrive = new Intent(this, GoogleDriveUploadActivity.class);
                googleDrive.putExtra("image", gifPath);
                startActivity(googleDrive);
                break;
            case R.id.upload_twitter_post:
                Intent twitterPost = new Intent(this, TwitterPostUploadActivity.class);
                twitterPost.putExtra("image", gifPath);
                startActivity(twitterPost);
                break;
            case R.id.share_cancel:
                finish();
                break;
        }
        finish();
    }
}
