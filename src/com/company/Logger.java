package com.company;

public class Logger {
    private final String LOG_TAG = this.getClass().getCanonicalName();

    public static void log(String log){
        System.out.println(log);
    }
}
