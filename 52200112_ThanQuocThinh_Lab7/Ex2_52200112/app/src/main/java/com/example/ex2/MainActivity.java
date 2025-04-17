package com.example.ex2; 

import android.Manifest;

import android.content.ContentUris;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnDelete;
    MediaAdapter adapter;
    List<MediaModel> mediaList = new ArrayList<>();
    boolean showImages = true; // Để bạn có thể chuyển giữa ảnh và video nếu muốn

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "Đã xoá thành công!", Toast.LENGTH_SHORT).show();
                            checkPermissionsAndLoad(); // load lại danh sách
                        } else {
                            Toast.makeText(this, "Đã huỷ xoá!", Toast.LENGTH_SHORT).show();
                        }
                    });

    // Khai báo launcher xin quyền
    ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean granted = false;
                for (Boolean b : result.values()) {
                    granted |= b;
                }

                if (granted) {
                    loadMedia();
                } else {
                    Toast.makeText(this, "Cần quyền để truy cập ảnh/video", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        btnDelete = findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(v -> deleteSelectedMedia());

        checkPermissionsAndLoad();
    }

    private void checkPermissionsAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasImagePerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean hasVideoPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;

            if (hasImagePerm || hasVideoPerm) {
                loadMedia();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                });
            }
        } else {
            // Android 12 trở xuống
            boolean hasPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (hasPerm) {
                loadMedia();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            }
        }
    }

    private void loadMedia() {
        // No boolean needed here anymore if Utils.loadMedia is changed
        mediaList = Utils.loadMedia(this);
        Collections.sort(mediaList, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));

        adapter = new MediaAdapter(this, mediaList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
    }

    private void deleteSelectedMedia() {
        List<MediaModel> selected = adapter.getSelectedItems();
        ArrayList<Uri> urisToDelete = new ArrayList<>();

        for (MediaModel m : selected) {
            Log.d("DeleteDebug", "Attempting to find URI for path: " + m.getPath()); // Log đường dẫn cần tìm
            Uri uri = getMediaUriFromPath(m.getPath());
            if (uri != null) {
                Log.d("DeleteDebug", "Found URI: " + uri.toString()); // Log URI tìm được
                urisToDelete.add(uri);
            } else {
                Log.w("DeleteDebug", "URI not found for path: " + m.getPath()); // Log khi không tìm thấy
            }
        }

        if (urisToDelete.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ảnh/video để xoá", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Tạo yêu cầu xoá từ MediaStore
            IntentSender sender = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                sender = MediaStore.createDeleteRequest(getContentResolver(), urisToDelete).getIntentSender();
            }
            IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
            deleteLauncher.launch(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo yêu cầu xoá", Toast.LENGTH_SHORT).show();
        }
    }


    // You ALSO need to update getMediaUriFromPath
    private Uri getMediaUriFromPath(String path) {
        // Try finding it as an image first
        Uri imageUri = findMediaUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, path);
        if (imageUri != null) {
            return imageUri;
        }
        // If not found as image, try finding it as video
        return findMediaUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);
    }

    // Helper method to avoid code duplication
    private Uri findMediaUri(Uri collection, String path) {
        String[] projection = {MediaStore.MediaColumns._ID};
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{path};
        Log.d("FindUriDebug", "Querying collection " + collection + " for path: " + path); // Log truy vấn

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null) {
                Log.d("FindUriDebug", "Cursor count for " + path + " in " + collection + ": " + cursor.getCount()); // Log số lượng kết quả
                if (cursor.moveToFirst()) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                    long id = cursor.getLong(idColumn);
                    Log.d("FindUriDebug", "Found ID: " + id + " for path " + path + " in " + collection); // Log ID tìm được
                    return ContentUris.withAppendedId(collection, id);
                } else {
                    Log.w("FindUriDebug", "Cursor moveToFirst returned false for path: " + path + " in " + collection); // Log khi không có dòng đầu tiên
                }
            } else {
                Log.w("FindUriDebug", "Cursor was null for path: " + path + " in " + collection); // Log khi cursor null
            }
        } catch (Exception e) {
            Log.e("FindUriDebug", "Error querying MediaStore for path: " + path, e); // Log lỗi
        }
        Log.w("FindUriDebug", "Returning null for path: " + path + " in collection " + collection); // Log khi trả về null
        return null;
    }

}

