package com.event.youthSphere.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.event.youthSphere.models.activity.ActivityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class ActivityDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "activities.db";
    private static final int DB_VERSION = 1;

    public ActivityDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE activities (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "domain TEXT, " +
                "minAge INTEGER, " +
                "maxAge INTEGER, " +
                "description TEXT, " +
                "days TEXT, " + // comma-separated
                "maxParticipants INTEGER, " +
                "month TEXT, " +
                "instructorId TEXT, " +
                "registeredCount INTEGER)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS activities");
        onCreate(db);
    }

    public void insertOrUpdate(ActivityModel model) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", model.getId());
        values.put("name", model.getName());
        values.put("domain", model.getDomain());
        values.put("minAge", model.getMinAge());
        values.put("maxAge", model.getMaxAge());
        values.put("description", model.getDescription());
        values.put("days", String.join(",", model.getDays()));
        values.put("maxParticipants", model.getMaxParticipants());
        values.put("month", model.getMonth());
        values.put("instructorId", model.getInstructorId());
        values.put("registeredCount", model.getRegisteredCount());

        db.insertWithOnConflict("activities", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ActivityModel> getAllActivities() {
        List<ActivityModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM activities", null);

        while (cursor.moveToNext()) {
            ActivityModel model = new ActivityModel();
            model.setId(cursor.getString(0));
            model.setName(cursor.getString(1));
            model.setDomain(cursor.getString(2));
            model.setMinAge(cursor.getInt(3));
            model.setMaxAge(cursor.getInt(4));
            model.setDescription(cursor.getString(5));
            model.setDays(Arrays.asList(cursor.getString(6).split(",")));
            model.setMaxParticipants(cursor.getInt(7));
            model.setMonth(cursor.getString(8));
            model.setInstructorId(cursor.getString(9));
            model.setRegisteredCount(cursor.getInt(10));
            list.add(model);
        }
        cursor.close();
        return list;
    }
}


