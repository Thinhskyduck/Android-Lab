package com.example.ex1;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_COUNT = "count";
    private static final String KEY_TEXT_COLOR = "textColor";
    private static final String KEY_BG_COLOR = "bgColor";

    private TextView txtCounter;
    private EditText edtTextColor, edtBgColor;
    private SharedPreferences sharedPreferences;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các view
        txtCounter = findViewById(R.id.txtCounter);
        edtTextColor = findViewById(R.id.edtTextColor);
        edtBgColor = findViewById(R.id.edtBgColor);
        Button btnSave = findViewById(R.id.btnSave);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Lấy dữ liệu từ SharedPreferences
        count = sharedPreferences.getInt(KEY_COUNT, 0) + 1; // Tăng số lần mở lên 1
        String textColor = sharedPreferences.getString(KEY_TEXT_COLOR, "#FFFFFF"); // Mặc định trắng
        String bgColor = sharedPreferences.getString(KEY_BG_COLOR, "#2222FF"); // Mặc định xanh

        // Cập nhật giao diện
        txtCounter.setText(String.valueOf(count));
        txtCounter.setTextColor(Color.parseColor(textColor));
        txtCounter.setBackgroundColor(Color.parseColor(bgColor));

        // Hiển thị giá trị trong EditText
        edtTextColor.setText(textColor);
        edtBgColor.setText(bgColor);

        // Lưu số lần mở ứng dụng
        sharedPreferences.edit().putInt(KEY_COUNT, count).apply();

        // Xử lý sự kiện lưu màu sắc
        btnSave.setOnClickListener(v -> {
            String newTextColor = edtTextColor.getText().toString();
            String newBgColor = edtBgColor.getText().toString();

            try {
                txtCounter.setTextColor(Color.parseColor(newTextColor));
                txtCounter.setBackgroundColor(Color.parseColor(newBgColor));

                // Lưu vào SharedPreferences
                sharedPreferences.edit()
                        .putString(KEY_TEXT_COLOR, newTextColor)
                        .putString(KEY_BG_COLOR, newBgColor)
                        .apply();
            } catch (IllegalArgumentException e) {
                // Màu nhập không hợp lệ, không thực hiện thay đổi
            }
        });
    }
}
