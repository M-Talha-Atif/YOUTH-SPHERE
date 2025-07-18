package com.event.youthSphere.localdb;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.event.youthSphere.models.users.InstructorUser;
import java.util.ArrayList;
import java.util.List;
public class InstructorDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "instructor_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "instructors";

    public InstructorDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "uid TEXT PRIMARY KEY, " +
                "email TEXT, " +
                "fullName TEXT, " +
                "fieldOfSpecialization TEXT, " +
                "employmentStartDate TEXT" +
                ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insertOrUpdate(InstructorUser instructor) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", instructor.getUid());
        values.put("email", instructor.getEmail());
        values.put("fullName", instructor.getFullName());
        values.put("fieldOfSpecialization", instructor.getFieldOfSpecialization());
        values.put("employmentStartDate", instructor.getEmploymentStartDate());
        db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<InstructorUser> getAllInstructors() {
        List<InstructorUser> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE, null);
        while (cursor.moveToNext()) {
            InstructorUser i = new InstructorUser();
            i.setUid(cursor.getString(0));
            i.setEmail(cursor.getString(1));
            i.setFullName(cursor.getString(2));
            i.setFieldOfSpecialization(cursor.getString(3));
            i.setEmploymentStartDate(cursor.getString(4));
            list.add(i);
        }
        cursor.close();
        return list;
    }
}

