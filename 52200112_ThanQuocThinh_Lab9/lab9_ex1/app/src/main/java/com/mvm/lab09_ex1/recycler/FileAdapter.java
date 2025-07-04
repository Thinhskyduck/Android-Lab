package com.mvm.lab09_ex1.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mvm.lab09_ex1.R;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileHolder> {

    private List<DownloadFile> files;

    public FileAdapter() {
        this.files = new ArrayList<>();
    }

    public List<DownloadFile> getFiles() {
        return files;
    }

    public void setFiles(List<DownloadFile> files) {
        this.files.clear();
        this.files.addAll(files);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_file, parent, false);
        return new FileHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull FileHolder holder, int position) {
        DownloadFile file = files.get(position);
        holder.bind(file, position);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void add(DownloadFile downloadFile) {
        files.add(0, downloadFile);
        notifyItemInserted(0);
    }

    public static class FileHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView name, size, status;
        ProgressBar progressBar;

        public FileHolder(@NonNull View root) {
            super(root);

            icon = root.findViewById(R.id.icon);
            name = root.findViewById(R.id.name);
            size = root.findViewById(R.id.size);
            status = root.findViewById(R.id.status);
            progressBar = root.findViewById(R.id.progressBar);
        }

        public void bind(DownloadFile file, int position) {
            Log.d("FileAdapter", "Binding file: " + file.getName() + ", Progress: " + file.getProgress() + ", Status: Waiting=" + file.isWaiting() + ", Completed=" + file.isCompleted() + ", Failed=" + file.isFailed());

            if (file.getName() == null || file.getName().isEmpty()) {
                icon.setImageResource(R.drawable.icon_other);
                name.setText("Reading file name...");
                size.setText("Reading file size...");
            } else {
                icon.setImageResource(file.getIcon());
                name.setText(file.getName());
                size.setText(file.getDisplaySize());
            }

            int colorSuccess = ContextCompat.getColor(icon.getContext(), R.color.success);
            int colorFail = ContextCompat.getColor(icon.getContext(), R.color.fail);
            int colorWaiting = ContextCompat.getColor(icon.getContext(), R.color.waiting);

            if (file.isWaiting()) {
                status.setText("Waiting");
                status.setTextColor(colorWaiting);
                status.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else if (file.isCompleted()) {
                status.setText("Complete");
                status.setTextColor(colorSuccess);
                status.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else if (file.isFailed()) {
                status.setText("Failed");
                status.setTextColor(colorFail);
                status.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setProgress(file.getProgress());
                status.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }
}