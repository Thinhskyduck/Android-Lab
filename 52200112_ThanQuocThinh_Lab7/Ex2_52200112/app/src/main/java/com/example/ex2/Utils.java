package com.example.ex2;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<MediaModel> loadMedia(Context context) {
        List<MediaModel> mediaList = new ArrayList<>();
        Uri collection;
        Cursor cursor = null;
        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA, // Path
                MediaStore.MediaColumns.DATE_ADDED
        };
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        // --- Query Images ---
        try {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            cursor = context.getContentResolver().query(collection, projection, null, null, sortOrder);
            if (cursor != null) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    // Truyền false cho isVideo khi là ảnh
                    mediaList.add(new MediaModel(path, dateAdded, false)); // <-- Sửa ở đây
                }
            }
        } catch (Exception e) {
            Log.e("LoadMedia", "Error loading images", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


        // --- Query Videos ---
        cursor = null; // Reset cursor variable
        try {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            cursor = context.getContentResolver().query(collection, projection, null, null, sortOrder);
            if (cursor != null) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    // Truyền true cho isVideo khi là video
                    mediaList.add(new MediaModel(path, dateAdded, true)); // <-- Sửa ở đây
                }
            }
        } catch (Exception e) {
            Log.e("LoadMedia", "Error loading videos", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        Log.d("LoadMedia", "Total media loaded: " + mediaList.size());
        return mediaList;
    }
}