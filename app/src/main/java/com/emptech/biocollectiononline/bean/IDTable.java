package com.emptech.biocollectiononline.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "IDTable")
public class IDTable {
    public static final String IDNumberType = "IDNumberType";
    public static final String IDNumber = "IDNumber";
    @DatabaseField(columnName = IDNumberType, generatedId = true)
    public int mIDNumberType;
    @DatabaseField(columnName = IDNumber, canBeNull = false, unique = true)
    public String mIDNumber;//用户身份ID；

    public static IDTable newItem(String IDNumber) {
        IDTable mIDTable = new IDTable();
        mIDTable.setmIDTable(IDNumber);
        return mIDTable;
    }


    public int getIDNumberType() {
        return mIDNumberType;
    }

    public void setIDNumberType(int IDNumberType) {
        this.mIDNumberType = IDNumberType;
    }

    public String getmIDNumber() {
        return mIDNumber;
    }

    public void setmIDTable(String mIDNumber) {
        this.mIDNumber = mIDNumber;
    }
}
