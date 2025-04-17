package com.example.lab6_bai4;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {
    @Override
    public int compare(File file1, File file2) {
        boolean isDir1 = file1.isDirectory();
        boolean isDir2 = file2.isDirectory();

        // Nếu một file là thư mục, một file là file thì thư mục sẽ đứng trước
        if (isDir1 != isDir2) {
            return isDir2 ? 1 : -1;
        }

        // Nếu cả hai đều là thư mục hoặc cả hai đều là file thì so sánh tên
        return file1.getName().compareToIgnoreCase(file2.getName());
    }
}