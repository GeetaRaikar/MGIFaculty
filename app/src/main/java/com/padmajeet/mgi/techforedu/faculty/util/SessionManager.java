package com.padmajeet.mgi.techforedu.faculty.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "com.padmajeet.preschool.shared_pref";

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void putString(String key, String value){
        editor.putString(key,value).commit();
    }
    public String getString(String key){
        return pref.getString(key,null);
    }

    public void putBoolean(String key, boolean value){
        editor.putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key){
        return pref.getBoolean(key,false);
    }

    public void putInt(String key, int value){
        editor.putInt(key, value).commit();
    }

    public int getInt(String key){
        return pref.getInt(key,-1);
    }

    public void remove(String key){
        editor.remove(key).commit();
    }

    public void clear(){
        editor.clear().commit();
    }
}
