package com.naver.hackday.android_extract_gif.extractor;

/**
 * Created by hanseungbeom on 2018. 5. 15..
 */


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.naver.hackday.android_extract_gif.R;
import com.naver.hackday.android_extract_gif.activity.MainActivity;
import com.naver.hackday.android_extract_gif.database.GifEntry;

import java.util.HashMap;

/**
 * Utility class for creating gif notifications
 */
public class GifNotificationUtils {

    private static final int GIF_EXTRACTION_PENDING_INTENT_ID = 1;
    private static final String GIF_EXTRACTION_NOTIFICATION_CHANNEL_ID = "extraction_notification_channel";

    private static final int AVAILABLE_NUM_OF_ID = 10;
    public static final int HAS_NO_EMPTY_ID = -1;


    public static HashMap<Integer, NotificationCompat.Builder> getInstance() {
        return builderMap.INSTANCE;
    }

    //LazyHolder singletone
    private static class builderMap {
        private static final HashMap<Integer, NotificationCompat.Builder> INSTANCE = new HashMap<>();
    }

    public static void registerNotification(final Context context, final int id) {
        GifEventPublisher.INSTANCE.addListener(new GifExtractManager.GifExtractManagerListerner() {
            @Override
            public void onExtractStarted(int max, Uri thumbUri) {
                initNotification(context, id, max, thumbUri);
            }

            @Override
            public void onExtractFrame(int max, int progress) {
                updateNotification(context, id, max, progress);
            }

            @Override
            public void onEncodeStarted() {
                setEncodingNotification(context, id);

            }

            @Override
            public void onEncodeFinished() {
            }

            @Override
            public void onExtractFinished(GifEntry gifEntry) {
                finishGifExtraction(context, id, gifEntry);
            }
        });
    }


    public static int getEmptyId() {
        HashMap<Integer, NotificationCompat.Builder> map = getInstance();
        for (int id = 1; id <= AVAILABLE_NUM_OF_ID; id++) {
            if (!map.containsKey(id))
                return id;
        }
        return HAS_NO_EMPTY_ID;
    }

    public static NotificationCompat.Builder getBuilder(Context context, int id) {
        HashMap<Integer, NotificationCompat.Builder> map = getInstance();
        if (!map.containsKey(id))
            map.put(id, new NotificationCompat.Builder(context, GIF_EXTRACTION_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_gif)
                    .setContentTitle(context.getString(R.string.gif_extraction_notification_extracting))
                    .setContentText(context.getString(R.string.gif_extraction_notification_extracting_ready))
            );

        return map.get(id);
    }

    public static void initNotification(Context context, int id, int max, Uri thumbUri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    GIF_EXTRACTION_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            getManagerAPI24(context).createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = getBuilder(context, id)
                .setLargeIcon(largeIcon(context, thumbUri))
                .setProgress(max, 0, false)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setOnlyAlertOnce(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        getManager(context).notify(id, builder.build());
    }


    public static void updateNotification(Context context, int id, int max, int progress) {
        NotificationCompat.Builder builder = getBuilder(context, id);
        builder
                .setProgress(max, progress, false)
                .setContentText(String.format(context.getString(R.string.gif_extraction_notification_extracting_info), progress, max));

        getManager(context).notify(id, builder.build());
    }

    public static void setEncodingNotification(Context context, int id) {
        NotificationCompat.Builder builder = getBuilder(context, id);
        builder
                .setContentTitle(context.getString(R.string.gif_extraction_notification_encoding))
                .setContentText(context.getString(R.string.gif_extraction_notification_encoding_info))
                .setProgress(0, 0, true);

        getManager(context).notify(id, builder.build());
    }


    public static void finishGifExtraction(Context context, int id, GifEntry gifEntry) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GIF_EXTRACTION_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_gif)
                .setLargeIcon(largeIcon(context, gifEntry.getThumbUri()))
                .setContentTitle(context.getString(R.string.gif_extraction_notification_title))
                .setContentText(context.getString(R.string.gif_extraction_notification_body))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context, gifEntry.getGifUri()))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        getManager(context).notify(id, builder.build());
        clearNotification(id);
    }

    public static void clearNotification(int id) {
        if (getInstance().containsKey(id))
            getInstance().remove(id);
    }

    public static NotificationManagerCompat getManager(Context context) {
        return NotificationManagerCompat.from(context);
    }

    @TargetApi(24)
    public static NotificationManager getManagerAPI24(Context context) {
        return context.getSystemService(NotificationManager.class);
    }

    public static Notification getNotification(Context context, int id){
        return getBuilder(context,id).build();
    }


    private static PendingIntent contentIntent(Context context, Uri gifUri) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.putExtra(ExtractorUtils.GIF_URI_EXTRA, gifUri.toString());
        return PendingIntent.getActivity(
                context,
                GIF_EXTRACTION_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context, Uri imageUri) {
        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return largeIcon;
    }
}