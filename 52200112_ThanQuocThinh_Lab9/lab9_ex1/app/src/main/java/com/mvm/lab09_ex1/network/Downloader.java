package com.mvm.lab09_ex1.network;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.os.Build;
import com.mvm.lab09_ex1.recycler.DownloadFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Downloader {

    public interface Callback {
        void onFileInfoChanged(DownloadFile file);
        void onComplete(DownloadFile file);
        void onError(Throwable t);
    }

    public static void fetchInfo(DownloadFile file, Callback callback) {
        try {
            URL url = new URL(file.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.connect();

            String fileName = getFileName(file.getDownloadUrl(), conn);
            int fileSize = conn.getContentLength();

            file.setName(fileName);
            file.setSize(fileSize);

            if (callback != null) {
                callback.onFileInfoChanged(file);
            }

            conn.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (callback != null) {
                callback.onError(ex);
            }
        }
    }

    public static void download(DownloadFile file, File parent, Callback callback) {
        try {
            URL url = new URL(file.getDownloadUrl());
            Log.d("Downloader", "Downloading from URL: " + file.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(15000);
            conn.connect();
            Log.d("Downloader", "Connection response code: " + conn.getResponseCode());

            String fileName = getFileName(file.getDownloadUrl(), conn);
            int fileSize = conn.getContentLength();
            Log.d("Downloader", "File name: " + fileName + ", Size: " + fileSize);

            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[10 * 1024];
            int downloaded = 0;
            int size;

            File outputFile = new File(parent, fileName);
            Log.d("Downloader", "Saving to: " + outputFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(outputFile);

            while ((size = is.read(buffer)) != -1) {
                fos.write(buffer, 0, size);
                downloaded += size;

                int percentage = (int) (downloaded * 100.0 / fileSize);
                file.setProgress(percentage);
                Log.d("Downloader", "Progress: " + percentage + "%");

                if (callback != null) {
                    callback.onFileInfoChanged(file);
                }

                // Độ trễ giả lập (tùy chọn) để đảm bảo UI cập nhật
                Thread.sleep(50); // 50ms giữa các lần cập nhật
            }

            if (callback != null) {
                callback.onComplete(file);
            }
            Log.d("Downloader", "Download completed: " + fileName);

            fos.close();
            is.close();
            conn.disconnect();

        } catch (Exception e) {
            Log.e("Downloader", "Download failed: ", e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    private static String getFileName(String fileURL, HttpURLConnection httpConn) {
        String fileName = "";
        String disposition = httpConn.getHeaderField("Content-Disposition");

        if (disposition != null) {
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10, disposition.length() - 1);
            }
        } else {
            fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
        }

        return fileName;
    }

    public static void downloadWithMediaStore(DownloadFile file, Context context, Callback callback) {
        try {
            URL url = new URL(file.getDownloadUrl());
            Log.d("Downloader", "Downloading from URL: " + file.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(15000);
            conn.connect();
            Log.d("Downloader", "Connection response code: " + conn.getResponseCode());

            String fileName = getFileName(file.getDownloadUrl(), conn);
            int fileSize = conn.getContentLength();
            Log.d("Downloader", "File name: " + fileName + ", Size: " + fileSize);

            if (fileSize <= 0) {
                throw new IOException("Invalid file size: " + fileSize);
            }

            // Ensure unique file name by appending timestamp
            String uniqueFileName = generateUniqueFileName(fileName);
            Log.d("Downloader", "Using unique file name: " + uniqueFileName);

            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[10 * 1024];
            int downloaded = 0;
            int size;

            // Sử dụng MediaStore để lưu vào thư mục Downloads
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, uniqueFileName);
            values.put(MediaStore.Downloads.MIME_TYPE, getMimeType(uniqueFileName));
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IOException("Failed to create MediaStore entry");
            }
            Log.d("Downloader", "MediaStore URI: " + uri.toString());

            OutputStream os = context.getContentResolver().openOutputStream(uri);
            if (os == null) {
                throw new IOException("Failed to open OutputStream for URI: " + uri);
            }

            try {
                while ((size = is.read(buffer)) != -1) {
                    os.write(buffer, 0, size);
                    downloaded += size;

                    int percentage = (int) (downloaded * 100.0 / fileSize);
                    file.setProgress(percentage);
                    Log.d("Downloader", "Progress: " + percentage + "%");

                    if (callback != null) {
                        callback.onFileInfoChanged(file);
                    }

                    Thread.sleep(100); // Increase delay to 100ms for better UI updates
                }

                // Đánh dấu file là hoàn thành
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                int updated = context.getContentResolver().update(uri, values, null, null);
                Log.d("Downloader", "MediaStore update rows affected: " + updated);

                if (callback != null) {
                    callback.onComplete(file);
                }
                Log.d("Downloader", "Download completed: " + uniqueFileName);

            } finally {
                os.close();
            }

            is.close();
            conn.disconnect();

        } catch (Exception e) {
            Log.e("Downloader", "Download failed: ", e);
            if (callback != null) {
                callback.onError(e);
            }
        }
    }

    // Helper method to generate unique file name
    private static String generateUniqueFileName(String originalName) {
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return baseName + "_" + timeStamp + extension;
    }

    // Helper method to determine MIME type
    private static String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            default: return "application/octet-stream";
        }
    }
}