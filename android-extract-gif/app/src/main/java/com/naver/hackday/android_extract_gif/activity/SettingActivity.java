package com.naver.hackday.android_extract_gif.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.naver.hackday.android_extract_gif.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_view);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.setting_login_information)
    public void onClick() {
        startActivity(new Intent(this, LoginInforActivity.class));
    }
}
