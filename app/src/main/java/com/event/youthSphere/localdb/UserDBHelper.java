package com.event.youthSphere.localdb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite helper class for fallback user storage when Firebase is unavailable.
 * Supports signup and login for multiple roles.
 */
public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "user_db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_USERS = "users";

    // Common columns
    private static final String COL_UID = "uid";
    private static final String COL_EMAIL = "email";
    private static final String COL_FULL_NAME = "fullName";
    private static final String COL_PASSWORD = "password";
    private static final String COL_ROLE = "role";

    // Role-specific columns
    private static final String COL_CHILD_IDS = "childIds"; // For Parent
    private static final String COL_ACTIVITY_IDS = "activityIds"; // For Participant & Instructor
    private static final String COL_FIELD_OF_SPEC = "fieldOfSpecialization"; // For Instructor
    private static final String COL_EMPLOYMENT_START_DATE = "employmentStartDate"; // For Instructor

    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates user table with all required columns.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_UID + " TEXT PRIMARY KEY,"
                + COL_EMAIL + " TEXT UNIQUE,"
                + COL_FULL_NAME + " TEXT,"
                + COL_PASSWORD + " TEXT,"
                + COL_ROLE + " TEXT,"
                + COL_CHILD_IDS + " TEXT,"
                + COL_ACTIVITY_IDS + " TEXT,"
                + COL_FIELD_OF_SPEC + " TEXT,"
                + COL_EMPLOYMENT_START_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Adds a new user to SQLite.
     * @param uid Unique ID for user (can be random UUID)
     * @param email User's email
     * @param fullName User's full name
     * @param password User's password (store hash ideally)
     * @param role UserRole string
     * @param childIds Comma-separated child IDs (for Parent)
     * @param activityIds Comma-separated activity IDs (for Participant/Instructor)
     * @param fieldOfSpecialization Instructor field
     * @param employmentStartDate Instructor start date
     * @return row ID or -1 if failure
     */
    public long addUser(String uid, String email, String fullName, String password, String role,
                        String childIds, String activityIds, String fieldOfSpecialization, String employmentStartDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_UID, uid);
        values.put(COL_EMAIL, email);
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_PASSWORD, password);
        values.put(COL_ROLE, role);
        values.put(COL_CHILD_IDS, childIds);
        values.put(COL_ACTIVITY_IDS, activityIds);
        values.put(COL_FIELD_OF_SPEC, fieldOfSpecialization);
        values.put(COL_EMPLOYMENT_START_DATE, employmentStartDate);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    /**
     * Login with email and password. Returns user role if success.
     * @param email User email
     * @param password User password
     * @return UserRole string or null if invalid
     */
    public String login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_ROLE},
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        String role = null;
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE));
            cursor.close();
        }
        db.close();
        return role;
    }

    /**
     * Fetches all user info by email.
     * @param email Email
     * @return User info as ContentValues or null if not found
     */
    public ContentValues getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        ContentValues result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = new ContentValues();
            for (String col : cursor.getColumnNames()) {
                int idx = cursor.getColumnIndex(col);
                if (idx != -1) {
                    result.put(col, cursor.getString(idx));
                }
            }
            cursor.close();
        }
        db.close();
        return result;
    }

    /**
     * Checks if user exists by email.
     */
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_EMAIL},
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // -------- Instructor-specific methods --------

    /**
     * Edit/update instructor user by uid.
     * Only updates instructor-specific fields and optionally common fields.
     * @param uid Instructor's UID
     * @param fullName New full name (nullable for no change)
     * @param email New email (nullable for no change)
     * @param password New password (nullable for no change)
     * @param activityIds New comma-separated activity IDs (nullable for no change)
     * @param fieldOfSpecialization New field of specialization (nullable for no change)
     * @param employmentStartDate New employment start date (nullable for no change)
     * @return number of rows updated
     */
    public int updateInstructor(String uid, String fullName, String email, String password,
                                String activityIds, String fieldOfSpecialization, String employmentStartDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Only update non-null fields
        if (fullName != null) values.put(COL_FULL_NAME, fullName);
        if (email != null) values.put(COL_EMAIL, email);
        if (password != null) values.put(COL_PASSWORD, password);
        if (activityIds != null) values.put(COL_ACTIVITY_IDS, activityIds);
        if (fieldOfSpecialization != null) values.put(COL_FIELD_OF_SPEC, fieldOfSpecialization);
        if (employmentStartDate != null) values.put(COL_EMPLOYMENT_START_DATE, employmentStartDate);

        int rows = db.update(TABLE_USERS, values,
                COL_UID + "=? AND " + COL_ROLE + "=?",
                new String[]{uid, "INSTRUCTOR"});
        db.close();
        return rows;
    }

    /**
     * Delete instructor user by UID.
     * @param uid Instructor's UID
     * @return number of rows deleted
     */
    public int deleteInstructor(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS,
                COL_UID + "=? AND " + COL_ROLE + "=?",
                new String[]{uid, "INSTRUCTOR"});
        db.close();
        return rows;
    }

    /**
     * Get all instructors.
     * @return List of ContentValues for each instructor.
     */
    public List<ContentValues> getAllInstructors() {
        List<ContentValues> instructors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COL_ROLE + "=?",
                new String[]{"INSTRUCTOR"},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ContentValues values = new ContentValues();
                for (String col : cursor.getColumnNames()) {
                    int idx = cursor.getColumnIndex(col);
                    if (idx != -1) {
                        values.put(col, cursor.getString(idx));
                    }
                }
                instructors.add(values);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return instructors;
    }

    /**
     * Fetches user details by UID.
     * @param uid Unique user ID
     * @return User info as ContentValues or null if not found
     */
    public ContentValues getUserByUid(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                null,
                COL_UID + "=?",
                new String[]{uid},
                null, null, null);

        ContentValues result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = new ContentValues();
            for (String col : cursor.getColumnNames()) {
                int idx = cursor.getColumnIndex(col);
                if (idx != -1) {
                    result.put(col, cursor.getString(idx));
                }
            }
            cursor.close();
        }
        db.close();
        return result;
    }

    /**
     * Removes an activity ID from the instructor's activityIds field.
     * @param instructorId UID of the instructor
     * @param activityId Activity ID to remove
     * @return number of rows updated
     */
    public int removeActivityFromInstructor(String instructorId, String activityId) {
        // Fetch the instructor's current data
        ContentValues instructor = getUserByUid(instructorId);
        if (instructor == null) return 0;

        String existingActivities = instructor.getAsString(COL_ACTIVITY_IDS);
        if (existingActivities == null || existingActivities.isEmpty()) return 0;

        // Split, remove the ID, and re-join
        String[] activityArray = existingActivities.split(",");
        List<String> updatedActivities = new ArrayList<>();
        for (String id : activityArray) {
            if (!id.trim().equals(activityId)) {
                updatedActivities.add(id.trim());
            }
        }

        // Prepare the updated activityIds string
        String updatedActivityIds = String.join(",", updatedActivities);

        // Update the instructor locally
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ACTIVITY_IDS, updatedActivityIds);

        int rows = db.update(TABLE_USERS, values,
                COL_UID + "=? AND " + COL_ROLE + "=?",
                new String[]{instructorId, "INSTRUCTOR"});
        db.close();
        return rows;
    }

    @SuppressLint("Range")
    public List<Map<String, String>> getActivitiesByInstructorId(String instructorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("activities",
                null,
                "instructorId=?",
                new String[]{instructorId},
                null, null, null);

        List<Map<String, String>> result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Map<String, String> activity = new HashMap<>();
                for (String col : cursor.getColumnNames()) {
                    activity.put(col, cursor.getString(cursor.getColumnIndex(col)));
                }
                result.add(activity);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return result;
    }

    // parent functions

    /**
     * Adds a childId to a parent user’s childIds field (comma-separated).
     * @param parentId Parent UID
     * @param childId Child UID to add
     * @return true if added or already present, false if failed
     */
    public boolean addChildToParent(String parentId, String childId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Fetch current childIds
        ContentValues parent = getUserByUid(parentId);
        if (parent == null || !parent.getAsString(COL_ROLE).equals("PARENT")) {
            return false;
        }

        String existingChildIds = parent.getAsString(COL_CHILD_IDS);
        List<String> updatedChildList = new ArrayList<>();

        if (existingChildIds != null && !existingChildIds.isEmpty()) {
            for (String id : existingChildIds.split(",")) {
                if (!id.trim().isEmpty()) {
                    updatedChildList.add(id.trim());
                }
            }
        }

        // 2. Check if already exists
        if (updatedChildList.contains(childId)) {
            return true; // Already added
        }

        // 3. Add and update
        updatedChildList.add(childId);
        String updatedChildIds = String.join(",", updatedChildList);

        ContentValues updateValues = new ContentValues();
        updateValues.put(COL_CHILD_IDS, updatedChildIds);

        int rows = db.update(TABLE_USERS, updateValues,
                COL_UID + "=? AND " + COL_ROLE + "=?",
                new String[]{parentId, "PARENT"});
        db.close();
        return rows > 0;
    }


    public boolean isChildAlreadyLinkedToAnyParent(String childId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_UID},
                COL_ROLE + "=?",
                new String[]{"PARENT"},
                null, null, null);

        boolean found = false;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String childIdsStr = cursor.getString(cursor.getColumnIndex(COL_CHILD_IDS));
                if (childIdsStr != null) {
                    for (String id : childIdsStr.split(",")) {
                        if (id.trim().equals(childId)) {
                            found = true;
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext() && !found);
            cursor.close();
        }
        db.close();
        return found;
    }





}