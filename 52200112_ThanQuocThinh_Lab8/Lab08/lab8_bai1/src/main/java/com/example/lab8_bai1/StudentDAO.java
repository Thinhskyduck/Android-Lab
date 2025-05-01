package com.example.lab8_bai1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudentDAO {
    @Insert
    public void insert(Student student);

    @Update
    public void update(Student student);

    @Delete
    public void delete(Student student);

    @Query("SELECT * FROM student")
    public List<Student> getAllStudents();

    @Query("SELECT * FROM student WHERE id = :id")
    public Student getStudentById(int id);

}