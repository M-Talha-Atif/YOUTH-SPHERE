package com.event.youthSphere.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local SQLite helper for activity backup and offline access.
 * After adding to Firebase, also add to local SQL via insertOrUpdate.
 */
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
                "days TEXT, " +
                "maxParticipants INTEGER, " +
                "month TEXT, " +
                "instructorId TEXT, " +
                "registeredCount INTEGER, " +
                "status TEXT)";
        db.execSQL(create);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS activities");
        onCreate(db);
    }

    /**
     * Insert a new activity or update if it already exists.
     * Call this after successfully adding activity in Firebase.
     */
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
        values.put("status", "not updated");

        db.insertWithOnConflict("activities", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * Get all activities from local database.
     */
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
        db.close();
        return list;
    }

    /**
     * Update the instructorId for a given activity.
     * @param activityId The activity's id.
     * @param instructorId The new instructor's id.
     * @return number of rows affected.
     */
    public int updateInstructorId(String activityId, String instructorId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("instructorId", instructorId);
        int rows = db.update("activities", values, "id=?", new String[]{activityId});
        db.close();
        return rows;
    }

    /**
     * Delete an activity by id. Also removes the activityId from instructor's activityIds in local UserDB.
     * @param activityId The activity's id to delete.
     * @param context The context to create UserDBHelper for instructor update.
     * @return number of rows deleted.
     */
    public int deleteActivity(String activityId, Context context) {
        SQLiteDatabase db = getWritableDatabase();

        // First, get the instructorId for the activity to update instructor's activityIds field
        Cursor cursor = db.query("activities", new String[]{"instructorId"}, "id=?", new String[]{activityId}, null, null, null);
        String instructorId = null;
        if (cursor != null && cursor.moveToFirst()) {
            instructorId = cursor.getString(0);
            cursor.close();
        }

        int rowsDeleted = db.delete("activities", "id=?", new String[]{activityId});
        db.close();

        // Update instructor's activityIds field for consistency
        if (instructorId != null && !instructorId.isEmpty()) {
            UserDBHelper userDBHelper = new UserDBHelper(context);
            android.content.ContentValues instructor = userDBHelper.getUserByUid(instructorId);
            if (instructor != null) {
                // Remove this activityId from instructor's activityIds field
                String activityIds = instructor.getAsString("activityIds");
                if (activityIds != null && !activityIds.isEmpty()) {
                    List<String> idsList = new ArrayList<>(Arrays.asList(activityIds.split(",")));
                    idsList.remove(activityId);
                    String updatedIds = String.join(",", idsList);
                    userDBHelper.updateInstructor(
                            instructorId,
                            null, // fullName
                            null, // email
                            null, // password
                            updatedIds,
                            null, // fieldOfSpecialization
                            null  // employmentStartDate
                    );
                }
            }
        }

        return rowsDeleted;
    }

    public boolean updateActivityStatus(String activityId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int rows = db.update("activities", values, "id=?", new String[]{activityId});
        db.close();
        return rows > 0;
    }

}