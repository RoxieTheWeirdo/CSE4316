package com.example.fitbite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class Notifications {
    private static final String PREFS_NAME = "notification_prefs";
    private static final String HISTORY_KEY = "notification_history";
    private static final String CHANNEL_ID = "main_channel";
    private static final String CHANNEL_NAME = "General Notifications";
    public static final int AllNotifs = 0;
    public static final int MinimalNotifs = 1;
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("App notifications");

            NotificationManager manager = context.getSystemService(
                    NotificationManager.class
            );
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, int id, String title, String message, int type) {
        LocalSettings settings = new LocalSettings(context);
        String pref = settings.getNotificationMode(); // "Off", "Minimal", "All"

        // Filter by user preference
        if (pref.equals("Off")) {
            return; // never send
        }

        // Correct logic: Minimal users skip AllNotifs, but receive MinimalNotifs
        if (pref.equals("Minimal") && type == AllNotifs) {
            return;
        }

        // Runtime permission check (Android 13+ / TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.appicon_grayscale)
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.appicon // full-color app icon
                ))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Send notification
        NotificationManagerCompat.from(context).notify(id, builder.build());
    }
    public static void showInAppNotification(
            Activity activity,
            String title,
            String message,
            Class<?> activityToOpen,
            long milliDelay
    ) {
        LocalSettings localSettings = new LocalSettings(activity);
        String mode = localSettings.getNotificationMode();
        if (activity == null || mode.equals("Off")) return;

        LayoutInflater inflater = activity.getLayoutInflater();
        saveNotificationToHistory(activity, title, message);
        View notificationView = inflater.inflate(R.layout.inapp_notification, null);

        TextView titleView = notificationView.findViewById(R.id.inAppTitle);
        TextView messageView = notificationView.findViewById(R.id.inAppMessage);
        TextView dismissView = notificationView.findViewById(R.id.inAppDismiss);

        // Set text
        titleView.setText(title);
        messageView.setText(message);

        WindowManager windowManager =
                (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP;
        params.token = activity.getWindow().getDecorView().getWindowToken();
        params.y = 50; // small offset from top

        // Add the view
        try {
            windowManager.addView(notificationView, params);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Root click behavior
        notificationView.setOnClickListener(v -> {
            if (activityToOpen != null) {
                Intent intent = new Intent(activity, activityToOpen);
                activity.startActivity(intent);
            }
            try {
                windowManager.removeView(notificationView);
            } catch (Exception ignored) {}
        });

        // Dismiss text click
        dismissView.setOnClickListener(v -> {
            try {
                windowManager.removeView(notificationView);
            } catch (Exception ignored) {}
        });

        // Auto-dismiss after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                windowManager.removeView(notificationView);
            } catch (Exception ignored) {}
        }, milliDelay);
    }
    public static void saveNotificationToHistory(Context context, String title, String message) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(HISTORY_KEY, null);
        JSONArray array;
        if (jsonString != null) {
            try {
                array = new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                array = new JSONArray();
            }
        } else {
            array = new JSONArray();
        }

        // Create new notification JSON
        JSONObject notif = new JSONObject();
        try {
            notif.put("title", title);
            notif.put("message", message);

            // System time in readable format
            String time = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault())
                    .format(new Date());
            notif.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Insert newest on top
        JSONArray newArray = new JSONArray();
        newArray.put(notif);
        for (int i = 0; i < array.length(); i++) {
            newArray.put(array.optJSONObject(i));
        }

        prefs.edit().putString(HISTORY_KEY, newArray.toString()).apply();
    }

    public static JSONArray getNotificationHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(HISTORY_KEY, null);
        if (jsonString != null) {
            try {
                return new JSONArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONArray();
    }

    public static void removeNotificationFromHistory(Context context, int index) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = getNotificationHistory(context);
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            if (i != index) newArray.put(array.optJSONObject(i));
        }
        prefs.edit().putString(HISTORY_KEY, newArray.toString()).apply();
    }
}
