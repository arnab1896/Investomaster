package com.aastudio.investomater.Databases;

/**
 * Created by Abhidnya on 1/7/2017.
 */

public class MyData {

    private final static String[] TYPES = {"Select Type", "Stock Market", "Futures", "Commodity", "Currency", "Options"};

    public static String[] getTYPES() {
        return TYPES;
    }

    public static class StockData {
        public final static String[] countries = {"Select Location", "India", "United States", "Europe", "China", "Other"};

        //India
        public final static String[] India = {"Select Market", "National Stock Exchange", "Bombay Stock Exchange"};

        //USA
        public final static String[] UnitedStates = {"Wiki EOD Stock Prices"};
    }

}
