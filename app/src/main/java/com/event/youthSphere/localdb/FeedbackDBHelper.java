package com.event.youthSphere.localdb;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import com.event.youthSphere.models.participant.FeedbackModel;


public class FeedbackDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "youth_sphere.db";
    private static final int DB_VERSION = 1;

    public FeedbackDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS feedback (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "activityId TEXT," +
                "participantId TEXT," +
                "participantName TEXT," +
                "instructorId TEXT," +
                "parentId TEXT," +
                "rating INTEGER," +
                "comment TEXT," +
                "timestamp INTEGER" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS feedback");
        onCreate(db);
    }

    public boolean feedbackExists(String activityId, String participantId, String instructorId, String parentId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "activityId=? AND participantId=? AND " +
                (instructorId != null ? "instructorId=?" : "parentId=?");

        String[] selectionArgs = instructorId != null ?
                new String[]{activityId, participantId, instructorId} :
                new String[]{activityId, participantId, parentId};

        Cursor cursor = db.query("feedback", null, selection, selectionArgs, null, null, null);
        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();
        return exists;
    }

    public void insertFeedback(FeedbackModel feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("activityId", feedback.getActivityId());
        values.put("participantId", feedback.getParticipantId());
        values.put("participantName", feedback.getParticipantName());
        values.put("instructorId", feedback.getInstructorId());
        values.put("parentId", feedback.getParentId());
        values.put("rating", feedback.getRating());
        values.put("comment", feedback.getComment());
        values.put("timestamp", feedback.getTimestamp().getTime());

        db.insert("feedback", null, values);
        db.close();
    }
}

