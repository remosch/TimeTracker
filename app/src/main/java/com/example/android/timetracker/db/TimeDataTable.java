package com.example.android.timetracker.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by admin on 31.12.2017.
 */

public class TimeDataTable {

    /**
     * ID für die Auflistung
     */
    public static final int ITEM_LIST_ID = 100;

    /**
     * ID für einen Datensatz
     */
    public static final int ITEM_ID = 101;

    /**
     * ID für einen Datensatz der noch nicht beendet wurde
     */
    public static final int NOT_FINISHED_ITEM_ID = 102;

    /**
     * Name der Tabelle
     */
    public static final String TABLE_NAME = "time_data";

    private static final String _CREATE =
            "CREATE TABLE [time_data] ("
                    + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "[start_time] TEXT NOT NULL, [end_time] TEXT)";

    public static void createTable(SQLiteDatabase db)
    {
        db.execSQL(_CREATE);
    }
}
