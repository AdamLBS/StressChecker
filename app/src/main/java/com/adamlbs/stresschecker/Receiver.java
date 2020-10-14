package com.adamlbs.stresschecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 333;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MyService.class);
        context.stopService(service);
    }
}
