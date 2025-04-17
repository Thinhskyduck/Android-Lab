package com.example.lab6_bai4;

import java.io.File;

public class StorageObject {
    private File file;
    private boolean isSelected;

    public StorageObject(File file) {
        this.file = file;
        this.isSelected = false;
    }

    public StorageObject(File file, boolean isSelected) {
        this.file = file;
        this.isSelected = isSelected;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
