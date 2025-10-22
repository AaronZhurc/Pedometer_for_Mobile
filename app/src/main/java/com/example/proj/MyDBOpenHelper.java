package com.example.proj;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBOpenHelper extends SQLiteOpenHelper {
    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {super(context, "my.db", null, 1); }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table user\n" +
                "(\n" +
                "   uid                  INTEGER primary key AUTOINCREMENT,\n" +
                "   uname                varchar(30),\n" +
                "   pwd                  varchar(30),\n" +
                "   height               float,\n" +
                "   weight               float,\n" +
                "   sex                  varchar(10),\n" +
                "   birth                date\n" +
                ");");
        db.execSQL("create table walk\n" +
                "(\n" +
                "   wid                  INTEGER primary key AUTOINCREMENT,\n" +
                "   date                 date,\n" +
                "   uid                  int,\n" +
                "   step                 int,\n" +
                "   distance             float,\n" +
                "   constraint fk_uid foreign key (uid) references user(uid) on delete cascade\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE person ADD phone VARCHAR(12) ");
    }
}
