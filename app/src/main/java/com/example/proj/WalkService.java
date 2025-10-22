package com.example.proj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WalkService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private LocationManager locationManager;
    private boolean hasRecord = false;
    private int hasStepCount = 0, previousStepCount = 0, totalStep = 0;
    private UpdateUICallback mCallback;
    public static Location preLocation;
    private WalkBinder walkBinder = new WalkBinder();
    private double totaldistance = 0;
    private NotificationManager manager;
    private Notification notification;
    private MyDBOpenHelper dbOpenHelper;
    private Thread thread;
    private BroadcastReceiver broadcastReceiver;
    private SharedHelper sh;

    public WalkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
        sh=new SharedHelper(getApplicationContext());
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(), "my.db", null, 1);
        initData();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return walkBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initData();
        thread=new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                //Looper死循环
                //mSensorManager 传感器，以获取步数数据
                //locationManager gps，以获取步行距离
                Log.e("walkservice", "start");
                Looper.prepare();
                startForeground(1, getNotification());
                if (mSensorManager != null) {
                    mSensorManager = null;
                }
                if (locationManager == null) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                }
                //如果没获取到位置权限则不执行
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                }
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                mSensorManager.registerListener(WalkService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Looper.loop();
            }
        });
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }
    public Notification getNotification(){
        //状态栏
        Log.e("walkservice","getNotification"+totalStep);
        try {
            Intent intent = new Intent(this, WalkActivity.class);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            SharedHelper sh=new SharedHelper(getApplicationContext());
            if(sh.readUsername().equals("")){
                notification = new Notification.Builder(this, "channel_1")
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("未登录")
                        .setContentText("步数"+totalStep+"步 距离"+(int)totaldistance+"米").build();
            }else{
                notification = new Notification.Builder(this, "channel_1")
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(sh.readUsername())
                        .setContentText("步数"+totalStep+"步 距离"+(int)totaldistance+"米").build();
            }
            NotificationChannel channel = new NotificationChannel("channel_1", "123", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
            manager.notify(1, notification);
            return notification;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private LocationListener locationListener=new LocationListener() {
        //gps测算距离
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                return;
            }
            Log.e("walkservice", "lat:"+location.getLatitude()+",lon:"+location.getLongitude());
            if (preLocation != null) {
                float[] result = new float[3];
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (preLocation.getLatitude() != latitude || preLocation.getLatitude() != longitude) {
                    Location.distanceBetween(preLocation.getLatitude(), preLocation.getLongitude(), latitude, longitude, result);
                    totaldistance += result[0];
                }
            }
            preLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //计算步数
        int tempStep = (int) sensorEvent.values[0];
        if (!hasRecord) {
            hasRecord = true;
            hasStepCount = tempStep;
        } else {
            int thisStepCount = tempStep - hasStepCount;
            int thisStep = thisStepCount - previousStepCount;
            totalStep += thisStep;
            previousStepCount = thisStepCount;
        }
        updateNotification();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public class WalkBinder extends Binder {
        WalkService getService(){
            return WalkService.this;
        }
    }
    private void updateNotification(){
        //实时更新数据
        if(mCallback!=null){
            mCallback.updateUI(totalStep,totaldistance);
            Log.e("walkservice","updatenotification"+totalStep);
            Intent intent = new Intent(this, WalkActivity.class);
            SharedHelper sh=new SharedHelper(getApplicationContext());
            if(sh.readUsername().equals("")){
                notification = new Notification.Builder(this, "channel_1")
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("未登录")
                        .setContentText("步数"+totalStep+"步 距离"+(int)totaldistance+"米").build();
            }else{
                notification = new Notification.Builder(this, "channel_1")
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(sh.readUsername())
                        .setContentText("步数"+totalStep+"步 距离"+(int)totaldistance+"米").build();
            }
            manager.notify(1, notification);
        }
    }
    public void registerCallback(UpdateUICallback paramICallback){
        this.mCallback=paramICallback;
    }

    @Override
    public void onDestroy() {
        //Service销毁时保存数据
        Log.e("walkservice","destory");
        super.onDestroy();
        saveData();
        stopForeground(true);
    }
    public void saveData(){
        //保存数据到数据库
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        if(!sh.readUsername().equals("")) {
            //确保此时已经登录
            Cursor cursor = db.query("user", new String[]{"uid"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
            cursor.moveToNext();
            ContentValues values = new ContentValues();
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            cursor=db.query("walk",new String[]{"wid"},"uid=? and date=?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex("uid"))),format.format(date)},null,null,null);
            if(cursor.getCount()!=0) {
                cursor.moveToNext();
                values.put("step", totalStep);
                values.put("distance", totaldistance);
                values.put("date", format.format(date));
                db.update("walk",values,"wid=?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex("wid")))});
            }else{
                cursor = db.query("user", new String[]{"uid"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
                cursor.moveToNext();
                values.put("uid", cursor.getInt(cursor.getColumnIndex("uid")));
                values.put("step", totalStep);
                values.put("distance", totaldistance);
                values.put("date", format.format(date));
                db.insert("walk", null, values);
            }
        }
    }
    public Map<String,Integer> getSavedData(){
        //获取数据库数据以初始化记步
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        Map<String,Integer> map=new HashMap<String, Integer>();
        if(!sh.readUsername().equals("")) {
            Cursor cursor = db.query("user", new String[]{"uid"}, "uname=?", new String[]{sh.readUsername()}, null, null, null);
            cursor.moveToNext();
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            cursor=db.query("walk",new String[]{"step","distance"},"uid=? and date=?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex("uid"))),format.format(date)},null,null,null);
            if(cursor.getCount()!=0) {
                cursor.moveToNext();
                map.put("step", cursor.getInt(cursor.getColumnIndex("step")));
                map.put("dist", cursor.getInt(cursor.getColumnIndex("distance")));
            }else{
                Map<String,Integer> data=sh.getDataWalk();
                map.put("step",data.get("step"));
                map.put("dist",data.get("dist"));
            }
            cursor.close();
        }else{
            Map<String,Integer> data=sh.getDataWalk();
            map.put("step",data.get("step"));
            map.put("dist",data.get("dist"));
        }
        return map;
    }
    private void initData(){
        //记步初始化
        sh.initWalk();
        if(sh.getLast()) {
            //上一次有用户登录
            if (sh.readUsername().equals("")) {
                totaldistance = 0;
                totalStep = 0;
                previousStepCount = 0;
            } else {
                totaldistance = getSavedData().get("dist");
                totalStep = getSavedData().get("step");
                previousStepCount = 0;
            }
        }else{
            //上一次免登录
            totaldistance += getSavedData().get("dist");
            totalStep += getSavedData().get("step");
            previousStepCount = 0;
        }
        if (sh.readUsername().equals("")) {
            sh.setLast(false);
        }else {
            sh.setLast(true);
        }
        hasRecord=false;
        updateNotification();
    }
    private void initDataExit(){
        //退出登录记步初始化
        sh.initWalk();
        if (sh.readUsername().equals("")) {
            totaldistance = 0;
            totalStep = 0;
            previousStepCount = 0;
        } else {
            totaldistance = getSavedData().get("dist");
            totalStep = getSavedData().get("step");
            previousStepCount = 0;
        }
        hasRecord=false;
        updateNotification();
    }
    private void initBroadcastReceiver() {
        //接受广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction("com.example.proj.USER_EXIT");
        filter.addAction("com.example.proj.USER_LOGIN");
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case  Intent.ACTION_SHUTDOWN:
                        //关机保存
                        saveData();
                        break;
                    case Intent.ACTION_DATE_CHANGED:
                        //换日保存与初始化
                        saveData();
                        initData();
                        break;
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:
                        //实时保存数据库
                        saveData();
                        break;
                    case "com.example.proj.USER_EXIT":
                        //用户登出时保存数据并初始化
                        Log.e("walkservice", "receive broadcast exit");
                        saveData();
                        SharedHelper sh=new SharedHelper(getApplicationContext());
                        sh.clear();
                        sh.setAuto(false);
                        sh.setLast(true);
                        initDataExit();
                        break;
                    case "com.example.proj.USER_LOGIN":
                        //用户登入时初始化数据
                        Log.e("walkservice", "receive broadcast login");
                        initData();
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }
}