package com.bss.maxencecoulibaly.familychat.utils;

public class DatabaseUtil {

    public static String getDatabasePath(String[] strings) {
        String path = "/";
        for(int i=0; i<strings.length; i++) {
            path += strings[i] + "/";
        }
        return path.substring(0, path.length() - 1);
    }

}
