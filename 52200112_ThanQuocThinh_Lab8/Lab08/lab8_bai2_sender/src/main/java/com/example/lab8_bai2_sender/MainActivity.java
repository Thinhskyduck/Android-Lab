package com.example.lab8_bai2_sender;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppCompatEditText edtMessage, edtTitle;
    private AppCompatButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    private void initViews() {
        edtMessage = findViewById(R.id.edtMessage);
        edtTitle = findViewById(R.id.edtTitle);
        btnSend = findViewById(R.id.btnSend);
    }

    private void initListeners() {
        btnSend.setOnClickListener(v -> {
            String message = Objects.requireNonNull(edtMessage.getText()).toString();
            String title = Objects.requireNonNull(edtTitle.getText()).toString();
            if (!message.isEmpty()) {
                Intent intent = new Intent();
                intent.setAction("com.example.myApp.SEND_MESSAGE");
                intent.putExtra("message", message);
                intent.putExtra("title", title);
                // Log để kiểm tra
                Log.d("Sender", "Sending message broadcast: " + message);
                sendBroadcast(intent, "com.example.myApp.SEND_MESSAGE");
            }
        });
    }
}