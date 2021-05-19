package com.adamlbs.stresschecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyService extends Service  implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    public Context context = this;
    public Handler handler = null;
    public PendingIntent pIntentlogin;
    public static Runnable runnable = null;
    public MyService() {
        super();
    }
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
startMyOwnForeground();
        else
            startForeground(1, new Notification());
        Toast.makeText(this, "StressChecker is working in the background.", Toast.LENGTH_LONG).show();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d("Service", "Service started ");
                startService();
            }
        }, 0, 120, TimeUnit.SECONDS);
        ScheduledExecutorService scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d("Service", "Monitor stopped ");
                onPause();
                clear();

            }
        }, 0, 390, TimeUnit.SECONDS);
    }

    public void clear(){
        SharedPreferences settings = this.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("mChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.adamlbs.stresschecker";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent intentAction = new Intent(context,MyReceiver.class);
        intentAction.putExtra("action","action1");
        pIntentlogin = PendingIntent.getBroadcast(context,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("StressChecker is running in background")
                .setContentText("StressChecker is monitoring your heart rate and will notify you if you're stressed !")
                .addAction(R.drawable.ic_pause_white_24dp, "Stop monitoring", pIntentlogin) // here is our closePendingIntent with the destroyCode .addAction is "the onClickListener for the notification button"//
                .setPriority(NotificationManager.IMPORTANCE_MIN)

                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);

    }
    public void stopservice () {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if(action!=null)

                switch (action) {
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                        break;
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void stopForegroundService() {
        Log.d("TAG_FOREGROUND_SERVICE", "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }
    private void initNotification() {
        //Register a receiver to stop Service

    }


    //We need to declare the receiver with onReceive function as below
    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
    private void makenotification_service() {

        int notificationId = 17;

        Notification.Builder b = new Notification.Builder(this);
        b.setVibrate(new long[]{500, 500});
        b.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        //FIX android O bug Notification add setChannelId("shipnow-message")
        NotificationChannel mChannel = null;;

        b.setSmallIcon(R.drawable.ic_launcher) // vector (doesn't work with png as well)
                .setContentTitle("Monitoring..")
                .setOnlyAlertOnce(true)
                .setContentText("StressChecker is monitoring your heart rate and will notify you if you're stressed !!")
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults( Notification.FLAG_ONLY_ALERT_ONCE)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel("your-channel", "yourSubjectName", NotificationManager.IMPORTANCE_HIGH);
            b.setChannelId("your-channel");
        }

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(mChannel);
            mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            mChannel.enableVibration(true);
        }
        b.setDefaults(Notification.DEFAULT_SOUND);
        b.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationManager.notify(notificationId, b.build());
    }
    public void stopmonitor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d("Service", "Monitor stopped ");
                onPause();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        SharedPreferences settings = this.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
        Toast.makeText(this, "StressChecker stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
       // Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }

    public void startService()
    {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        startMeasure();

    }
    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener((SensorEventListener) this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));

    }

    private void makenotification() {
        boolean firstrun = getSharedPreferences("NOTIFICATION_SERVICE", MODE_PRIVATE).getBoolean("firstrun", true);
        if (firstrun) {
            int notificationId = 1723;

            Notification.Builder b = new Notification.Builder(this);
            b.setVibrate(new long[]{500, 500});
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            b.setSound(alarmSound);


            //FIX android O bug Notification add setChannelId("shipnow-message")
            NotificationChannel mChannel = null;
            b.setSmallIcon(R.drawable.ic_launcher) // vector (doesn't work with png as well)
                    .setContentTitle("You're stressed !")
                    .setOnlyAlertOnce(true)
                    .setContentText("Your heart rate indicates that you are stressed. Relax, do breathing exercises to stabilize your heart rate!")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)

                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mChannel = new NotificationChannel("your-channel", "Stress Alert", NotificationManager.IMPORTANCE_HIGH);
                mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                mChannel.enableVibration(true);
                b.setChannelId("your-channel");

            }

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(mChannel);
                notificationManager.createNotificationChannel(mChannel);
                mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                mChannel.enableVibration(true);
            }
            b.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
            b.setDefaults(Notification.DEFAULT_SOUND);
            b.setDefaults(Notification.DEFAULT_VIBRATE);

            notificationManager.notify(notificationId, b.build());
            getSharedPreferences("NOTIFICATION_SERVICE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstrun", false)
                    .apply();
        }
        }
            int notificationId = 1723;



    public void onSensorChanged(SensorEvent event) {
        float mHeartRateFloat = event.values[0];
        int j = Math.round(mHeartRateFloat);

        for (int i = 0; i < Math.round(mHeartRateFloat); i++) {
            Log.d("Old Heart Rate :", String.valueOf(j));
            Log.d("New Heart Rate :", String.valueOf(i));

            int heartrate_differences = j-i;
            Log.d("Difference between old and new heart rate :", String.valueOf(heartrate_differences));
            if (heartrate_differences > 15) {
                boolean alreadyExecuted = false;
makenotification();


            }
        }
        int mHeartRate = Math.round(mHeartRateFloat);
        Log.d("Sensor Status:", String.valueOf(mHeartRate));
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    protected void onPause() {
        SharedPreferences settings = this.getSharedPreferences("NOTIFICATION_SERVICE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
        mSensorManager.unregisterListener(this);
    }
}
