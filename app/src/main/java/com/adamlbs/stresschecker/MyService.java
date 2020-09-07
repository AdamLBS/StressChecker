package com.adamlbs.stresschecker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyService extends Service  implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    public MyService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
        }        Toast.makeText(this, "StressChecker is working in the background.", Toast.LENGTH_LONG).show();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d("Service", "Service started ");
                startService();
            }
        }, 0, 1, TimeUnit.MINUTES);
        ScheduledExecutorService scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d("Service", "Monitor stopped ");
                onPause();
                clear();

            }
        }, 0, 90, TimeUnit.SECONDS);
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
