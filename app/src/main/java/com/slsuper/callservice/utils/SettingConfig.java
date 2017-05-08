package com.slsuper.callservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by regis on 2017/4/26.
 */

public class SettingConfig {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private String HostKey = "ServerHost";
    private String WatchKey = "WatchID";

    public SettingConfig(Context context){
        mContext = context.getApplicationContext();
        sharedPreferences = mContext.getSharedPreferences("CallServiceConfig",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public SettingConfig(Context context,String file){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(file,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean SetServerHost(String serverhost){
        editor.putString(HostKey,serverhost);
        return editor.commit();
    }

    public boolean SetWatchID(String watchid){
        editor.putString(WatchKey,watchid);
        return editor.commit();
    }

    public String getServerHost(){
        String value = sharedPreferences.getString(HostKey,null);
        return value;
    }

    public String getWatchID(){
        String value = sharedPreferences.getString(WatchKey,null);
        return value;
    }
}
