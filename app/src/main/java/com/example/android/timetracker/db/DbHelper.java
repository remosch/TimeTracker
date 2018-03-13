package com.example.android.timetracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 31.12.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String _DB_FILE_NAME = "TimeTracking-com.example.android.timetracker.db";
    private static final int _DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, _DB_FILE_NAME, null, _DB_VERSION);
    }

    //public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    //    super(context, name, factory, version);
    //}

    //private static final String _CREATE =
    //        "CREATE TABLE [time_data] ("
    //        + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
    //        + "[start_time] TEXT NOT NULL, [end_time] TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {

        //com.example.android.timetracker.db.execSQL(_CREATE);
        TimeDataTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
