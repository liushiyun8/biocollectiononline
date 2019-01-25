package com.emptech.biocollectiononline.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "IDUserInfoTable")
public class IDUserTable {
    public static final String IDNumberType = "IDNumberType";
    public static final String IDNumberKey = "IDKey";
    public static final String IDNumberValue = "IDValue";
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(columnName = IDNumberType, foreignAutoCreate = true, foreign = true)
    public IDTable mIDNumber;//信息ID；
    @DatabaseField(columnName = IDNumberKey)
    public String mIDNumberKey;
    @DatabaseField(columnName = IDNumberValue)
    public String mIDNumberValue;

    public static IDUserTable newItem(IDTable IDNumber, String mIDNumberKey, String mIDNumberValue) {
        IDUserTable mIDUserTable = new IDUserTable();
        mIDUserTable.setmIDNumber(IDNumber);
        mIDUserTable.setmIDNumberKey(mIDNumberKey);
        mIDUserTable.setmIDNumberValue(mIDNumberValue);
        return mIDUserTable;
    }

    public IDTable getmIDTable() {
        return mIDNumber;
    }

    public void setmIDNumber(IDTable mIDNumber) {
        this.mIDNumber = mIDNumber;
    }

    public String getmIDNumberKey() {
        return mIDNumberKey;
    }

    public void setmIDNumberKey(String mIDNumberKey) {
        this.mIDNumberKey = mIDNumberKey;
    }

    public String getmIDNumberValue() {
        return mIDNumberValue;
    }

    public void setmIDNumberValue(String mIDNumberValue) {
        this.mIDNumberValue = mIDNumberValue;
    }
}
