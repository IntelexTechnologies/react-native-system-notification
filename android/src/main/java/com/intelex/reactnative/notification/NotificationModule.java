package com.intelex.reactnative.notification;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import com.intelex.reactnative.notification.NotificationManager;
import com.intelex.reactnative.notification.Notification;
import com.intelex.reactnative.notification.NotificationAttributes;
import com.intelex.reactnative.notification.NotificationEventReceiver;

import java.util.ArrayList;
import android.util.Log;

/**
 * Main RN module
 */
public class NotificationModule extends ReactContextBaseJavaModule {
    final static String PREFERENCES_KEY = "ReactNativeSystemNotification";
    public Context mContext = null;
    public NotificationManager mNotificationManager = null;

    @Override
    public String getName() {
        return "NotificationModule";
    }

    /**
     * Constructor
     */
    public NotificationModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.mContext = reactContext;
        this.mNotificationManager = (NotificationManager) new NotificationManager(reactContext);

        listenNotificationEvent();
    }

    /**
     * Create or update a notification
     */
    @ReactMethod
    public void rnSend(
        Integer notificationID,
        ReadableMap notificationAttributes,
        Callback errorCallback,
        Callback successCallback
    ) {
        try {
            NotificationAttributes attrs = getNotificationAttributesFromReadableMap(notificationAttributes);
            Notification notification = mNotificationManager.createOrUpdate(notificationID, attrs);

            successCallback.invoke(notification.getAttributes().asReadableMap());

        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
            Log.e("ReactSystemNotification", "NotificationModule: rnSend Error: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Delete notification by id
     */
    @ReactMethod
    public void rnDelete(
        int notificationID,
        Callback errorCallback,
        Callback successCallback
    ) {
        try {
            Notification notification = mNotificationManager.delete(notificationID);

            successCallback.invoke(notification.getAttributes().asReadableMap());

        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
            Log.e("ReactSystemNotification", "NotificationModule: rDelete Error: " + Log.getStackTraceString(e));
        }
    }

    @ReactMethod
    public void rnGetAppName(
        Callback errorCallback,
        Callback successCallback
    ) {
        try {
            int stringId = getReactApplicationContext().getApplicationInfo().labelRes;
            successCallback.invoke(getReactApplicationContext().getString(stringId));

        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
            Log.e("ReactSystemNotification", "NotificationModule: rnGetAppName Error: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Emit JavaScript events
     */
    private void sendEvent(
        String eventName,
        Object params
    ) {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);

        Log.i("ReactSystemNotification", "NotificationModule: sendEvent (to JS): " + eventName);
    }

    private NotificationAttributes getNotificationAttributesFromReadableMap(
        ReadableMap readableMap
    ) {
        NotificationAttributes notificationAttributes = new NotificationAttributes();

        notificationAttributes.loadFromReadableMap(readableMap);

        return notificationAttributes;
    }

    private void listenNotificationEvent() {
        IntentFilter intentFilter = new IntentFilter("NotificationEvent");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle extras = intent.getExtras();

                WritableMap params = Arguments.createMap();
                params.putInt("notificationID", extras.getInt(NotificationEventReceiver.NOTIFICATION_ID));
                params.putString("action", extras.getString(NotificationEventReceiver.ACTION));
                params.putString("payload", extras.getString(NotificationEventReceiver.PAYLOAD));

                sendEvent("sysModuleNotificationClick", params);
            }
        }, intentFilter);
    }

}
