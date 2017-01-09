package com.aastudio.investomater.Databases;

/**
 * Created by Abhidnya on 1/7/2017.
 */

public class NSEEntry {
    private String companyName;
    private String dataBaseCode;
    private String dataSetCode;
    private int id;

    public NSEEntry() {
    }

    public NSEEntry(String companyName, String dataBaseCode, String dataSetCode) {
        this.companyName = companyName;
        this.dataBaseCode = dataBaseCode;
        this.dataSetCode = dataSetCode;
    }

    public NSEEntry(int id, String companyName, String dataBaseCode, String dataSetCode) {
        this.companyName = companyName;
        this.dataBaseCode = dataBaseCode;
        this.dataSetCode = dataSetCode;
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataBaseCode() {
        return dataBaseCode;
    }

    public void setDataBaseCode(String dataBaseCode) {
        this.dataBaseCode = dataBaseCode;
    }

    public String getDataSetCode() {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode) {
        this.dataSetCode = dataSetCode;
    }
}
