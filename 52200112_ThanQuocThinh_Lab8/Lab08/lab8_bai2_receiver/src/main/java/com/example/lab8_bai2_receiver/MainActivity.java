package com.example.lab8_bai2_receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_NOTIFICATION = 1234;
    private AppCompatTextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txtError = findViewById(R.id.txtError);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionIfNotGranted();
    }

    private void checkPermissionIfNotGranted(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
            }
        }
    }

    private void initListeners() {
        txtError.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Quyền truy cập thông báo")
                    .setMessage("Ứng dụng này hiện không có quyền gửi thông báo.\n" +
                            "Nó sẽ không thể gửi thông báo khi nhận được tín hiệu từ Sender.\n" +
                            "Bạn có muốn cấp quyền không?")
                    .setPositiveButton("Cấp quyền", (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                txtError.setVisibility(View.GONE);
            }
            else{
                txtError.setVisibility(View.VISIBLE);
            }
        }
    }
}