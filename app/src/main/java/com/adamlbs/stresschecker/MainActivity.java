package com.adamlbs.stresschecker;
import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationCompat.BigPictureStyle;
import androidx.core.app.NotificationCompat.BigTextStyle;
import androidx.core.app.NotificationCompat.InboxStyle;
import androidx.core.app.NotificationCompat.MessagingStyle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.wear.ambient.AmbientMode;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.WearableExtender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.CalendarContract.EXTRA_EVENT_ID;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "StressChecker";
    private TextView mTextView;
    private ImageButton btnStart;
    private ImageButton btnPause;
    private Drawable imgStart;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    String key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(MainActivity.this, MyService.class));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        checkPermissions();
        SensorManager sensorManager =
                (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        boolean hasSensor = sensorManager.getDefaultSensor(34, true /* wakeup */) != null;
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.bpmText);
                btnStart = (ImageButton) stub.findViewById(R.id.btnStart);
                btnPause = (ImageButton) stub.findViewById(R.id.btnPause);

                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnStart.setVisibility(ImageButton.GONE);
                        btnPause.setVisibility(ImageButton.VISIBLE);
                        mTextView.setText("Checking for stress signs...");
                        startMeasure();
                    }
                });

                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        mTextView.setText("--");
                        throw new RuntimeException("Test Crash");
                    }
                });

            }
        });
        SharedPreferences settings = this.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();

        setAmbientEnabled();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

    }
    private void checkPermissions() {

        boolean BODY_SENSORSPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                        == PackageManager.PERMISSION_GRANTED;

        if (BODY_SENSORSPermissionGranted) {

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BODY_SENSORS},
                    100);
        }
    }
    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
        String available = String.valueOf(sensorRegistered);
        String navailable = "false";
        if (navailable.equals(available)) {
            mTextView.setText("Your device is not supported.");

        }
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
        SharedPreferences settings = this.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }
private void makenotification() {

    boolean firstrun2 = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun2", true);
    if (firstrun2) {
        int notificationId = 1723;

        Notification.Builder b = new Notification.Builder(this);
        b.setVibrate(new long[]{500, 500});
        b.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        //FIX android O bug Notification add setChannelId("shipnow-message")
        NotificationChannel mChannel = null;;

        b.setSmallIcon(R.drawable.ic_launcher) // vector (doesn't work with png as well)
                .setContentTitle("You're stressed !")
                .setOnlyAlertOnce(true)
                .setContentText("Your heart rate indicates that you are stressed. Relax, do breathing exercises to stabilize your heart rate!")
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
        getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .edit()
                .putBoolean("firstrun2", false)
                .apply();
    }
}
    @Override
    public void onSensorChanged(SensorEvent event) {
        float mHeartRateFloat2 = event.values[0];
        int j = Math.round(mHeartRateFloat2);

        for (int i = 0; i < Math.round(mHeartRateFloat2); i++) {
            Log.d("Old Heart Rate :", String.valueOf(j));
            Log.d("New Heart Rate :", String.valueOf(i));

            int heartrate_differences = j-i;
            Log.d("Difference between old and new heart rate :", String.valueOf(heartrate_differences));
            if (heartrate_differences >= 15) {
                mTextView.setText("You seems stressed ! " +
                        "\n Relax, do breathing exercises to stabilize your heart rate! ");


                }            if (heartrate_differences < 15) {
                mTextView.setText("Your heart rate is normal! " +
                        "\n You don't seems stressed ! ");



            }

            }
        int mHeartRate2 = Math.round(mHeartRateFloat2);
        Log.d("Sensor Status:", String.valueOf(mHeartRate2));


            SensorManager sensorManager =
                    (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            boolean hasSensor = sensorManager.getDefaultSensor(34, true /* wakeup */) != null;

            Log.d("Sensor Status:", String.valueOf(hasSensor));



        }





    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "getString(R.string.channel_name)";
            String description = "Stress";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Stress", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        //Print all the sensors
//        if (mHeartRateSensor == null) {
//            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//            for (Sensor sensor1 : sensors) {
//                Log.i("Sensor list", sensor1.getName() + ": " + sensor1.getType());
//            }
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}