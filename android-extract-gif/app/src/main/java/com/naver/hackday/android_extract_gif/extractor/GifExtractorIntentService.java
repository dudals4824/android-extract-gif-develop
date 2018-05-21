package com.naver.hackday.android_extract_gif.extractor;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.naver.hackday.android_extract_gif.database.GifEntry;

/**
 * Created by hanseungbeom on 2018. 5. 14..
 */

public class GifExtractorIntentService extends IntentService {

    public static final String TAG = GifExtractorIntentService.class.getSimpleName();

    private GifExtractManager gifExtractManager;

    public GifExtractorIntentService() {
        super("GifExtractorIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        Bundle extractInfo = intent.getExtras();

        int notificationId = GifNotificationUtils.getEmptyId();

        if (notificationId == GifNotificationUtils.HAS_NO_EMPTY_ID)
            return;

        GifNotificationUtils.registerNotification(context,notificationId);

        startForeground(notificationId, GifNotificationUtils.getNotification(context,notificationId));

        gifExtractManager = new GifExtractManager(context, extractInfo);
        gifExtractManager.setListerner(new GifExtractManager.GifExtractManagerListerner() {
            @Override
            public void onExtractStarted(int max, Uri thumbUri) {
                GifEventPublisher.INSTANCE.notifyFirstExtracted(max, thumbUri);
            }

            @Override
            public void onExtractFrame(int max, int progress) {
                GifEventPublisher.INSTANCE.notifyFrameExtracted(max, progress);
            }

            @Override
            public void onEncodeStarted() {
                GifEventPublisher.INSTANCE.notifyEncodeStarted();
            }

            @Override
            public void onEncodeFinished() {
                GifEventPublisher.INSTANCE.notifyEncodeFinished();
            }

            @Override
            public void onExtractFinished(GifEntry gifEntry) {
                GifEventPublisher.INSTANCE.notiyExtractFinished(gifEntry);
                stopForeground(false);
            }
        });

        gifExtractManager.startExtract();

    }
}
