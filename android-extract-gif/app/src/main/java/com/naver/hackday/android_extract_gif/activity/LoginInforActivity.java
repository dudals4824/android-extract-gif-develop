package com.naver.hackday.android_extract_gif.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.http.activity.NaverBlogUploadActivity;
import com.naver.hackday.android_extract_gif.http.activity.TwitterPostUploadActivity;
import com.naver.hackday.android_extract_gif.http.listener.LogoutButtonClick;
import com.naver.hackday.android_extract_gif.http.model.LoginInforItem;
import com.naver.hackday.android_extract_gif.recyclerViewObject.LoginInforListAdapter;

import java.util.ArrayList;

public class LoginInforActivity extends Activity {
    private RecyclerView loginInforList;
    private LinearLayoutManager linearLayoutManager;
    private final int NAVER_LOGOUT = 0;
    private final int TWITTER_LOGOUT = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_information_view);

        initView();
    }

    public void initView() {
        ArrayList<LoginInforItem> loginInforItems = new ArrayList<>();
        loginInforItems.add(new LoginInforItem("네이버", NaverBlogUploadActivity.getLoginState()));
        loginInforItems.add(new LoginInforItem("트위터", TwitterPostUploadActivity.getTwitterState()));
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        loginInforList = (RecyclerView) findViewById(R.id.login_infor_list);
        loginInforList.setHasFixedSize(true);
        loginInforList.setLayoutManager(linearLayoutManager);
        loginInforList.setAdapter(new LoginInforListAdapter(loginInforItems, logoutButtonClick));
    }

    LogoutButtonClick logoutButtonClick = new LogoutButtonClick() {
        @Override
        public void logoutButtonClick(int position) {
            switch (position) {
                case NAVER_LOGOUT:
//                    Toast.makeText(getApplicationContext(), "NaverLogout", Toast.LENGTH_SHORT).show();
                    Intent naverLogout = new Intent(getApplicationContext(), NaverBlogUploadActivity.class);
                    naverLogout.putExtra("logout", true);
                    startActivity(naverLogout);
                    break;
                case TWITTER_LOGOUT:
//                    Toast.makeText(getApplicationContext(), "TwitterLogout", Toast.LENGTH_SHORT).show();
                    Intent twitterLogout = new Intent(getApplicationContext(), TwitterPostUploadActivity.class);
                    twitterLogout.putExtra("logout", true);
                    startActivity(twitterLogout);
                    break;
            }
        }
    };
}
