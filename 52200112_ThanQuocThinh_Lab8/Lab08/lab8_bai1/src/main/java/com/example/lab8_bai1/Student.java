package com.example.lab8_bai1;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "student")
public class Student implements Serializable {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @PrimaryKey(autoGenerate = true)
    @NotNull
    int id;
    String name;
    String email;
    String phone;

    public Student(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("Student {id=%d, name='%s', email='%s', phone='%s'}", this.id, this.name, this.email, this.phone);
    }

    public void update(Student s){
        this.name = s.getName();
        this.email = s.getEmail();
        this.phone = s.getPhone();
    }

    public void update(String name, String email, String phone){
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
