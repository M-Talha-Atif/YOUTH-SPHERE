package com.event.youthSphere.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import com.event.youthSphere.models.participant.EnrollmentModel;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "youthSphere.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ENROLLMENTS = "enrollments";

    private static final String COL_ID = "enrollmentId";
    private static final String COL_PARTICIPANT_ID = "participantId";
    private static final String COL_ACTIVITY_ID = "activityId";
    private static final String COL_ENROLLED_AT = "enrolledAt";
    private static final String COL_NAME = "name";
    private static final String COL_DOMAIN = "domain";
    private static final String COL_DESCRIPTION = "description";

    public EnrollmentDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ENROLLMENTS + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_PARTICIPANT_ID + " TEXT, " +
                COL_ACTIVITY_ID + " TEXT, " +
                COL_ENROLLED_AT + " INTEGER, " +
                COL_NAME + " TEXT, " +
                COL_DOMAIN + " TEXT, " +
                COL_DESCRIPTION + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertEnrollment(EnrollmentModel model) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID, model.getEnrollmentId());
        values.put(COL_PARTICIPANT_ID, model.getParticipantId());
        values.put(COL_ACTIVITY_ID, model.getActivityId());
        values.put(COL_ENROLLED_AT, model.getEnrolledAt());
        values.put(COL_NAME, model.getName());
        values.put(COL_DOMAIN, model.getDomain());
        values.put(COL_DESCRIPTION, model.getDescription());

        db.insertWithOnConflict(TABLE_ENROLLMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteEnrollment(String enrollmentId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ENROLLMENTS, COL_ID + "=?", new String[]{enrollmentId});
        db.close();
    }

    public List<EnrollmentModel> getEnrollmentsByParticipantId(String participantId) {
        List<EnrollmentModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_ENROLLMENTS, null,
                COL_PARTICIPANT_ID + "=?",
                new String[]{participantId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                EnrollmentModel model = new EnrollmentModel();
                model.setEnrollmentId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
                model.setParticipantId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PARTICIPANT_ID)));
                model.setActivityId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTIVITY_ID)));
                model.setEnrolledAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENROLLED_AT)));
                model.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                model.setDomain(cursor.getString(cursor.getColumnIndexOrThrow(COL_DOMAIN)));
                model.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
                list.add(model);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }
}
