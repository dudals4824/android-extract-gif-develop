package com.naver.hackday.android_extract_gif.recyclerViewObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.activity.MainActivity;
import com.naver.hackday.android_extract_gif.database.GIFDataBase;
import com.naver.hackday.android_extract_gif.http.activity.DialogActivity;
import com.naver.hackday.android_extract_gif.database.GifEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
public class GIFAdapter extends RecyclerView.Adapter<GIFAdapter.MyViewHolder>{
    private List<GifEntry> gifList;
    private GifDrawable mGifDrawable;
    private GIFDataBase mDB;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private GifImageView mGIFImageView;
        private ImageButton mShareBtn, mDeleteBtn;



        private MyViewHolder(View view){
            super(view);
            Context context = view.getContext();
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            mGIFImageView = view.findViewById(R.id.giv_gif_thumbnail);
            mGIFImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height/3));
            mShareBtn = view.findViewById(R.id.btn_share);
            mShareBtn.setLayoutParams(new LinearLayout.LayoutParams(width/2, LinearLayout.LayoutParams.WRAP_CONTENT));
            mDeleteBtn = view.findViewById(R.id.btn_delete);
            mDeleteBtn.setLayoutParams(new LinearLayout.LayoutParams(width/2, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public GIFAdapter(List<GifEntry> gifList){
            this.gifList = gifList;
    }

    public void setGifs(List<GifEntry> gifEntries) {
        gifList = gifEntries;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gif_item_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GifEntry gif = gifList.get(position);
        //URI 들어가기
        Uri uri = gif.getGifUri();
        File gifFile = new File(uri.toString());
        try{
            mGifDrawable = new GifDrawable(null,uri);
            mGifDrawable.setLoopCount(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        holder.mGIFImageView.setImageDrawable(mGifDrawable);
        holder.mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, DialogActivity.class);
                intent.putExtra("imageURI", uri.toString());
                context.startActivity(intent);
            }
        });
        holder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        Context context = view.getContext();
                        mDB = GIFDataBase.getInstance(context);
                        int pos = holder.getAdapterPosition();
                        List<GifEntry> entries = gifList;
                        mDB.gifDao().deleteGIF(entries.get(pos));
                        ((MainActivity)(MainActivity.CONTEXT)).onResume();
            }
        });
    }

    @Override
    public int getItemCount() {
        return gifList.size();
    }
}
