package com.emptech.biocollectiononline.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "IDPictureTable")
public class IDPictureTable {
    public static final String IDNumberType = "IDNumberType";
    public static final String IDPictureKey = "PicturePathKey";
    public static final String IDPictureValue = "PicturePathValue";
    public static final String IDPictureNumber = "IDPictureNumber";
    public static final String IDPictureUse = "IDPictureUse";
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField(columnName = IDNumberType, foreignAutoCreate = true,foreign = true)
    public IDTable mIDNumber;//信息ID；
    @DatabaseField(columnName = IDPictureKey)
    public String mIDPictureKey;
    @DatabaseField(columnName = IDPictureValue)
    public String mIDPictureValue;
    @DatabaseField(columnName = IDPictureNumber)
    public String mIDPictureNumber;
    @DatabaseField(columnName = IDPictureUse)
    public int mIDPictureUse;

    public static IDPictureTable newItem(IDTable mIDNumber, String mIDPictureKey, String mIDPictureValue, String mIDPictureNumber) {
        IDPictureTable mIDPictureTable = new IDPictureTable();
        mIDPictureTable.setmIDNumber(mIDNumber);
        mIDPictureTable.setmIDPictureKey(mIDPictureKey);
        mIDPictureTable.setmIDPictureValue(mIDPictureValue);
        mIDPictureTable.setmIDPictureNumber(mIDPictureNumber);
        mIDPictureTable.setmIDPictureUse(0);
        return mIDPictureTable;
    }


    public IDTable getmIDNumber() {
        return mIDNumber;
    }

    public void setmIDNumber(IDTable mIDNumber) {
        this.mIDNumber = mIDNumber;
    }

    public int ismIDPictureUse() {
        return mIDPictureUse;
    }

    public void setmIDPictureUse(int mIDPictureUse) {
        this.mIDPictureUse = mIDPictureUse;
    }

    public String getmIDPictureKey() {
        return mIDPictureKey;
    }

    public void setmIDPictureKey(String mIDPictureKey) {
        this.mIDPictureKey = mIDPictureKey;
    }

    public String getmIDPictureValue() {
        return mIDPictureValue;
    }

    public void setmIDPictureValue(String mIDPictureValue) {
        this.mIDPictureValue = mIDPictureValue;
    }

    public String getmIDPictureNumber() {
        return mIDPictureNumber;
    }

    public void setmIDPictureNumber(String mIDPictureNumber) {
        this.mIDPictureNumber = mIDPictureNumber;
    }
}
