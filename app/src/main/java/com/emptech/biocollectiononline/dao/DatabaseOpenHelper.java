package com.emptech.biocollectiononline.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.emptech.biocollectiononline.bean.IDPictureTable;
import com.emptech.biocollectiononline.bean.IDTable;
import com.emptech.biocollectiononline.bean.IDUserTable;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class DatabaseOpenHelper extends OrmLiteSqliteOpenHelper {

    @SuppressWarnings("rawtypes")
    private Map<String, Dao> mDaos = new HashMap<String, Dao>();

    static DatabaseOpenHelper inst;
    private Context context;

    private DatabaseOpenHelper(Context context) {
        super(context, "BiosignatureCollection.db", null, 1);
        this.context = context;
    }

    public static synchronized DatabaseOpenHelper get(Context context) {
        if (inst == null)
            inst = new DatabaseOpenHelper(context);
        return inst;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        try {
            TableUtils.createTableIfNotExists(arg1, IDUserTable.class);
            TableUtils.createTableIfNotExists(arg1, IDTable.class);
            TableUtils.createTableIfNotExists(arg1, IDPictureTable.class);
        } catch (SQLException e) {
            e.printStackTrace();
//            LogUtils.e(AppConfig.MODULE_CATCHERR, "创建数据库时候崩溃了：" + e.getMessage());
        }
//        LogUtils.e(AppConfig.MODULE_CATCHERR, "创建数据库成功！");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldver, int newver) {
        try {
            TableUtils.dropTable(connectionSource, IDUserTable.class, true);
            TableUtils.dropTable(connectionSource, IDTable.class, true);
            TableUtils.dropTable(connectionSource, IDPictureTable.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized Dao getDao(Class clazz) {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (mDaos.containsKey(className)) {
            dao = mDaos.get(className);
        }
        if (dao == null) {
            try {
                dao = super.getDao(clazz);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (dao != null)
                mDaos.put(className, dao);
        }
        return dao;
    }

    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(this.getDatabaseName());
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
        for (String key : mDaos.keySet()) {
            Dao dao = mDaos.get(key);
            dao = null;
        }
    }
}
