package com.autocloset.mobile.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDbHelper extends SQLiteOpenHelper {

    String TAG = "UserDbHelper";

    static final String DATABASE_NAME = "user.db";

    static final int DATABASE_VERSION = 1;

    public UserDbHelper (Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_USER_TABLE = "CREATE TABLE " +
                UserContract.UserEntry.TABLE_NAME + "(" +
                UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserContract.UserEntry.USER_ID + " INTEGER NOT NULL " +
                ");";

        sqLiteDatabase.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
