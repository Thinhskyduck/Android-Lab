package com.mvm.lab9_ex02;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private List<Uri> songList = new ArrayList<>();
    private int currentPosition = 0;
    private OnPlaybackListener playbackListener;

    public interface OnPlaybackListener {
        void onPlaybackError(String error);
        void onPositionChanged(int newPosition); // Thêm callback cho vị trí thay đổi
    }

    public void setPlaybackListener(OnPlaybackListener listener) {
        this.playbackListener = listener;
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        createNotificationChannel();
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "MusicServiceChannel")
                    .setContentTitle("Music Service")
                    .setContentText("Running in background")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .build();
        }
        startForeground(1, notification);
        Log.d("MusicService", "Service created");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "MusicServiceChannel",
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void setSongList(List<Uri> list) {
        songList.clear();
        songList.addAll(list);
    }

    public void playSong(int position) {
        if (position < 0 || position >= songList.size()) {
            if (playbackListener != null) {
                playbackListener.onPlaybackError("Invalid position: " + position);
            }
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentPosition = position;
        mediaPlayer = new MediaPlayer();
        try {
            Uri uri = songList.get(position);
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> playNext());
            if (playbackListener != null) {
                playbackListener.onPositionChanged(currentPosition); // Thông báo vị trí thay đổi
            }
            Log.d("MusicService", "Playing song at position " + position + " from URI: " + uri.toString());
        } catch (Exception e) {
            Log.e("MusicService", "Error playing song: " + e.getMessage());
            if (playbackListener != null) {
                playbackListener.onPlaybackError("Error playing song: " + e.getMessage());
            }
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d("MusicService", "Resumed song at position " + currentPosition);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d("MusicService", "Paused song at position " + currentPosition);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void playNext() {
        if (currentPosition < songList.size() - 1) {
            playSong(currentPosition + 1);
        }
    }

    public void playPrevious() {
        if (currentPosition > 0) {
            playSong(currentPosition - 1);
        }
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
            Log.d("MusicService", "Seeked to " + progress + " ms");
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (IllegalStateException e) {
            Log.e("MusicService", "Error getting current position: " + e.getMessage());
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        } catch (IllegalStateException e) {
            Log.e("MusicService", "Error getting duration: " + e.getMessage());
            return 0;
        }
    }

    public int getCurrentSongPosition() {
        return currentPosition; // Getter cho vị trí bài hát hiện tại
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}