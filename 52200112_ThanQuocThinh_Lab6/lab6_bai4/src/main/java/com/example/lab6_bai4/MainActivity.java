package com.example.lab6_bai4;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.os.BuildCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2945;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 2026;
    public static final String ROOT_FOLDER = Environment.getExternalStorageDirectory().getPath();

    private int backPressCounter = 0;
    private ConstraintLayout viewEmpty;
    private AppCompatButton btnBack;
    private AppCompatTextView tvUrl;
    private RecyclerView recyclerView;
    private StorageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initVariables();
        checkPermissionAndInitData();
    }

    private void initVariables(){
        viewEmpty = findViewById(R.id.viewEmpty);
        btnBack = findViewById(R.id.btnBack);
        tvUrl = findViewById(R.id.tvUrl);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void checkPermissionAndInitData(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Manage External Storage permission is needed to access files")
                        .setPositiveButton("Grant permission", (dialogInterface, i) -> {
                            if (!Environment.isExternalStorageManager()) {
                                sendRequestPermissionAllFileAccess();
                                checkPermissionAndInitData();
                            } else initDataAndListeners();
                        })
                        .setNegativeButton("Exit", (dialogInterface, i) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            } else initDataAndListeners();
        } else {
            if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            } else if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                initDataAndListeners();
            }
        }
    }

    private void initDataAndListeners(){
        // Set data for adapter
        tvUrl.setText(ROOT_FOLDER);
        mAdapter = new StorageAdapter(this, ROOT_FOLDER, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setViewEmpty(mAdapter.getItemCount() == 0);
        // Set listeners
        btnBack.setOnClickListener(view -> {
            mAdapter.backToParentPath();
        });
        tvUrl.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle("Current directory")
                .setMessage(tvUrl.getText().toString())
                .show();
        });
    }

    public void setViewEmpty(boolean value){
        if (value){
            viewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            viewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkPermission(String permission){
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        requestPermissions(new String[]{ permission }, requestCode);
    }

    public void setPath(String path){
        tvUrl.setText(path);
        mAdapter.setPath(path);
    }

    public Uri getUriFromPath(String path){
        return FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                new File(path)
        );
    }

    public boolean checkFileThenOpen(String filePath) {
        File file = new File(filePath);
        if (file.isFile()){
            openDialogViewFile(filePath);
            return true;
        }
        return false;
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null)
        {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void createNewFile(String fileName, String extension, String fileContent) throws IOException {
        // Kiểm tra xem tên file có hợp lệ không
        if (fileName.isEmpty()){
            Toast.makeText(this, "File name can not be empty", Toast.LENGTH_SHORT).show();
            openDialogCreateFile(fileName, extension, fileContent);
            return;
        }
        if (extension.isEmpty()){
            Toast.makeText(this, "File extension can not be empty", Toast.LENGTH_SHORT).show();
            openDialogCreateFile(fileName, extension, fileContent);
            return;
        }
        // Kiểm tra xem file đã tồn tại chưa
        String path = tvUrl.getText().toString();
        try {
            File file = new File(path, fileName + "." + extension);
            if (file.exists()){
                new AlertDialog.Builder(this)
                        .setTitle("File exists")
                        .setMessage("Do you want to overwrite this file?")
                        .setPositiveButton("Override", (dialogInterface, i) -> {
                            if (writeContentToFile(file, fileContent)){
                                mAdapter.refresh();
                                Toast.makeText(this, "File created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Rename file", (dialogInterface, i) -> {
                            openDialogCreateFile(fileName, extension, fileContent);
                            Toast.makeText(this, "Please rename the file", Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return;
            }
            file.createNewFile();
            if (writeContentToFile(file, fileContent)){
                mAdapter.refresh();
                Toast.makeText(this, "File created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewFolder(String folderName){
        if (folderName.isEmpty()){
            Toast.makeText(this, "Folder name can not be empty", Toast.LENGTH_SHORT).show();
            openDialogCreateDirectory(folderName);
            return;
        }
        String path = tvUrl.getText().toString();
        File folder = new File(path, folderName);
        if (folder.exists()){
            new AlertDialog.Builder(this)
                    .setTitle("Folder exists")
                    .setMessage("Can not create folder with the same name")
                    .setPositiveButton("Rename", (dialogInterface, i) -> {
                        openDialogCreateDirectory(folderName + "_copy");
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                    .show();
            return;
        }
        if (folder.mkdir()){
            mAdapter.refresh();
            Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFileFromUri(Uri uri){
        if (uri == null) return;
        try {
            Scanner fc = new Scanner(new InputStreamReader(getContentResolver().openInputStream(uri)));
            StringBuilder stringBuilder = new StringBuilder();
            while (fc.hasNextLine()) {
                stringBuilder.append(fc.nextLine()).append("\n");
            }
            stringBuilder.setLength(0);
            fc.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getContentFromFileUri(Uri uri){
        if (uri == null) return null;
        try {
            Scanner fc = new Scanner(new InputStreamReader(getContentResolver().openInputStream(uri)));
            StringBuilder stringBuilder = new StringBuilder();
            while (fc.hasNextLine()) {
                stringBuilder.append(fc.nextLine()).append("\n");
            }
            fc.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean writeContentToFile(File file, String content){
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        if (uri == null) { return false; }
        try {
            OutputStreamWriter sw = new OutputStreamWriter(getContentResolver().openOutputStream(uri));
            sw.write(content);
            sw.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFiles(File[] files){
        for (File file: files){
            file.delete();
        }
        mAdapter.refresh();
    }

    private void openDialogCreateFile(String fileName, String extension, String fileContent){
        // Tạo view để nhập tên file và nội dung file
        View view = LayoutInflater.from(this).inflate(R.layout.layout_create_file, null);
        AppCompatEditText etFileName = view.findViewById(R.id.edt_file_name);
        AppCompatEditText edt_file_ext = view.findViewById(R.id.edt_file_ext);
        AppCompatEditText etFileContent = view.findViewById(R.id.edt_file_content);
        // Nếu fileName và fileContent không null thì set vào EditText
        if (fileName != null) etFileName.setText(fileName);
        if (extension != null) edt_file_ext.setText(extension);
        if (fileContent != null) etFileContent.setText(fileContent);
        // Tạo dialog
        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Create", (dialogInterface, i) -> {
                    try {
                        createNewFile(
                                Objects.requireNonNull(etFileName.getText()).toString(),
                                Objects.requireNonNull(edt_file_ext.getText()).toString(),
                                Objects.requireNonNull(etFileContent.getText()).toString()
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .show();
    }

    private void openDialogCreateDirectory(@Nullable String directoryName){
        // Tạo view để nhập tên file và nội dung file
        View view = LayoutInflater.from(this).inflate(R.layout.layout_create_directory, null);
        AppCompatEditText edtFolderName = view.findViewById(R.id.edt_folder_name);
        // Nếu fileName và fileContent không null thì set vào EditText
        if (directoryName != null) edtFolderName.setText(directoryName);
        // Tạo dialog
        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Create", (dialogInterface, i) ->
                    createNewFolder( Objects.requireNonNull(edtFolderName.getText()).toString() )
                )
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .show();
    }

    private void openDialogViewFile(String filePath){
        File file = new File(filePath);
        // Nếu tập tin vượt quá 1MB thì không mở được
        if (file.length() > 1024 * 100){
            Toast.makeText(this, "File is too large to open", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy nội dung file
        String fileContent = getContentFromFileUri(getUriFromPath(filePath)).trim();
        String fileName = file.getName();
        // Tạo view để nhập tên file và nội dung file
        View view = LayoutInflater.from(this).inflate(R.layout.layout_content_view, null);
        AppCompatTextView tvFileName = view.findViewById(R.id.tv_file_name);
        AppCompatEditText tvFileContent = view.findViewById(R.id.tv_file_content);
        // Gán fileName và fileContent vào EditText
        tvFileName.setText(fileName);
        tvFileContent.setText(fileContent);
        tvFileContent.setEnabled(false);
        // Tạo dialog
        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Close", (dialogInterface, i) -> {})
                .show();
    }

    public void openDialogEditFile(String filePath){
        File file = new File(filePath);
        if (file.length() > 1024 * 100){
            Toast.makeText(this, "File is too large to edit", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy nội dung file
        String fileContent = getContentFromFileUri(getUriFromPath(filePath)).trim();
        String fileName = new File(filePath).getName();
        // Tạo view để nhập tên file và nội dung file
        View view = LayoutInflater.from(this).inflate(R.layout.layout_content_view, null);
        AppCompatTextView tvFileName = view.findViewById(R.id.tv_file_name);
        AppCompatEditText tvFileContent = view.findViewById(R.id.tv_file_content);
        AppCompatImageView imgStatus = view.findViewById(R.id.imgStatus);
        // Gán fileName và fileContent vào EditText
        tvFileName.setText(fileName);
        tvFileContent.setText(fileContent);
        imgStatus.setImageResource(R.drawable.ic_edit);
        tvFileContent.requestFocus();
        // Tạo dialog
        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setNegativeButton("Close", (dialogInterface, i) -> {})
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    try {
                        writeContentToFile(new File(filePath), Objects.requireNonNull(tvFileContent.getText()).toString());
                        Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void askUserConfirmDelete(File[] files){
        new AlertDialog.Builder(this)
                .setTitle("Delete files")
                .setMessage("Do you want to delete " + files.length + " files and folders?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    deleteFiles(files);
                })
                .setNegativeButton("No", (dialogInterface, i) -> {})
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_permission);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_create_file){
            openDialogCreateFile(null, null, null);
        }
        if (item.getItemId() == R.id.menu_create_folder){
            openDialogCreateDirectory(null);
        }
        if (item.getItemId() == R.id.menu_delete_all){
            mAdapter.tryDeleteAllFiles();
        }
        if (item.getItemId() == R.id.menu_delete_selected){
            mAdapter.tryDeleteSelectedFiles();
        }
        if (item.getItemId() == R.id.menu_open_with){
            mAdapter.tryReadingFileOutside();
        }
        if (item.getItemId() == R.id.menu_permission){
            sendRequestPermissionAllFileAccess();
        }
        if (item.getItemId() == R.id.menu_about){
            new AlertDialog.Builder(this)
                    .setTitle("Về ứng dụng này")
                    .setMessage("Đây là một ứng dụng quản lý file đơn giản.\n" +
                            "Nó cung cấp một vài tính năng như:\n" +
                            "- Xem cây thư mục hiện tại.\n" +
                            "- Tạo file với định dạng và nội dung tùy chỉnh.\n" +
                            "- Tạo thư mục mới.\n" +
                            "- Xem nội dung văn bản của file bằng cách nhấn 1 lần (hỗ trợ file tối đa 100kB)\n" +
                            "- Xem nội dung thông qua ứng dụng ngoài bằng cách chọn 'Open with' trong Option Menu\n" +
                            "- Chỉnh sửa nội dung file bằng cách nhấn giữ (hỗ trợ file tối đa 100kB).\n" +
                            "- Xóa những file và thư mục được chọn.\n" +
                            "- Xóa tất cả file và thư mục trong cây thư mục hiện tại.\n")
                    .setPositiveButton("OK", (dialogInterface, i) -> {})
                    .show();
        }
        return true;
    }

    private void sendRequestPermissionAllFileAccess(){
        try {
            Uri uri = Uri.parse("package:" + getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            startActivity(intent);
        } catch (Exception ex) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                checkPermissionAndInitData();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Write External Storage permission is needed to write files")
                        .setPositiveButton("Grant permission", (dialogInterface, i) -> {
                            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("Exit", (dialogInterface, i) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
        else if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                checkPermissionAndInitData();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Read External Storage permission is needed to read files")
                        .setPositiveButton("Grant permission", (dialogInterface, i) -> {
                            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("Exit", (dialogInterface, i) -> {
                            finish();
                        })
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        backPressCounter++;
        if (backPressCounter == 2) {
            super.onBackPressed(); // Exit the app
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressCounter = 0;
                }
            }, 2000);
        }
    }
}