package com.weather.lock.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.weather.lock.R;
import com.weather.lock.SettingActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xlc on 2017/2/28.
 */
public class NotificationUtil {

    private static NotificationUtil instance = null;

    private NotificationManager notificationManager = null;

    private static final String TAG = "tool";

    private final int notid = TAG.hashCode();

    private Context mContext;

    public static NotificationUtil getInstance(Context context) {
        if (instance == null)
            instance = new NotificationUtil(context);
        return instance;
    }


    private NotificationUtil(Context context) {

        this.mContext = context;

        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Notification updateNotification(int icon_id, String location, String current_temp, String temp_text) {

        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(mContext);

        noteBuilder.setSmallIcon(icon_id);

        Intent intent = new Intent(mContext, SettingActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notid, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);

        remoteViews.setImageViewResource(R.id.not_weather_icon, icon_id);

        remoteViews.setTextViewText(R.id.not_current_temp, current_temp);

        remoteViews.setTextViewText(R.id.not_location, location);

        remoteViews.setTextViewText(R.id.not_temp_h_l, temp_text);

        remoteViews.setTextViewText(R.id.not_update_time,getTime());

        noteBuilder.setContentIntent(pendingIntent);

        noteBuilder.setCategory(Notification.CATEGORY_TRANSPORT);

        noteBuilder.setPriority(Notification.PRIORITY_MAX);

        Notification notification = noteBuilder.build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        notification.contentView = remoteViews;

        notificationManager.notify(notid, notification);

        return notification;

    }

    private String getTime() {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(Util.getDateFormat(mContext));

        Date curDate = new Date(System.currentTimeMillis());//获取当前时间

        return  formatter.format(curDate);
    }


}
