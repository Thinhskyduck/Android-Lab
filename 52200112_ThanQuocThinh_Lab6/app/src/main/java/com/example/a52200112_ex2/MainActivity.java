package com.example.a52200112_ex2;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    String fileName = "data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);

        findViewById(R.id.btnGhiInternal).setOnClickListener(v -> ghiInternal());
        findViewById(R.id.btnDocInternal).setOnClickListener(v -> docInternal());
        findViewById(R.id.btnGhiExternal).setOnClickListener(v -> ghiExternal());
        findViewById(R.id.btnDocExternal).setOnClickListener(v -> docExternal());
    }

    private void ghiInternal() {
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(editText.getText().toString().getBytes());
            fos.close();
            Toast.makeText(this, "Ghi internal thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void docInternal() {
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            editText.setText(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ghiExternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);

            // Kiểm tra xem file đã tồn tại chưa
            String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{fileName};

            Uri fileUri = null;

            Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                // Nếu tồn tại thì lấy uri hiện có
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
                fileUri = ContentUris.withAppendedId(collection, id);
                cursor.close();
            } else {
                // Nếu chưa có thì insert mới
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                fileUri = resolver.insert(collection, values);
            }

            // Ghi nội dung
            try (OutputStream out = resolver.openOutputStream(fileUri, "w")) {
                out.write(editText.getText().toString().getBytes());
                Toast.makeText(this, "Ghi external thành công", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void docExternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
                String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
                String[] selectionArgs = new String[]{fileName};

                Cursor cursor = getContentResolver().query(collection, null, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    Uri fileUri = ContentUris.withAppendedId(collection, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)));
                    InputStream inputStream = getContentResolver().openInputStream(fileUri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    editText.setText(builder.toString());
                    cursor.close();
                } else {
                    Toast.makeText(this, "Không tìm thấy file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
