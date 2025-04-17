package com.example.ex2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
// Bỏ import không cần thiết: import com.example.ex2.R;

import java.util.ArrayList;
import java.util.List;
// Bỏ import không cần thiết: import java.util.function.Consumer;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private List<MediaModel> mediaList;
    private Context context;

    public MediaAdapter(Context context, List<MediaModel> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    // ViewHolder đã được cập nhật
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        CheckBox checkBox;
        ImageView videoIndicator; // <-- Thêm ImageView cho biểu tượng video

        public ViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            checkBox = view.findViewById(R.id.checkbox);
            videoIndicator = view.findViewById(R.id.video_indicator); // <-- Tìm ID của biểu tượng
        }
    }

    @NonNull // Thêm annotation bị thiếu
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Thêm annotation bị thiếu
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // Thêm annotation bị thiếu
        MediaModel item = mediaList.get(position);

        // Load thumbnail (Glide hoạt động cho cả ảnh và video)
        Glide.with(context)
                .load(item.getPath())
                .centerCrop() // Đảm bảo ảnh/video vừa vặn
                .placeholder(R.drawable.ic_launcher_background) // Thêm placeholder nếu muốn
                .error(android.R.drawable.stat_notify_error) // Thêm error drawable nếu muốn
                .into(holder.thumbnail);

        holder.checkBox.setChecked(item.isSelected());

        // Quan trọng: Đặt lại listener mỗi lần bind để tránh lỗi tái sử dụng ViewHolder
        holder.checkBox.setOnCheckedChangeListener(null); // Xóa listener cũ
        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lấy đúng vị trí hiện tại của item trong adapter
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                mediaList.get(currentPosition).setSelected(isChecked);
            }
        });


        // Hiển thị hoặc ẩn biểu tượng video dựa trên isVideo
        if (item.isVideo()) {
            holder.videoIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.videoIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public List<MediaModel> getSelectedItems() {
        List<MediaModel> selected = new ArrayList<>();
        for (MediaModel m : mediaList) {
            if (m.isSelected()) {
                selected.add(m);
            }
        }
        return selected;
    }

    // Hàm này không còn cần thiết nếu bạn load lại toàn bộ list sau khi xóa
    // Nếu muốn tối ưu hơn thì mới cần dùng
    /*
    public void removeItems(List<MediaModel> selected) {
        // Cần cẩn thận khi xóa trực tiếp và dùng notifyDataSetChanged
        // Cách an toàn hơn là dùng DiffUtil hoặc load lại list
        mediaList.removeAll(selected);
        notifyDataSetChanged(); // Có thể gây lỗi nếu index thay đổi không đúng cách
    }
    */

    // Thêm hàm cập nhật dữ liệu (nếu bạn dùng cách load lại list ít hơn)
    public void updateData(List<MediaModel> newMediaList) {
        this.mediaList.clear();
        this.mediaList.addAll(newMediaList);
        notifyDataSetChanged(); // Hoặc dùng DiffUtil để hiệu quả hơn
    }
}