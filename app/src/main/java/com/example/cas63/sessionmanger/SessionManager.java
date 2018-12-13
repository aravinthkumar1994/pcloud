package com.example.cas63.sessionmanger;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;
Context context;
    // Sharedpref file name
    private static final String PREF_NAME = "Pcloud";
    //2rBAZLj3hWIDpP58ZjWo3U7Z1o3Lem4hufpMRil6cOrl78BKO3Ck
private static final String Pref_tocken="oLWAZatJYLO3iGwHZ6Q1fI7ZewTmcLLMjh0JPI01IMJc9LM7u81y";



public SessionManager(Context context)
{
this.context=context;
sharedPreferences=context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
editor=sharedPreferences.edit();
}

public void setAccessTocken(String token)
{
    editor.putString(Pref_tocken,token);
    editor.commit();
}

public  String getAccessTocken()
    {
return sharedPreferences.getString(Pref_tocken,"oLWAZatJYLO3iGwHZ6Q1fI7ZewTmcLLMjh0JPI01IMJc9LM7u81y");
    }
}
