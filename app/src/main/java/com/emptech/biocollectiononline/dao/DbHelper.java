package com.emptech.biocollectiononline.dao;

import android.content.Context;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.bean.IDPictureTable;
import com.emptech.biocollectiononline.bean.IDTable;
import com.emptech.biocollectiononline.bean.IDUserTable;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbHelper {

    private final static String TAG = "SQLite";
    Dao<IDTable, Integer> mIDTable;
    Dao<IDUserTable, Integer> mIDUserTable;
    Dao<IDPictureTable, Integer> mIDPictureTable;
    static DbHelper inst;
    private Context context;

    @SuppressWarnings("unchecked")
    private DbHelper(Context context) {
        super();
        this.context = context;
        mIDUserTable = (Dao<IDUserTable, Integer>) DatabaseOpenHelper.get(
                context.getApplicationContext()).getDao(IDUserTable.class);
        mIDTable = (Dao<IDTable, Integer>) DatabaseOpenHelper.get(
                context.getApplicationContext()).getDao(IDTable.class);
        mIDPictureTable = (Dao<IDPictureTable, Integer>) DatabaseOpenHelper.get(
                context.getApplicationContext()).getDao(IDPictureTable.class);
    }

    public static DbHelper get(Context context) {
        synchronized (DbHelper.class) {
            if (inst == null)
                inst = new DbHelper(context);
            return inst;

        }
    }

    /**
     * 搜索所有数据
     *
     * @return
     */
    public List<IDUserTable> findIDUserInfoTalble() {
        try {
            List<IDUserTable> findData = mIDUserTable.queryForAll();
            for (int i = 0; findData != null && i < findData.size(); i++) {
                mIDTable.refresh(findData.get(i).getmIDTable());
                LogUtils.v(TAG, findData.get(i).getmIDTable().getmIDNumber() + "获得[" + findData.get(i).getmIDNumberKey()
                        + "," + findData.get(i).getmIDNumberValue() + "]");
            }
            return findData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<IDTable> findIDTalbleByUserID(String UserID) {
        try {
            List<IDTable> idtable = mIDTable.queryForEq(IDTable.IDNumber, UserID);
            return idtable;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据身份证标志搜索信息；
     *
     * @return
     */
    public List<IDUserTable> findIDUserInfoTalbleByUserIDType(int UserIDType) {
        try {
            List<IDUserTable> findData = mIDUserTable.queryForEq(IDUserTable.IDNumberType, UserIDType);
            for (int i = 0; findData != null && i < findData.size(); i++) {
                mIDTable.refresh(findData.get(i).getmIDTable());
            }
            return findData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public IDTable createID(String IDNumber) {
        try {
            IDTable mID = mIDTable.createIfNotExists(IDTable.newItem(IDNumber));
        } catch (SQLException e) {
            LogUtils.v(TAG, "已经存在，不需要重新建立ID：" + IDNumber);
        }
        try {
            List<IDTable> table = mIDTable.queryForEq(IDTable.IDNumber, IDNumber);
            if (table != null && table.size() >= 1) {
                LogUtils.v(TAG, "ID表：[" + table.get(0).getIDNumberType() + "," + table.get(0).getmIDNumber() + "]");
                return table.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createIDUserInfo(String IDNumber, Map<String, String> userInfo) {
        IDTable mIDTable = createID(IDNumber);
        DeleteBuilder mIdDelete = mIDUserTable.deleteBuilder();
        try {
            mIdDelete.where().eq(IDUserTable.IDNumberType, mIDTable.getIDNumberType());
            mIdDelete.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mIdDelete = mIDPictureTable.deleteBuilder();
        try {
            mIdDelete.where().eq(IDPictureTable.IDNumberType, mIDTable.getIDNumberType());
            mIdDelete.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            for (String key : userInfo.keySet()) {
                if("Addr".equals(key))
                    continue;
                IDUserTable mIDuser = IDUserTable.newItem(mIDTable, key, userInfo.get(key));
                mIDUserTable.createOrUpdate(mIDuser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateIDUserInfo(String IDNumber, Map<String, String> userInfo) {
        try {
            List<IDTable> idTables = mIDTable.queryForEq(IDTable.IDNumber, IDNumber);
            if (idTables != null && idTables.size() == 1) {
                List<IDUserTable> idUserTables = mIDUserTable.queryForEq(IDUserTable.IDNumberType, idTables.get(0));
                mIDUserTable.delete(idUserTables);
                for (String key : userInfo.keySet()) {
                    IDUserTable mIDuser = IDUserTable.newItem(idTables.get(0), key, userInfo.get(key));
                    mIDUserTable.createOrUpdate(mIDuser);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertPictureInfo(String userID, String pathKey, String pathVale, String pictureNumber) {
        try {
            LogUtils.v(TAG, "保存图片数据：[" + userID + "," + pathKey + "," + pathVale + "," + pictureNumber + "]");
            List<IDTable> mIDTable = findIDTalbleByUserID(userID);
            if (mIDTable != null && mIDTable.size() > 0) {
                mIDPictureTable.createOrUpdate(IDPictureTable.newItem(mIDTable.get(0), pathKey, pathVale, pictureNumber));
                LogUtils.v(TAG, "保存图片数据成功！！！");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<IDPictureTable> findPhotoInfoByUserID(String userID) {
        try {
            List<IDTable> mIDTable = findIDTalbleByUserID(userID);
            if (mIDTable != null && mIDTable.size() > 0) {
                List<IDPictureTable> pictureTables = mIDPictureTable.queryBuilder().where()
                        .eq(IDPictureTable.IDNumberType, mIDTable.get(0).getIDNumberType())
                        .and().eq(IDPictureTable.IDPictureKey, AppConfig.PREFERENCE_KEY_PHOTO).query();
                return pictureTables;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新确认使用照片；
     *
     * @param userID
     * @param photoNumber
     * @return
     */
    public boolean updateUsePhotoInfoByUserID(String userID, String photoNumber) {
        try {
            List<IDTable> mIDTable = findIDTalbleByUserID(userID);
            if (mIDTable != null && mIDTable.size() > 0) {
                mIDPictureTable.updateBuilder().updateColumnValue(IDPictureTable.IDPictureUse, 1).where()
                        .eq(IDPictureTable.IDNumberType, mIDTable.get(0).getIDNumberType())
                        .and().ne(IDPictureTable.IDPictureNumber, photoNumber);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据用户ID，查找用户信息；
     *
     * @param userID
     * @return 用戶信息；
     */
    public Map<String, String> findUserInfoByUserID(String userID) {
        try {
            LogUtils.v(TAG, "获取userID：" + userID + "信息");
            List<IDTable> mIDTable = findIDTalbleByUserID(userID);
            if (mIDTable != null && mIDTable.size() > 0) {
                //获取图片资源,使用的资源文件；
                List<IDPictureTable> pictureTables = mIDPictureTable.queryBuilder().where()
                        .eq(IDPictureTable.IDNumberType, mIDTable.get(0).getIDNumberType())
                        .and().eq(IDPictureTable.IDPictureUse, 0)
                        .query();
                LogUtils.v(TAG, "获取IDPictureTable：" + pictureTables.size() + " 条信息");
                //获取信息资源；
                List<IDUserTable> userTables = mIDUserTable.queryForEq(IDUserTable.IDNumberType, mIDTable.get(0).getIDNumberType());
                LogUtils.v(TAG, "获取IDUserTable：" + userTables.size() + " 条信息");
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < userTables.size(); i++) {
                    LogUtils.e(TAG,"getmIDNumberKey()"+userTables.get(i).getmIDNumberKey()+"getmIDNumberValue()"+userTables.get(i).getmIDNumberValue());
                    map.put(userTables.get(i).getmIDNumberKey(), userTables.get(i).getmIDNumberValue());
                }
                for (int i = 0; i < pictureTables.size(); i++) {
                    IDPictureTable idPictureTable = pictureTables.get(i);
                    LogUtils.e(TAG,"getmIDPictureKey()"+idPictureTable.getmIDPictureKey()+"getIDPictureValue"+idPictureTable.getmIDPictureValue());
                    map.put(pictureTables.get(i).getmIDPictureKey(), pictureTables.get(i).getmIDPictureValue());
                }
                return map;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
