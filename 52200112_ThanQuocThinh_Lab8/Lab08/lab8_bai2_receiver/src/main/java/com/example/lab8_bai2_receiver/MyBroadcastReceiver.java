package com.example.lab8_bai2_receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public MyBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        String title = intent.getStringExtra("title");

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        // Kiểm tra API > 26 và tạo channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_receiver", "Channel Receiver", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_channel_receiver")
                .setSmallIcon(R.mipmap.ic_app_icon_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Hiển thị notification, log để kiểm tra
        int id = (int) (System.currentTimeMillis() % 10000000);
        Log.d("Receiver", "Received message broadcast id: " + id);
        notificationManager.notify(id, builder.build());

    }
}
