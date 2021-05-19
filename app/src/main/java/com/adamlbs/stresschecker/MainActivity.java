package com.adamlbs.stresschecker;
import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import static android.hardware.SensorManager.SENSOR_DELAY_UI;
import static android.provider.CalendarContract.EXTRA_EVENT_ID;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "StressChecker";
    private TextView mTextView;
    private ImageButton btnStart;
    private ImageButton btnPause;
    private Drawable imgStart;
    private SensorManager mSensorManager;
    int READINGRATE = 1000000000; // time in us
    private Sensor mHeartRateSensor;
    float mHeartRateFloat2 ;
    int j = Math.round(mHeartRateFloat2);
    int stresstimes;
    int error;
    String key;
    public PendingIntent pIntentlogin;

    int j2;
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
                        stopMeasure();
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS},
                    100);
        }
    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SENSOR_DELAY_UI);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
        String available = String.valueOf(sensorRegistered);
        String navailable = "false";
        if (navailable.equals(available)) {
            mTextView.setText("Your device is not supported."); } else {

                Handler handler = new Handler();
                int finalStresstimes = stresstimes;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        stopMeasure();
                        if (error == 1 ) {
                            changtexterror();
                            stopMeasure();
                            notificationwork();
                        }


                            else {
                            if (j2 == 0 ) {
                                changtexterror();
                                stopMeasure();
                                notificationwork();

                            } else
                            if (finalStresstimes <= 0) {

                            changetextnotstressed();
                            stopMeasure();
                                notificationwork();


                        } else {


                            if (finalStresstimes > 15) {

                                changetextstressed();
                                stopMeasure();
                                notificationwork();


                            }
                            }

                            {

                            }
                        }                }
                }, 20000);

            }

    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
        SharedPreferences settings = this.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
        btnStart.setVisibility(ImageButton.VISIBLE);
        btnPause.setVisibility(ImageButton.GONE);


    }





 public void notificationwork() {

     Notification.Builder b = new Notification.Builder(this);
     Intent intentAction = new Intent(this,MainActivity.class);
     ComponentName cName = new ComponentName
             ("com.google.android.apps.fitness","com.google.android.apps.fitness/com.google.android.wearable.fitness.realtime.breathe.BreatheActivity");
     intentAction.setComponent(cName);

     pIntentlogin = PendingIntent.getBroadcast(this,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);
     //FIX android O bug Notification add setChannelId("shipnow-message")
     NotificationChannel mChannel = null;
     b.setSmallIcon(R.drawable.ic_launcher) // vector (doesn't work with png as well)
             .setContentTitle("Vous êtes stressé ")
             .setContentText("L'application a détecté que vous êtes stressé ! Souhaitez vous faire un exercice de respiration? ")
             .addAction(R.drawable.ic_pause_white_24dp, "Exercice de respiration", pIntentlogin) // here is our closePendingIntent with the destroyCode .addAction is "the onClickListener for the notification button"//
             .setPriority(Notification.PRIORITY_MAX);

     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         mChannel = new NotificationChannel("your-channel", "yourSubjectName",NotificationManager.IMPORTANCE_HIGH);
         b.setChannelId("your-channel");
     }

     NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         notificationManager.createNotificationChannel(mChannel);
     }
     notificationManager.notify(2, b.build());

 }
    public void changetextstressed() {
        final String stressed = "You seems stressed ! Relax, do breathing exercises to stabilize your heart rate! ";
        ((Activity) this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)mTextView).setText(stressed);
                mTextView.postInvalidate();

                Log.d("Stress status", "Stressed");
            stopMeasure();
                notificationwork();
            }
        });




    }
    public void changtexterror() {
        final String stressed = "Une erreur est survenue. ! ";
        ((Activity) this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)mTextView).setText(stressed);
                mTextView.postInvalidate();

                Log.e("Stress status", "ERROR");
                stopMeasure();
            }
        });




    }

    public void changetextnotstressed() {

        mTextView = (TextView) findViewById(R.id.bpmText);
        mTextView.setText("Your heart rate is normal! " +
                "\n You don't seems stressed ! ");
        mTextView.postInvalidate();

    }

    @Override

    public void onSensorChanged(SensorEvent event) {
        mHeartRateFloat2 = event.values[0];
        int j = Math.round(mHeartRateFloat2);
        stresstimes = 0;

        j2 = j;
        for (int i = 0; i < Math.round(mHeartRateFloat2); i++) {
            Log.d("Old Heart Rate :", String.valueOf(j));
            Log.d("New Heart Rate :", String.valueOf(i));

            int heartrate_differences = j - i;
            Log.d("Difference between old and new h1eart rate :", String.valueOf(heartrate_differences));
            if (heartrate_differences > 15) {
                stresstimes = stresstimes+1;
            } else if (j == 0){
                error = 1;
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