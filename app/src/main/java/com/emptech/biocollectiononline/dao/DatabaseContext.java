package com.emptech.biocollectiononline.dao;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class DatabaseContext extends ContextWrapper {

    private String dbDir;

    public DatabaseContext(Context base, String path) {
        super(base);
        dbDir = path;
    }

    @Override
    public File getDatabasePath(String name) {
        boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
        if (!sdExist) {
            Log.e("Database error", "SD not existed");
            return null;
        }

        File dirFile = new File(dbDir);
        if (!dirFile.exists())
            dirFile.mkdirs();

        boolean isFileCreateSuccess = false;
        String dbPath = dbDir + "/" + name;
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                isFileCreateSuccess = dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            isFileCreateSuccess = true;

        if (isFileCreateSuccess)
            return dbFile;
        else
            return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }
}
