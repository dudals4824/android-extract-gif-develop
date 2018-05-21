package com.naver.hackday.android_extract_gif.activity;

import com.naver.hackday.android_extract_gif.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.naver.hackday.android_extract_gif.database.GIFDataBase;
import com.naver.hackday.android_extract_gif.database.GifEntry;
import com.naver.hackday.android_extract_gif.extractor.GifExtractorIntentService;
import com.naver.hackday.android_extract_gif.player.Utils;

import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_END;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_FPS;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_START;
import static com.naver.hackday.android_extract_gif.extractor.ExtractorUtils.GIF_EXTRACT_VIDEO_URI;

import org.florescu.android.rangeseekbar.RangeSeekBar;

public class ConvertActivity extends Activity{

    private GIFDataBase mDB;
    private TextView mTvStartTime, mTvEndTime;
    private Button mBtnConvert;
    private MediaSource mOrigin;
    private SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private RangeSeekBar<Long> mRangeSeekBar;
    private Uri videoUri;

    private boolean flag;
    private long duration;
    private long minValueTmp = 0;
    private long maxValueTmp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_range_select);

        Intent intent = getIntent();
        final String mVideoUri = intent.getStringExtra("videoUri");
        videoUri = Uri.parse(mVideoUri);

        init();
        setVideo(videoUri);

        mRangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                mTvStartTime.setText(minValue.toString());
                mTvEndTime.setText(maxValue.toString());
                if(minValueTmp == minValue && maxValueTmp != maxValue){
                    maxValueTmp = maxValue;
                    mPlayer.seekTo(maxValue);
                }
                else if(minValueTmp != minValue && maxValueTmp == maxValue){
                    minValueTmp = minValue;
                    mPlayer.seekTo(minValue);
                }
                else if(minValueTmp == 0 && maxValueTmp == 0){
                    minValueTmp = minValue;
                    maxValueTmp = maxValue;
                    mPlayer.seekTo(minValue);
                }
            }
        });
        mBtnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Convert
                Bundle bundle = getExtractInfo(mVideoUri);
                Intent intent = new Intent(getApplicationContext(), GifExtractorIntentService.class);
                intent.putExtras(bundle);
                startService(intent);

                Uri gifUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.
                        DIRECTORY_PICTURES).toString()+"/KakaoTalk"+"/1526183238696.gif");
//                Uri thumbUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.
//                        DIRECTORY_PICTURES).toString()+"/KakaoTalk"+"/1526183238696.gif");
//                Long createdTime = (long)0;
//
//                GifEntry gifEntry = new GifEntry(gifUri,thumbUri,createdTime);
//
//                Log.d("Value Check : ", gifEntry.getId()+"");
//                Log.d("Value Check : ", gifEntry.getCreatedTime()+"");
//                Log.d("Value Check : ", gifEntry.getGifUri().toString());
//                Log.d("Value Check : ", gifEntry.getThumbUri().toString());
//                mDB.gifDao().insertGIF(gifEntry);

                Intent i = new Intent(getApplicationContext(), GifActivity.class);
                i.putExtra("gifUri", gifUri.toString());
                startActivity(i);
            }
        });

    }

    @NonNull
    private Bundle getExtractInfo(String mVideoUri) {
        Bundle bundle = new Bundle();
        bundle.putInt(GIF_EXTRACT_FPS, 15);
        bundle.putLong(GIF_EXTRACT_END, maxValueTmp);
        bundle.putLong(GIF_EXTRACT_START, minValueTmp);
        bundle.putString(GIF_EXTRACT_VIDEO_URI, mVideoUri);
        return bundle;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ThisISOnStop??", "dflksjdflsdfsdfsdf");

        mPlayer.release();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("ThisIsOnRestart", "ThisisOnRestart");
        setVideo(videoUri);
    }

    private void init() {
        //variable init
        mTvStartTime = (TextView) findViewById(R.id.tv_start_time2);
        mTvEndTime = (TextView) findViewById(R.id.tv_end_time2);
        mBtnConvert = (Button) findViewById(R.id.btn_convert2);
        mBtnConvert.setEnabled(false);

        mDB = GIFDataBase.getInstance(getApplicationContext());
        flag = false;

        //Player Init
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, Utils.getDefaultTrackSelector());
        mPlayerView = (PlayerView) findViewById(R.id.epv_player_view2);
        mPlayerView.setPlayer(mPlayer);
        mRangeSeekBar = (RangeSeekBar<Long>)findViewById(R.id.range_seekbar2);
    }


    public void setVideo(Uri videoUri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), new DefaultBandwidthMeter());

        mOrigin = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoUri);
        mPlayer.prepare(mOrigin);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(playbackState == Player.STATE_READY && !flag){
                    flag = true;
                    duration = mPlayer.getDuration();
                    mBtnConvert.setEnabled(true);

                    mRangeSeekBar.setRangeValues((long)0, duration);

                }
                flag = false;
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        mPlayer.setPlayWhenReady(false);
    }

}
