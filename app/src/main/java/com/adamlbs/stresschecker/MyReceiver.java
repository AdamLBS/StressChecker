package com.adamlbs.stresschecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        String action=intent.getStringExtra("action");
        if(action.equals("action1")){
            performAction1();
            Intent serviceIntent = new Intent(context, MyService.class);
            context.stopService(serviceIntent);

        }
        else if(action.equals("action2")){
            performAction2();

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void performAction1(){
        Log.d("Service", "Action 1 ");

    }

    public void performAction2(){
        Log.d("Service", "Action 2 ");

    }
}


