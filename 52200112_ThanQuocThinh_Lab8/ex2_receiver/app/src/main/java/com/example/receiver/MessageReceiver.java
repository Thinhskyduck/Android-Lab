package com.example.receiver;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MessageReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "MessageChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MessageReceiver", "Received broadcast with action: " + intent.getAction());
        String message = intent.getStringExtra("message");

        if (message != null) {
            Log.d("MessageReceiver", "Message received: " + message);
        } else {
            Log.w("MessageReceiver", "No message found in intent");
        }

        // Tạo Notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Notification Channel (cần cho Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Thông điệp từ Sender")
                .setContentText(message != null ? message : "Không có thông điệp")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Hiển thị Notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d("MessageReceiver", "Notification displayed successfully");
    }
}
