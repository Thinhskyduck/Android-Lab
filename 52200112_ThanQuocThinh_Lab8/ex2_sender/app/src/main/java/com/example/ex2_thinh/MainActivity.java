package com.example.ex2_thinh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String BROADCAST_ACTION = "com.example.sender.SEND_MESSAGE";
    private static final String PERMISSION = "com.example.ex2_thinh.permission.SEND_MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSendBroadcast = findViewById(R.id.btnSendBroadcast);
        btnSendBroadcast.setOnClickListener(v -> {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("message", "Xin chào từ Sender! Đây là thông điệp broadcast.");
            sendBroadcast(intent, PERMISSION); // Sửa thành PERMISSION
            Log.d("MainActivity", "Broadcast sent with action: " + BROADCAST_ACTION);

            Toast.makeText(MainActivity.this, "Đã gửi broadcast thành công", Toast.LENGTH_SHORT).show();
        });
    }
}