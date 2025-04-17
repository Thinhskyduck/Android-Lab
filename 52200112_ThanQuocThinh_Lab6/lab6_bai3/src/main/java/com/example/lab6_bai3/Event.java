package com.example.lab6_bai3;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event implements java.io.Serializable {

    private static int ID_AUTO_INCREAMENT = 0;

    private int id;
    private String name, place;
    private LocalDateTime dateTime;
    private boolean status;

    public Event(String name, String place, LocalDateTime dateTime, boolean status) {
        ++ID_AUTO_INCREAMENT;
        this.id = ID_AUTO_INCREAMENT;
        this.name = name;
        this.place = place;
        this.dateTime = dateTime;
        this.status = status;
    }

    public Event(int id, String name, String place, LocalDateTime dateTime, boolean status) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDateTimeStringFormat(){
        return getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDateStringFormat(){
        return getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getTimeStringFormat(){
        return getDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDateTime parseDateTime(String dateTime){
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean isStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void replace(Event event){
        this.id = event.getId();
        this.name = event.getName();
        this.place = event.getPlace();
        this.dateTime = event.getDateTime();
        this.status = event.isStatus();
    }
}
