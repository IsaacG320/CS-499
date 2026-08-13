package com.example.weighttrackingappproject_isaac_garcia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "weight_tracking.db";
    private static final int DATABASE_VERSION = 2;

    // =========================
    // USERS TABLE
    // =========================

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // =========================
    // WEIGHTS TABLE
    // =========================

    public static final String TABLE_WEIGHTS = "weights";
    public static final String COLUMN_WEIGHT_ID = "id";
    public static final String COLUMN_WEIGHT_USER_ID = "user_id";
    public static final String COLUMN_WEIGHT_DATE = "date";
    public static final String COLUMN_WEIGHT_VALUE = "weight";

    public AppDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);

        // Enforces relationships between database tables.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        createUsersTable(db);
        createWeightsTable(db);
        createWeightIndex(db);
    }

    private void createUsersTable(SQLiteDatabase db)
    {
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " ("
                        + COLUMN_USER_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_USERNAME
                        + " TEXT NOT NULL UNIQUE, "
                        + COLUMN_PASSWORD
                        + " TEXT NOT NULL"
                        + ");";

        db.execSQL(createUsersTable);
    }

    private void createWeightsTable(SQLiteDatabase db)
    {
        String createWeightsTable =
                "CREATE TABLE " + TABLE_WEIGHTS + " ("
                        + COLUMN_WEIGHT_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_WEIGHT_USER_ID
                        + " INTEGER NOT NULL, "
                        + COLUMN_WEIGHT_DATE
                        + " TEXT NOT NULL, "
                        + COLUMN_WEIGHT_VALUE
                        + " REAL NOT NULL CHECK("
                        + COLUMN_WEIGHT_VALUE + " >= 20 AND "
                        + COLUMN_WEIGHT_VALUE + " <= 1000), "
                        + "FOREIGN KEY("
                        + COLUMN_WEIGHT_USER_ID + ") REFERENCES "
                        + TABLE_USERS + "(" + COLUMN_USER_ID + ") "
                        + "ON DELETE CASCADE, "
                        + "UNIQUE("
                        + COLUMN_WEIGHT_USER_ID + ", "
                        + COLUMN_WEIGHT_DATE + ")"
                        + ");";

        db.execSQL(createWeightsTable);
    }

    private void createWeightIndex(SQLiteDatabase db)
    {
        String createIndex =
                "CREATE INDEX IF NOT EXISTS index_weights_user_date "
                        + "ON " + TABLE_WEIGHTS + " ("
                        + COLUMN_WEIGHT_USER_ID + ", "
                        + COLUMN_WEIGHT_DATE + " DESC"
                        + ");";

        db.execSQL(createIndex);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < 2)
        {
            /*
             * Version 1 weight records did not contain a user ID.
             * Because those records cannot be safely assigned to a specific
             * account, only the old weights table is recreated. Existing
             * registered user accounts are preserved.
             */
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);

            createWeightsTable(db);
            createWeightIndex(db);
        }
    }

    // =========================
    // USER REGISTRATION
    // =========================

    public boolean registerUser(String username, String password)
    {
        if (username == null || password == null)
        {
            return false;
        }

        String cleanUsername = username.trim();

        if (cleanUsername.isEmpty() || password.isEmpty())
        {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, cleanUsername);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(
                TABLE_USERS,
                null,
                values
        );

        return result != -1;
    }

    // =========================
    // USER LOGIN
    // =========================

    public long getUserId(String username, String password)
    {
        if (username == null || password == null)
        {
            return -1;
        }

        SQLiteDatabase db = getReadableDatabase();

        String[] columns =
                {
                        COLUMN_USER_ID
                };

        String selection =
                COLUMN_USERNAME + " = ? AND "
                        + COLUMN_PASSWORD + " = ?";

        String[] selectionArgs =
                {
                        username.trim(),
                        password
                };

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        long userId = -1;

        if (cursor.moveToFirst())
        {
            int userIdIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_ID);
            userId = cursor.getLong(userIdIndex);
        }

        cursor.close();
        return userId;
    }

    public boolean checkUserLogin(String username, String password)
    {
        return getUserId(username, password) != -1;
    }

    // =========================
    // CREATE WEIGHT
    // =========================

    public long insertWeight(long userId, String date, double weight)
    {
        if (userId <= 0 || date == null || date.trim().isEmpty())
        {
            return -1;
        }

        if (weight < 20 || weight > 1000)
        {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_USER_ID, userId);
        values.put(COLUMN_WEIGHT_DATE, date.trim());
        values.put(COLUMN_WEIGHT_VALUE, weight);

        return db.insert(
                TABLE_WEIGHTS,
                null,
                values
        );
    }

    // =========================
    // READ WEIGHTS
    // =========================

    public Cursor getWeightsForUser(long userId)
    {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns =
                {
                        COLUMN_WEIGHT_ID,
                        COLUMN_WEIGHT_DATE,
                        COLUMN_WEIGHT_VALUE
                };

        String selection = COLUMN_WEIGHT_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        return db.query(
                TABLE_WEIGHTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_WEIGHT_DATE + " DESC, "
                        + COLUMN_WEIGHT_ID + " DESC"
        );
    }

    // =========================
    // UPDATE WEIGHT
    // =========================

    public boolean updateWeight(
            long weightId,
            long userId,
            String date,
            double weight)
    {
        if (weightId <= 0
                || userId <= 0
                || date == null
                || date.trim().isEmpty())
        {
            return false;
        }

        if (weight < 20 || weight > 1000)
        {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_DATE, date.trim());
        values.put(COLUMN_WEIGHT_VALUE, weight);

        String selection =
                COLUMN_WEIGHT_ID + " = ? AND "
                        + COLUMN_WEIGHT_USER_ID + " = ?";

        String[] selectionArgs =
                {
                        String.valueOf(weightId),
                        String.valueOf(userId)
                };

        int rowsUpdated = db.update(
                TABLE_WEIGHTS,
                values,
                selection,
                selectionArgs
        );

        return rowsUpdated > 0;
    }

    // =========================
    // DELETE WEIGHT
    // =========================

    public boolean deleteWeight(long weightId, long userId)
    {
        SQLiteDatabase db = getWritableDatabase();

        String selection =
                COLUMN_WEIGHT_ID + " = ? AND "
                        + COLUMN_WEIGHT_USER_ID + " = ?";

        String[] selectionArgs =
                {
                        String.valueOf(weightId),
                        String.valueOf(userId)
                };

        int rowsDeleted = db.delete(
                TABLE_WEIGHTS,
                selection,
                selectionArgs
        );

        return rowsDeleted > 0;
    }
}