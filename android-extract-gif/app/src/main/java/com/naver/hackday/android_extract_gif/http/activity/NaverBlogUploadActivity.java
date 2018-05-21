package com.naver.hackday.android_extract_gif.http.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.RealPathUtil;
import com.naver.hackday.android_extract_gif.http.context.GlobalContext;
import com.naver.hackday.android_extract_gif.http.model.HttpResponse;
import com.naver.hackday.android_extract_gif.http.provider.NaverUploadManager;
import com.naver.hackday.android_extract_gif.http.util.Constant;
import com.naver.hackday.android_extract_gif.http.provider.HttpCallback;
import com.naver.hackday.android_extract_gif.http.util.Utils;
import com.naver.hackday.android_extract_gif.http.model.Category;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class NaverBlogUploadActivity extends Activity {
    private OAuthLogin mOAuthLoginInstance;
    private String token;
    private byte[] imageFile;
    private File file;
    private EditText titleEdit, contentEdit;
    private static boolean isLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naver_post);

        initView();
    }

    public void initView() {
        ButterKnife.bind(this);
        titleEdit = (EditText) findViewById(R.id.naver_post_title_edit);
        contentEdit = (EditText) findViewById(R.id.naver_post_content_edit);

        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(
                getApplicationContext(),
                Constant.OAUTH_CLIENT_ID,
                Constant.OAUTH_CLIENT_SECRET,
                Constant.OAUTH_CLIENT_NAME
        );
        mOAuthLoginInstance.disableSimpleLoginActivity();

        if (getIntent().getBooleanExtra("logout", false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mOAuthLoginInstance.logoutAndDeleteToken(getApplicationContext());
                }
            }).start();
            finish();
        } else {
            file = new File(RealPathUtil.getRealPath(GlobalContext.getAppContext(), Uri.parse(getIntent().getStringExtra("image"))));
            imageFile = Utils.fileToByte(file);
        }
    }

    @OnClick(R.id.naver_post_send_button)
    public void onClick() {
        if (titleEdit.getText() == null || titleEdit.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "제목을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contentEdit.getText() == null || contentEdit.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "내용을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        mOAuthLoginInstance.startOauthLoginActivity(NaverBlogUploadActivity.this, mOAuthLoginHandler);
    }

    @SuppressLint("HandlerLeak")
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                try {
                    if (OAuthLoginState.OK == mOAuthLoginInstance.getState(getApplicationContext())) {
                        isLogin = true;
                    } else {
                        isLogin = false;
                    }
                    token = mOAuthLoginInstance.getAccessToken(getApplicationContext());
                    Log.i("naverToken", token);
                    NaverUploadManager.getInstance().getNaverBlogCategory(token, new HttpCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            if (o instanceof HttpResponse) {
                                makeDialog(token, (HttpResponse) o);
                            }
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("success", "success");
            } else {
                Log.i("fail", "fail");
            }
        }
    };

    public void makeDialog(String token, HttpResponse category) {
        final ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(NaverBlogUploadActivity.this, android.R.layout.select_dialog_singlechoice);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NaverBlogUploadActivity.this);
        alertBuilder.setTitle("카테고리를 선택하세요.");

        for (int i = 0; i < category.getMessage().getResult().size(); i++) {
            if (category.getMessage().getResult().get(i).getSubCategories().size() == 0) {
                adapter.add(category.getMessage().getResult().get(i));
            } else {
                for (int j = 0; j < category.getMessage().getResult().get(i).getSubCategories().size(); j++) {
                    adapter.add(category.getMessage().getResult().get(i).getSubCategories().get(j));
                }
            }
        }

        alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Category clickedItem = adapter.getItem(id);
                String title = titleEdit.getText().toString().trim();
                String content = contentEdit.getText().toString().trim();
                NaverUploadManager.getInstance().naverBlogPost(token, title, content, clickedItem.getCategoryNo(), file, new HttpCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            }
        });
        alertBuilder.show();
    }

    public static boolean getLoginState() {
        return isLogin;
    }
}
