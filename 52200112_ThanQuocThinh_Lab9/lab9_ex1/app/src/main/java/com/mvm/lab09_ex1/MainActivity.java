package com.mvm.lab09_ex1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mvm.lab09_ex1.network.Downloader;
import com.mvm.lab09_ex1.recycler.DownloadFile;
import com.mvm.lab09_ex1.recycler.FileAdapter;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtUrl;
    private Button btnDownload;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private FileAdapter adapter;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static final String PREF_NAME = "DownloadPrefs";
    private static final String KEY_FILES = "files";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private Handler handler; // Thêm Handler để cập nhật UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Download Manager");

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        gson = new Gson();
        handler = new Handler(Looper.getMainLooper()); // Khởi tạo Handler trên main thread

        initViews();
        initObject();
        updateUI();
        requestStoragePermission();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    private void updateUI() {
        Log.d("MainActivity", "Adapter item count: " + adapter.getItemCount());
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initObject() {
        adapter = new FileAdapter();
        List<DownloadFile> savedFiles = loadFiles();
        if (savedFiles != null && !savedFiles.isEmpty()) {
            adapter.setFiles(savedFiles);
        } else {
            adapter.setFiles(DownloadFile.generate());
        }
        recyclerView.setAdapter(adapter);
    }

    private void initViews() {
        txtUrl = findViewById(R.id.txtUrl);
        btnDownload = findViewById(R.id.btnDownload);
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));

        btnDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnDownload) {
            String url = txtUrl.getText().toString().trim();
            Log.d("MainActivity", "Download URL: " + url);
            if (!url.isEmpty()) {
                DownloadFile file = new DownloadFile();
                file.setDownloadUrl(url);
                adapter.add(file);
                saveFiles(adapter.getFiles());
                updateUI();
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();

                // Sử dụng MediaStore để lưu vào thư mục Downloads
                new Thread(() -> {
                    Downloader.fetchInfo(file, new Downloader.Callback() {
                        @Override
                        public void onFileInfoChanged(DownloadFile file) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    Log.d("MainActivity", "Updating file info at index: " + index);
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                            });
                        }

                        @Override
                        public void onComplete(DownloadFile file) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    file.markAsComplete();
                                    Log.d("MainActivity", "File completed at index: " + index);
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                            });
                        }

                        @Override
                        public void onError(Throwable t) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    file.markAsFail();
                                    Log.d("MainActivity", "File failed at index: " + index);
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                                Toast.makeText(MainActivity.this, "Download failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });

                    // Cập nhật Downloader để sử dụng MediaStore
                    Downloader.downloadWithMediaStore(file, this, new Downloader.Callback() {
                        @Override
                        public void onFileInfoChanged(DownloadFile file) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    Log.d("MainActivity", "Updating progress at index: " + index + ", Progress: " + file.getProgress());
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                            });
                        }

                        @Override
                        public void onComplete(DownloadFile file) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    file.markAsComplete();
                                    Log.d("MainActivity", "Download completed at index: " + index);
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                                Toast.makeText(MainActivity.this, "Download completed: " + file.getName(), Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(Throwable t) {
                            handler.post(() -> {
                                int index = adapter.getFiles().indexOf(file);
                                if (index != -1) {
                                    file.markAsFail();
                                    Log.d("MainActivity", "Download failed at index: " + index);
                                    adapter.notifyItemChanged(index);
                                }
                                saveFiles(adapter.getFiles());
                                Toast.makeText(MainActivity.this, "Download failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }).start();
            } else {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveFiles(List<DownloadFile> files) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(files);
        editor.putString(KEY_FILES, json);
        editor.apply();
    }

    private List<DownloadFile> loadFiles() {
        String json = sharedPreferences.getString(KEY_FILES, null);
        if (json != null) {
            Type type = new TypeToken<List<DownloadFile>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Storage permission granted");
                // Quyền được cấp, có thể tiếp tục tải xuống
            } else {
                Log.d("MainActivity", "Storage permission denied");
                Toast.makeText(this, "Storage permission denied. Cannot download files.", Toast.LENGTH_LONG).show();
            }
        }
    }
}