package com.example.lab6_bai3;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static final String DATABASE_NAME = "EventManager.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "events";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PLACE = "place";
    private static final String KEY_DATETIME = "datetime";
    private static final String KEY_STATUS = "status";
    private static final int POS_ID = 0;
    private static final int POS_NAME = 1;
    private static final int POS_PLACE = 2;
    private static final int POS_DATETIME = 3;
    private static final int POS_STATUS = 4;

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String create_students_table =
                    String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",
                            TABLE_NAME, KEY_ID, KEY_NAME, KEY_PLACE, KEY_DATETIME, KEY_STATUS);
            db.execSQL(create_students_table);
        } catch(SQLiteException e) {
            throw new SQLiteException(e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_students_table = String.format("DROP TABLE IF EXISTS %s", TABLE_NAME);
        db.execSQL(drop_students_table);
        onCreate(db);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addEvent(Event event) {
        // Add event to database
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, event.getName());
        values.put(KEY_PLACE, event.getPlace());
        values.put(KEY_DATETIME, event.getDateTimeStringFormat());
        values.put(KEY_STATUS, event.isStatus());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Event getEvent(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.query(TABLE_NAME, null, KEY_ID + " = ?", new String[] { String.valueOf(id) },null, null, null);
        if(cursor == null)
            return null;
        else{
            cursor.moveToFirst();
            return new Event(
                    cursor.getInt(POS_ID),
                    cursor.getString(POS_NAME),
                    cursor.getString(POS_PLACE),
                    Event.parseDateTime(cursor.getString(POS_DATETIME)),
                    cursor.getString(POS_STATUS).equals("1")
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if(cursor.moveToFirst()){
            do {
                events.add(new Event(
                        cursor.getInt(POS_ID),
                        cursor.getString(POS_NAME),
                        cursor.getString(POS_PLACE),
                        Event.parseDateTime(cursor.getString(POS_DATETIME)),
                        cursor.getString(POS_STATUS).equals("1")
                ));
            } while(cursor.moveToNext());
        }
        return events;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, event.getName());
        values.put(KEY_PLACE, event.getPlace());
        values.put(KEY_DATETIME, event.getDateTimeStringFormat());
        values.put(KEY_STATUS, event.isStatus());
        db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(event.getId()) });
        db.close();
    }

    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[] { String.valueOf(event.getId()) });
        db.close();
    }

    public void clearEvent(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

}
