package com.event.youthSphere.local;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.event.youthSphere.models.users.ParticipantUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class ParticipantDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "participants.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "participants";

    private static final String COL_UID = "uid";
    private static final String COL_EMAIL = "email";
    private static final String COL_FULL_NAME = "fullName";
    private static final String COL_ACTIVITY_IDS = "activityIds"; // comma-separated

    public ParticipantDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_UID + " TEXT PRIMARY KEY, " +
                COL_EMAIL + " TEXT, " +
                COL_FULL_NAME + " TEXT, " +
                COL_ACTIVITY_IDS + " TEXT)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertParticipant(ParticipantUser user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_UID, user.getUid());
        values.put(COL_EMAIL, user.getEmail());
        values.put(COL_FULL_NAME, user.getFullName());
        values.put(COL_ACTIVITY_IDS, TextUtils.join(",", user.getActivityIds()));

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<ParticipantUser> getAllParticipants() {
        List<ParticipantUser> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String uid = cursor.getString(cursor.getColumnIndexOrThrow(COL_UID));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME));
            String idsString = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTIVITY_IDS));
            List<String> activityIds = Arrays.asList(idsString.split(","));

            list.add(new ParticipantUser(uid, email, fullName, activityIds));
        }
        cursor.close();
        db.close();
        return list;
    }
}
