package com.intelex.reactnative.notification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.net.Uri;
import static android.support.v7.app.NotificationCompat.InboxStyle;

import java.lang.System;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import com.intelex.reactnative.notification.NotificationAttributes;
import com.intelex.reactnative.notification.NotificationEventReceiver;

import android.util.Base64;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.graphics.Color;

/**
 * A Wrapper around system notification class
 */
public class Notification {
    private Context context;
    private int id;
    private NotificationAttributes attributes;

    /**
     * Constructor
     */
    public Notification(Context context, int id, @Nullable NotificationAttributes attributes) {
        this.context = context;
        this.id = id;
        this.attributes = attributes;

        initChannels(context);
    }

    /**
     * Public context getter
     */
    public Context getContext() {
        return context;
    }

    /**
     * Public attributes getter
     */
    public NotificationAttributes getAttributes() {
        return attributes;
    }

    /**
     * Create the notification
     */
    public Notification create() {
        show();

        Log.i("ReactSystemNotification", "Notification Created: " + id);

        return this;
    }

    /**
     * Update notification
     */
    public Notification update(NotificationAttributes notificationAttributes) {
        delete();
        attributes = notificationAttributes;
        show();

        return this;
    }

    /**
     * Clear notification
     */
    public Notification clear() {
        getSysNotificationManager().cancel(id);

        Log.i("ReactSystemNotification", "Notification Cleared: " + id);

        return this;
    }

    /**
     * Cancel notification
     */
    public Notification delete() {
        Log.i("ReactSystemNotification", "Delete start: " + id);
        getSysNotificationManager().cancel(id);

        deleteFromPreferences();

        Log.i("ReactSystemNotification", "Notification Deleted: " + id);

        return this;
    }

    /**
     * Create channel
     */
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        if (attributes == null) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = attributes.channelName == null ? attributes.channelId : attributes.channelName;
        Log.i("ReactSystemNotification", "Channel name: " + channelName + " Name from attrs: " + attributes.channelName);
        NotificationChannel channel = new NotificationChannel(attributes.channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        if (attributes.channelDescription != null) channel.setDescription(attributes.channelDescription);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Build notification
     */
    public android.app.Notification build() {
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(context, "default");

        notificationBuilder
            .setContentText(attributes.message)
            .setSmallIcon(context.getResources().getIdentifier(attributes.smallIcon, "mipmap", context.getPackageName()))
            .setAutoCancel(attributes.autoClear)
            .setContentIntent(getContentIntent());
        
        if (attributes.showAppName) {
            if (Build.VERSION.SDK_INT < 26) {
                // should add app name
                Log.i("ReactSystemNotification", "showAppName and need to show app name" + attributes.subject);
                notificationBuilder.setContentTitle(attributes.appName + " " + attributes.subject);
            } else {
                // app name will be shown by OS                
                Log.i("ReactSystemNotification", "showAppName but no need to show app name" + attributes.subject);
                notificationBuilder.setContentTitle(attributes.subject);
            }
        } else {
                Log.i("ReactSystemNotification", "no showAppName" + attributes.subject);
            notificationBuilder.setContentTitle(attributes.subject);
        }

        if (attributes.isOngoing) {
            notificationBuilder.setOngoing(attributes.isOngoing);
        }

        if (attributes.priority != null) {
            notificationBuilder.setPriority(attributes.priority);
        }

        if (attributes.largeIcon != null) {
            int largeIconId = context.getResources().getIdentifier(attributes.largeIcon, "drawable", context.getPackageName());
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), largeIconId);
            notificationBuilder.setLargeIcon(largeIcon);
        }

        if (attributes.group != null) {
          notificationBuilder.setGroup(attributes.group);
          notificationBuilder.setGroupSummary(true);
        }

        if (attributes.inboxStyle) {
            InboxStyle inboxStyle = new InboxStyle();

            if (attributes.inboxStyleBigContentTitle != null){
                inboxStyle.setBigContentTitle(attributes.inboxStyleBigContentTitle);
            }

            if (attributes.inboxStyleSummaryText != null){
                inboxStyle.setSummaryText(attributes.inboxStyleSummaryText);
            }

            if (attributes.inboxStyleLines != null){
                for(int i = 0; i < attributes.inboxStyleLines.size(); i++) {
                    inboxStyle.addLine(Html.fromHtml(attributes.inboxStyleLines.get(i)));
                }
            }

            notificationBuilder.setStyle(inboxStyle);

            Log.i("ReactSystemNotification", "set inbox style");
        } else {
            int defaults = 0;

            if ("default".equals(attributes.sound)) {
                defaults = defaults | android.app.Notification.DEFAULT_SOUND;
            }

            if ("default".equals(attributes.vibrate)) {
                defaults = defaults | android.app.Notification.DEFAULT_VIBRATE;
            }

            if ("default".equals(attributes.lights)) {
                defaults = defaults | android.app.Notification.DEFAULT_LIGHTS;
            }

            notificationBuilder.setDefaults(defaults);
        }

        if (attributes.onlyAlertOnce != null) {
            notificationBuilder.setOnlyAlertOnce(attributes.onlyAlertOnce);
        }

        if (attributes.tickerText != null) {
            notificationBuilder.setTicker(attributes.tickerText);
        }

        if (attributes.when != null) {
            notificationBuilder.setWhen(attributes.when);
            notificationBuilder.setShowWhen(true);
        }

        if (attributes.bigText != null) {
            notificationBuilder
              .setStyle(new NotificationCompat.BigTextStyle()
              .bigText(attributes.bigText));
        } else if (attributes.bigStyleUrlImgage != null && attributes.bigStyleUrlImgage != "") {
            Bitmap bigPicture = null;

            try {
                Log.i("ReactSystemNotification", "start getting image from URL : " + attributes.bigStyleUrlImgage);
                URL url = new URL(attributes.bigStyleUrlImgage);
                bigPicture = BitmapFactory.decodeStream(url.openStream());
                Log.i("ReactSystemNotification", "finishing getting image from URL");
            } catch (Exception e) {
                Log.e("ReactSystemNotification", "Error when getting image from URL" + e.getStackTrace());
            }

            if (bigPicture != null) {
                notificationBuilder
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture));
            }
        } else if (attributes.bigStyleImageBase64 != null) {
            Bitmap bigPicture = null;

            try {
                Log.i("ReactSystemNotification", "start converting bigStyleImageBase64 to bitmap");
                byte[] bitmapAsBytes = Base64.decode(attributes.bigStyleImageBase64.getBytes(), Base64.DEFAULT);
                bigPicture = BitmapFactory.decodeByteArray(bitmapAsBytes, 0, bitmapAsBytes.length);
                Log.i("ReactSystemNotification", "finish converting bigStyleImageBase64 to bitmap");

            } catch (Exception e) {
                Log.e("ReactSystemNotification", "Error when converting base 64 to Bitmap" + e.getStackTrace());
            }

            if (bigPicture != null) {
                notificationBuilder
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture));
            }
        }

        if (attributes.color != null) {
          notificationBuilder.setColor(Color.parseColor(attributes.color));
        }

        if (attributes.subText != null) {
            notificationBuilder.setSubText(attributes.subText);
        }

        if (attributes.progress != null) {
            if (attributes.progress < 0 || attributes.progress > 1000) {
                notificationBuilder.setProgress(1000, 100, true);
            } else {
                notificationBuilder.setProgress(1000, attributes.progress, false);
            }
        }

        if (attributes.number != null) {
            notificationBuilder.setNumber(attributes.number);
        }

        if (attributes.localOnly != null) {
            notificationBuilder.setLocalOnly(attributes.localOnly);
        }

        if (attributes.sound != null) {
            notificationBuilder.setSound(Uri.parse(attributes.sound));
        }

        return notificationBuilder.build();
    }

    /**
     * Show notification
     */
    public void show() {
        getSysNotificationManager().notify(id, build());

        Log.i("ReactSystemNotification", "Notification show:" + id);
    }

    public void saveAttributesToPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        String attributesJSONString = new Gson().toJson(attributes);

        editor.putString(Integer.toString(id), attributesJSONString);

        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }

        Log.i("ReactSystemNotification", "Notification save to pref: " + id + ": " + attributesJSONString);
    }

    public void loadAttributesFromPreferences() {
        String attributesJSONString = getSharedPreferences().getString(Integer.toString(id), null);
        this.attributes = (NotificationAttributes) new Gson().fromJson(attributesJSONString, NotificationAttributes.class);

        Log.i("ReactSystemNotification", "Notification Loaded From Pref: " + id + ": " + attributesJSONString);
    }

    public void deleteFromPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.remove(Integer.toString(id));

        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }

        Log.i("ReactSystemNotification", "Notification Deleted From Pref: " + id);
    }

    private NotificationManager getSysNotificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private SharedPreferences getSharedPreferences () {
        return (SharedPreferences) context.getSharedPreferences(com.intelex.reactnative.notification.NotificationManager.PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(context, NotificationEventReceiver.class);

        intent.putExtra(NotificationEventReceiver.NOTIFICATION_ID, id);
        intent.putExtra(NotificationEventReceiver.ACTION, attributes.action);
        intent.putExtra(NotificationEventReceiver.PAYLOAD, attributes.payload);

        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
