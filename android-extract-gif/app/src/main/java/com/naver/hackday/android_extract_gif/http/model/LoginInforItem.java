package com.naver.hackday.android_extract_gif.http.model;

public class LoginInforItem {
    private String title;
    private boolean isLogin;

    public LoginInforItem(String title, boolean isLogin) {
        this.title = title;
        this.isLogin = isLogin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }
}
