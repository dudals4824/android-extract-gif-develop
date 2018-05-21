package com.naver.hackday.android_extract_gif.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.database.GIFDataBase;
import com.naver.hackday.android_extract_gif.database.GifEntry;
import com.naver.hackday.android_extract_gif.http.activity.DialogActivity;
import com.naver.hackday.android_extract_gif.http.context.GlobalContext;
import com.naver.hackday.android_extract_gif.recyclerViewObject.GIFAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 1;
    public static Context CONTEXT;

    private LinearLayoutManager mGridLayoutManager;
    private RecyclerView mGIFList;
    private GIFAdapter mGIFAdapter;
    private GIFDataBase mDB;

    private List<GifEntry> gifList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CONTEXT=this;
        requestPermission();
        init();
    }

    private void init() {
        mDB = GIFDataBase.getInstance(getApplicationContext());
        mGIFList = (RecyclerView) findViewById(R.id.rv_gif_horizontal_list);
        mGridLayoutManager = new LinearLayoutManager(getApplicationContext());
        mGIFAdapter = new GIFAdapter(gifList);
        mGIFList.setLayoutManager(mGridLayoutManager);
        mGIFList.setItemAnimator(new DefaultItemAnimator());
        mGIFList.setAdapter(mGIFAdapter);
    }


    public void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGIFAdapter.setGifs(mDB.gifDao().getAll());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = MainActivity.this;
        int selectedItem = item.getItemId();

        switch (selectedItem) {
            case R.id.menu_select_video:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
                Toast.makeText(context, "select Video!", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedVideoUri = data.getData();
                Intent intent = new Intent(this, ConvertActivity.class);
                intent.putExtra("videoUri", selectedVideoUri.toString());
                startActivity(intent);
            }
        }
    }
}
