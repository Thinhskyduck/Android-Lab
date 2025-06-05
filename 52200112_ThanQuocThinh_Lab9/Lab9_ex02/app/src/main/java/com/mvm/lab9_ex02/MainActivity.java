package com.mvm.lab9_ex02;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicAdapter.OnItemClickListener, MusicService.OnPlaybackListener {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private List<Uri> songList;
    private MusicService musicService;
    private boolean isBound = false;
    private SeekBar seekBar;
    private TextView currentTimeText, totalTimeText;
    private ImageButton btnPlayPause, btnNext, btnBack;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private int currentSongPosition = -1; // Biến để theo dõi vị trí bài hát hiện tại

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;
            musicService.setSongList(songList);
            musicService.setPlaybackListener(MainActivity.this);
            updateMediaControls();
            startSeekBarUpdate();
            Log.d("MainActivity", "Service connected successfully, songList size: " + songList.size());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
            stopSeekBarUpdate();
            Log.d("MainActivity", "Service disconnected unexpectedly");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        songList = new ArrayList<>();
        adapter = new MusicAdapter(this, songList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        setupMediaControls();
        checkStoragePermission();
        Log.d("MainActivity", "onCreate: Starting permission check...");
    }

    private void checkStoragePermission() {
        Log.d("MainActivity", "Checking storage permission...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "READ_MEDIA_AUDIO permission not granted, requesting...");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                    Log.d("MainActivity", "Showing rationale dialog...");
                    new AlertDialog.Builder(this)
                            .setTitle("Quyền truy cập cần thiết")
                            .setMessage("Ứng dụng cần quyền truy cập tệp âm thanh để tải danh sách bài hát. Vui lòng cấp quyền.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Log.d("MainActivity", "User clicked OK, requesting permission...");
                                requestPermission();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    Log.d("MainActivity", "No rationale needed, requesting permission directly...");
                    requestPermission();
                }
            } else {
                Log.d("MainActivity", "READ_MEDIA_AUDIO permission already granted, loading songs...");
                loadSongs();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "READ_EXTERNAL_STORAGE permission not granted, requesting...");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d("MainActivity", "Showing rationale dialog...");
                    new AlertDialog.Builder(this)
                            .setTitle("Quyền truy cập cần thiết")
                            .setMessage("Ứng dụng cần quyền truy cập bộ nhớ để tải danh sách bài hát. Vui lòng cấp quyền.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Log.d("MainActivity", "User clicked OK, requesting permission...");
                                requestPermission();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    Log.d("MainActivity", "No rationale needed, requesting permission directly...");
                    requestPermission();
                }
            } else {
                Log.d("MainActivity", "READ_EXTERNAL_STORAGE permission already granted, loading songs...");
                loadSongs();
            }
        }
    }

    private void requestPermission() {
        Log.d("MainActivity", "Requesting permissions...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Log.e("MainActivity", "Error requesting READ_MEDIA_AUDIO permission: " + e.getMessage());
            }
        } else {
            try {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Log.e("MainActivity", "Error requesting READ_EXTERNAL_STORAGE permission: " + e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MainActivity", "onRequestPermissionsResult called, requestCode=" + requestCode);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permission granted, loading songs...");
                loadSongs();
            } else {
                Log.d("MainActivity", "Permission denied");
                Toast.makeText(this, "Quyền truy cập tệp âm thanh bị từ chối. Vui lòng cấp quyền trong Cài đặt.", Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(this)
                        .setTitle("Quyền bị từ chối")
                        .setMessage("Bạn có muốn mở Cài đặt để cấp quyền không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
        }
    }

    private void loadSongs() {
        songList.clear();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );
        if (cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    Log.d("MainActivity", "Found song: " + displayName + " at path: " + filePath);
                    Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    songList.add(uri);
                }
                cursor.close();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Found " + songList.size() + " songs", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Loaded " + songList.size() + " songs from MediaStore");
            } else {
                cursor.close();
                Toast.makeText(this, "No songs found in MediaStore", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "No songs found in MediaStore");
            }
        } else {
            Toast.makeText(this, "Error accessing MediaStore", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Cursor is null, cannot access MediaStore");
        }
    }

    private void setupMediaControls() {
        btnBack.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playPrevious();
                currentSongPosition = musicService.getCurrentSongPosition();
                recyclerView.scrollToPosition(currentSongPosition); // Cuộn đến vị trí bài hát
                updateMediaControls();
                Log.d("MainActivity", "Pressed Back, current position: " + currentSongPosition);
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicService.play();
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }
                updateMediaControls();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playNext();
                currentSongPosition = musicService.getCurrentSongPosition();
                recyclerView.scrollToPosition(currentSongPosition); // Cuộn đến vị trí bài hát
                updateMediaControls();
                Log.d("MainActivity", "Pressed Next, current position: " + currentSongPosition);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateMediaControls() {
        if (isBound && musicService != null) {
            int currentPosition = musicService.getCurrentPosition();
            int duration = musicService.getDuration();
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            currentTimeText.setText(formatTime(currentPosition));
            totalTimeText.setText(formatTime(duration));
            btnPlayPause.setImageResource(musicService.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            findViewById(R.id.mediaControls).setVisibility(View.VISIBLE);
            Log.d("MainActivity", "Updated media controls: position=" + currentPosition + ", duration=" + duration);
        } else {
            Log.e("MainActivity", "Cannot update media controls, service not bound");
        }
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null) {
                    int currentPosition = musicService.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTimeText.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(updateSeekBar, 1000);
    }

    private void stopSeekBarUpdate() {
        if (updateSeekBar != null) {
            handler.removeCallbacks(updateSeekBar);
        }
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onItemClick(int position) {
        if (isBound && musicService != null) {
            currentSongPosition = position;
            musicService.playSong(position);
            recyclerView.scrollToPosition(currentSongPosition);
            updateMediaControls();
            Log.d("MainActivity", "Clicked song at position " + position);
        } else {
            Log.e("MainActivity", "Service not bound or musicService is null");
            Intent intent = new Intent(this, MusicService.class);
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPlaybackError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "Playback error: " + error);
        });
    }

    @Override
    public void onPositionChanged(int newPosition) {
        currentSongPosition = newPosition;
        runOnUiThread(() -> {
            recyclerView.scrollToPosition(currentSongPosition);
            updateMediaControls();
            Log.d("MainActivity", "Position changed to: " + currentSongPosition);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        boolean bound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("MainActivity", "onStart: Binding to MusicService, bound=" + bound);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
            stopSeekBarUpdate();
            Log.d("MainActivity", "onStop: Unbinding from MusicService");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekBarUpdate();
    }
}