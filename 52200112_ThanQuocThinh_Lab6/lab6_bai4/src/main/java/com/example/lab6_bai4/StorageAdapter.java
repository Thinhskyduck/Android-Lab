package com.example.lab6_bai4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.StorageViewHolder> {

    public class StorageViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView ivIcon;
        private AppCompatTextView tvName;
        private AppCompatCheckBox cbSelect;

        public StorageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Set view variables
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            // Set listener on click
            ivIcon.setOnClickListener(v -> choose());
            tvName.setOnClickListener(v -> choose());
            // Set listener on long click
            ivIcon.setOnLongClickListener(v -> {
                edit();
                return true;
            });
            tvName.setOnLongClickListener(v -> {
                edit();
                return true;
            });
            // Set listener on checkbox
            cbSelect.setOnClickListener(v -> {
                mFilesAndFolders.get(getAdapterPosition()).setSelected(cbSelect.isChecked());
            });
        }

        private void choose(){
            String path = mFilesAndFolders.get(getAdapterPosition()).getFile().getPath();
            if (!mMainActivity.checkFileThenOpen(path)){
                mMainActivity.setPath(path);
            }
        }

        private void edit(){
            String path = mFilesAndFolders.get(getAdapterPosition()).getFile().getPath();
            mMainActivity.openDialogEditFile(path);
        }

    }

    private String path;
    private final Context mContex;
    private final MainActivity mMainActivity;
    private ArrayList<StorageObject> mFilesAndFolders = new ArrayList<>();

    public StorageAdapter(Context mContex, String path, MainActivity mMainActivity) {
        this.path = path;
        this.mContex = mContex;
        this.mMainActivity = mMainActivity;
        for (File f: Objects.requireNonNull(new File(path).listFiles())){
            mFilesAndFolders.add(new StorageObject(f));
        }
    }

    public void setPath(String path){
        this.path = path;
        mFilesAndFolders.clear();
        File[] files = new File(path).listFiles();
        if (files == null || files.length == 0) {
            mMainActivity.setViewEmpty(true);
        }
        else {
            Arrays.sort(files, new FileComparator());
            for (File f: files){
                mFilesAndFolders.add(new StorageObject(f));
            }
            mMainActivity.setViewEmpty(false);
        }
        notifyDataSetChanged();
    }

    public void refresh(){
        setPath(this.path);
    }

    public void backToParentPath(){
        if (this.path.equals(MainActivity.ROOT_FOLDER)){
            Toast.makeText(mContex, "You are at root folder!", Toast.LENGTH_SHORT).show();
            return;
        }
        String path = new File(this.path).getParent();
        mMainActivity.setPath(path);
    }

    private File[] getSelectedFiles(){
        return mFilesAndFolders.stream()
                .filter(StorageObject::isSelected)
                .map(StorageObject::getFile)
                .toArray(File[]::new);
    }

    public void tryDeleteSelectedFiles(){
        // Lấy danh sách file đã chọn từ mFilesAndFolders
        File[] files = getSelectedFiles();
        if (files != null && files.length > 0){
            mMainActivity.askUserConfirmDelete(files);
        } else {
            Toast.makeText(mContex, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    public void tryDeleteAllFiles(){
        // Lấy danh sách file đã chọn từ mFilesAndFolders
        File[] files = mFilesAndFolders.stream()
                .map(StorageObject::getFile)
                .toArray(File[]::new);
        if (files.length > 0){
            mMainActivity.askUserConfirmDelete(files);
        } else {
            Toast.makeText(mContex, "No file exists in current directory!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void tryReadingFileOutside(){
        File[] files = getSelectedFiles();
        if (files != null && files.length > 0){
            File file = files[0];
            if (file.isFile()){
                // Tạo Uri từ đường dẫn file
                Uri uri = mMainActivity.getUriFromPath(file.getPath());
                // Lấy kiểu MIME của file
                String mime = mMainActivity.getMimeType(file.getPath());
                // Tạo intent để mở file
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, mime);
                // Kiểm tra xem có ứng dụng nào có thể mở file không
                if (intent.resolveActivity(mMainActivity.getPackageManager()) != null){
                    mMainActivity.startActivity(intent);
                } else {
                    Toast.makeText(mContex, "No app available can open this type file: " + file.getName(), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(mContex, file.getName() + " is not a file!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(mContex, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public StorageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContex);
        View customView = inflater.inflate(R.layout.storage_line_view, parent, false);
        return new StorageViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull StorageViewHolder holder, int position) {
        StorageObject obj = mFilesAndFolders.get(position);
        // Set data for view
        holder.tvName.setText(obj.getFile().getName());
        if (obj.getFile().isDirectory()) {
            holder.ivIcon.setImageResource(R.drawable.ic_folder_24);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_file_24);
        }
        holder.cbSelect.setChecked(obj.isSelected());
        // Set listener for checkbox

    }

    @Override
    public int getItemCount() {
        return mFilesAndFolders.size();
    }

}
