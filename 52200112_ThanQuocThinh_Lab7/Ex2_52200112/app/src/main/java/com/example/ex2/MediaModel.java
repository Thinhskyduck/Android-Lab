package com.example.ex2;

public class MediaModel {
    private String path; // Đường dẫn hệ thống tệp (VD: /storage/emulated/0/...)
    private long dateAdded;
    private boolean isSelected;
    private boolean isVideo; // <-- Thêm trường này

    // Constructor đã được cập nhật để nhận isVideo
    public MediaModel(String path, long dateAdded, boolean isVideo) {
        this.path = path;
        this.dateAdded = dateAdded;
        this.isVideo = isVideo; // <-- Gán giá trị
        this.isSelected = false;
    }

    // Getters
    public String getPath() { return path; }
    public long getDateAdded() { return dateAdded; }
    public boolean isSelected() { return isSelected; }
    public boolean isVideo() { return isVideo; } // <-- Getter cho isVideo

    // Setter
    public void setSelected(boolean selected) { isSelected = selected; }
}