package com.event.youthSphere.localdb;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import com.event.youthSphere.models.activity.ActivityImageModel;

import java.util.ArrayList;
import java.util.List;

public class ActivityImageDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "youthSphere.db";
    private static final int DB_VERSION = 1;

    public ActivityImageDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS activity_images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "activityId TEXT, " +
                "imageUrl TEXT, " +
                "uploadDate TEXT, " +
                "domain TEXT, " +
                "name TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS activity_images");
        onCreate(db);
    }

    public void insertImage(String activityId, ActivityImageModel image) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activityId", activityId);
        values.put("imageUrl", image.getImageUrl());
        values.put("uploadDate", image.getUploadDate());
        values.put("domain", image.getDomain());
        values.put("name", image.getName());
        db.insert("activity_images", null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<ActivityImageModel> getImagesByActivityId(String activityId) {
        List<ActivityImageModel> images = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("activity_images", null, "activityId=?",
                new String[]{activityId}, null, null, "uploadDate DESC");

        if (cursor.moveToFirst()) {
            do {
                ActivityImageModel model = new ActivityImageModel();
                model.setImageUrl(cursor.getString(cursor.getColumnIndex("imageUrl")));
                model.setUploadDate(cursor.getString(cursor.getColumnIndex("uploadDate")));
                model.setDomain(cursor.getString(cursor.getColumnIndex("domain")));
                model.setName(cursor.getString(cursor.getColumnIndex("name")));
                images.add(model);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return images;
    }
}

