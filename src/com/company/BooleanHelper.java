package com.company;

public class BooleanHelper {
    private final String LOG_TAG = this.getClass().getCanonicalName();

    public static boolean toBoolean(int value){
        boolean res;
        if (value == 0)
            res = false;
        else
            res = true;

        return res;
    }

    public static int toInt(boolean value){
        int res;
        if (value)
            res = 1;
        else
            res = 0;

        return res;
    }
}
