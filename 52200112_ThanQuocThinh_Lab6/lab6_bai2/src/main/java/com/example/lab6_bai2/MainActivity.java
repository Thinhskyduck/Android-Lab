package com.example.lab6_bai2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Permission;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton btnDocIn, btnDocEx, btnGhiIn, btnGhiEx;
    private AppCompatEditText txtContent;

    private static final String FILE_INTERNAL_NAME = "output.txt";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 294025;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 294026;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    private void initViews() {
        btnDocIn = findViewById(R.id.btnDocIn);
        btnDocEx = findViewById(R.id.btnDocEx);
        btnGhiIn = findViewById(R.id.btnGhiIn);
        btnGhiEx = findViewById(R.id.btnGhiEx);
        txtContent = findViewById(R.id.txtContent);
    }

    private void initListeners(){
        // Đọc nội dung tệp từ bộ nhớ trong
        btnDocIn.setOnClickListener(view -> {
            File file = new File(getFilesDir(), FILE_INTERNAL_NAME);
            // Tạo tệp nếu chưa tồn tại
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Đọc nội dung tệp
            readFileFromUri(Uri.fromFile(file));
        });

        // Đọc nội dung tệp từ bộ nhớ ngoài
        btnDocEx.setOnClickListener(view -> {
            // Kiểm tra quyền đọc bộ nhớ ngoài
            if (checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_EXTERNAL_STORAGE);
                return;
            }
            // Đọc nội dung tệp
            readExternal();
        });

        // Ghi nội dung tệp vào bộ nhớ trong
        btnGhiIn.setOnClickListener(view -> {
            File file = new File(getFilesDir(), FILE_INTERNAL_NAME);
            // Tạo tệp nếu chưa tồn tại
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writeFileFromUri(Uri.fromFile(file));
        });

        // Ghi nội dung tệp vào bộ nhớ ngoài
        btnGhiEx.setOnClickListener(view -> {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                return;
            }
            writeExternal();
        });
    }

    private void readExternal(){
        if (isExternalStorageWritable()) {
            mGetContent.launch("*/*");
        } else {
            toastMakeText("Không tìm thấy bộ nhớ ngoài hoặc không có quyền đọc ghi");
        }
    }

    private void writeExternal(){
        if (isExternalStorageWritable()) {
            mSetContent.launch("output.txt");
        } else {
            toastMakeText("Không tìm thấy bộ nhớ ngoài");
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::readFileFromUri);

    ActivityResultLauncher<String> mSetContent = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(),
            this::writeFileFromUri);

    private void readFileFromUri(Uri uri){
        if (uri == null) return;
        try {
            Scanner fc = new Scanner(new InputStreamReader(getContentResolver().openInputStream(uri)));
            StringBuilder stringBuilder = new StringBuilder();
            while (fc.hasNextLine()) {
                stringBuilder.append(fc.nextLine()).append("\n");
            }
            txtContent.setText(stringBuilder.toString().trim());
            stringBuilder.setLength(0);
            fc.close();
            toastMakeText("Đọc nội dung tệp thành công");
        } catch (Exception e) {
            toastMakeText("Đọc nội dung tệp thất bại");
            throw new RuntimeException(e);
        }
    }

    private void writeFileFromUri(Uri uri){
        if (uri == null) return;
        try {
            OutputStreamWriter sw = new OutputStreamWriter(getContentResolver().openOutputStream(uri));
            sw.write(Objects.requireNonNull(txtContent.getText()).toString().trim());
            sw.close();
            toastMakeText("Ghi nội dung tệp thành công");
        } catch (Exception e) {
            toastMakeText("Ghi nội dung tệp thất bại");
            throw new RuntimeException(e);
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void toastMakeText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission(String permission){
        return ActivityCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mGetContent.launch("*/*");
            } else {
                toastMakeText("Read External Storage permission denied");
            }
        }
        else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mSetContent.launch("output.txt");
            } else {
                toastMakeText("Write External Storage permission denied");
            }
        }
    }
}