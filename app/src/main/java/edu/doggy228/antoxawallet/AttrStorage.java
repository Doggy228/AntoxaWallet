package edu.doggy228.antoxawallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AttrStorage {
    private static final String PREF_SERVER_NAME = "server_name";
    private static final String DEFAULT_SERVER_NAME = "localhost";
    private static String sServerName = null;
    private static final Object sLock = new Object();

    public static void serverNameSet(Context c, String s) {
        synchronized(sLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_SERVER_NAME, s).commit();
            sServerName = s;
        }
    }

    public static String serverNameGet(Context c) {
        synchronized (sLock) {
            if (sServerName == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                String serverName = prefs.getString(PREF_SERVER_NAME, DEFAULT_SERVER_NAME);
                sServerName = serverName;
            }
            return sServerName;
        }
    }

}

