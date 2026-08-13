package com.example.weighttrackingappproject_isaac_garcia;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class AppDatabaseHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "weight_tracking.db";
    private static final int DATABASE_VERSION = 1;

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Weights table
    public static final String TABLE_WEIGHTS = "weights";
    public static final String COLUMN_WEIGHT_ID = "id";
    public static final String COLUMN_WEIGHT_DATE = "date";
    public static final String COLUMN_WEIGHT_VALUE = "weight";

    public AppDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Create users table
        String createUsers = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT);";

        // Create weights table
        String createWeights = "CREATE TABLE " + TABLE_WEIGHTS + " ("
                + COLUMN_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_WEIGHT_DATE + " TEXT, "
                + COLUMN_WEIGHT_VALUE + " TEXT);";

        db.execSQL(createUsers);
        db.execSQL(createWeights);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        onCreate(db);
    }

    // =========================
    // USER LOGIN / REGISTER
    // =========================

    public boolean registerUser(String username, String password)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUserLogin(String username, String password)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_USER_ID +
                " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // =========================
    // WEIGHT CRUD
    // =========================

    public long insertWeight(String date, String weight)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_DATE, date);
        values.put(COLUMN_WEIGHT_VALUE, weight);

        return db.insert(TABLE_WEIGHTS, null, values);
    }

    public Cursor getAllWeights()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_WEIGHTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_WEIGHT_ID + " DESC"
        );
    }

    public boolean deleteWeight(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_WEIGHTS,
                COLUMN_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }
}

