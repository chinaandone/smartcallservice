package com.slsuper.callservice.compents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.slsuper.callservice.smartcallservice.ApplicationActivity;

/**
 * Created by regis on 2017/5/3.
 */

public class BootReceiver extends BroadcastReceiver {
    private final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION)) {
            Intent intent2 = new Intent(context, ApplicationActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
            Log.d("DEBUG", "start pad on..." + System.currentTimeMillis());
        }
    }
}
