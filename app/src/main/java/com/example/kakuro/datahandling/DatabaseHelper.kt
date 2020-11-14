package com.example.kakuro.datahandling

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    init {
        //val db = this.writableDatabase
    }



    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table " + TableName1 + " (" + Tab1Col1 + " INTEGER PRIMARY KEY AUTOINCREMENT," + Tab1Col2 + " INTEGER," + Tab1Col3 + " INTEGER," + Tab1Col4 + " INTEGER)")
        db?.execSQL("create table " + TableName2 + " (" + Tab2Col1 + " INTEGER PRIMARY KEY AUTOINCREMENT," + Tab2Col2 + " INTEGER," + Tab2Col3 + " INTEGER," + Tab2Col4 + " INTEGER," + Tab2Col5 + " INTEGER," + Tab2Col6 +" INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TableName1)
        db?.execSQL("DROP TABLE IF EXISTS " + TableName2)

    }

    fun insertData1(rows: Int, columns: Int, time: Long) : Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Tab1Col2, rows)
        contentValues.put(Tab1Col3, columns)
        contentValues.put(Tab1Col4, time)
        val result = db.insert(TableName1, null, contentValues)

        return (result != -1L)
    }

    fun insertData2(row: Int, column: Int, type: Int, val1: Int, val2: Int) : Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Tab2Col2, row)
        contentValues.put(Tab2Col3, column)
        contentValues.put(Tab2Col4, type)
        contentValues.put(Tab2Col5, val1)
        contentValues.put(Tab2Col6, val2)
        val result = db.insert(TableName2, null, contentValues)

        return (result != -1L)
    }

    fun getData1() : Cursor {
        val db = this.writableDatabase
        val result = db.rawQuery("select * from " + TableName1, null)
        if (result.count == 1) {

        }
        return result
    }

    fun getData2() : Cursor {
        val db = this.writableDatabase
        val result = db.rawQuery("select * from " + TableName2, null)
        if (result.count == 1) {

        }
        return result
    }

    fun clearData() {
        val db = this.writableDatabase
        db.delete(TableName1, null, null)
        db.delete(TableName2, null, null)
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Kakuro.db"

        const val TableName1 = "generaldata"
        const val Tab1Col1 = "ID"
        const val Tab1Col2 = "Rows"
        const val Tab1Col3 = "Columns"
        const val Tab1Col4 = "Time"

        const val TableName2 = "fields"
        const val Tab2Col1 = "ID"
        const val Tab2Col2 = "Row"
        const val Tab2Col3 = "Col"
        const val Tab2Col4 = "Type" // Blank, Hint, Value
        const val Tab2Col5 = "Val1" // row hint or value
        const val Tab2Col6 = "Val2" // col hint or 0
    }
}