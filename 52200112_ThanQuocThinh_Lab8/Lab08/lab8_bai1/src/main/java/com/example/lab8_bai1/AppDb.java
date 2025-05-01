package com.example.lab8_bai1;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Student.class}, version = 1)
public abstract class AppDb extends RoomDatabase {

    public abstract StudentDAO getStudentDAO();
}