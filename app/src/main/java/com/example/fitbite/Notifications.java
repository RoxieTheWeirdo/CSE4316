package com.example.fitbite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications {

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
        String pref = settings.getNotificationMode();
        if (pref.equals("Off"))
            return;

        if (pref.equals("Minimal"))
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.appicon_grayscale)
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.appicon // Full-color app icon inside notification
                ))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }
}
