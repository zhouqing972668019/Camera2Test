package com.sunshanglei.camera.oneselfcamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SensorRecordService extends Service implements SensorEventListener {

    private static final String TAG = "SensorRecordService";
    private SensorManager mSensorManager;
    boolean isLogging=false;
    private static List<ComparableSensorEvent> sensorEventList = new ArrayList<ComparableSensorEvent>();
    String timeString;//传感器截止记录时间戳

    private int sensorSampleRate = SensorManager.SENSOR_DELAY_GAME;

    private static SensorRecordService instance;//当前类的实例

    private static String SENSOR_RECORD_PATH = Environment.getExternalStorageDirectory()+"/AndroidCamera/Sensor/";

    public static SensorRecordService instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;//初始化当前类实例
        //初始化传感器管理器
        mSensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    public void startLogging(String timestr) {
        sensorEventList.clear();
        timeString = timestr;
        isLogging = true;
    }

    public void stopLogging() {
        isLogging = false;
        logToFile(timeString);
    }

    public int stopLoggingAndReturn(){
        isLogging = false;
        return getStep();
    }

    public int getStep(){
        Set<ComparableSensorEvent> eventSet = new LinkedHashSet<ComparableSensorEvent>();
        eventSet.addAll(sensorEventList);
        float startStep = 0;
        float endStep = 0;
        boolean isStart = true;
        for (ComparableSensorEvent event : eventSet) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    System.out.println("answer:"+event.values[0]);
                    if(isStart){
                        startStep = event.values[0];
                        isStart = false;
                    }
                    endStep = event.values[0];
                    break;
            }
        }
        return (int)(endStep - startStep + 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorSampleRate = intent.getIntExtra("sensorSampleRate",SensorManager.SENSOR_DELAY_GAME);
        System.out.println("sensorSampleRate:"+Constant.SENSOR_SAMPLE_RATE_NAME[sensorSampleRate]);
        // 为加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),sensorSampleRate);
        // 为方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), sensorSampleRate);
        // 为陀螺仪传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorSampleRate);
        // 为磁场传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorSampleRate);
        // 为重力传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sensorSampleRate);
        // 为线性加速度传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), sensorSampleRate);
        // 为温度传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), sensorSampleRate);
        // 为光传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), sensorSampleRate);
        // 为压力传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), sensorSampleRate);
        //为计步传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), sensorSampleRate);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(isLogging)
        {
            //将变化的传感器值以及时间戳记录
            sensorEventList.add(new ComparableSensorEvent(sensorEvent, System.currentTimeMillis()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    //写文件
    public static void logToFile(String string) {
        Set<ComparableSensorEvent> eventSet = new LinkedHashSet<ComparableSensorEvent>();
        eventSet.addAll(sensorEventList);
        String machineName = android.os.Build.MODEL.replace(" ", "");
        if (!new File(SENSOR_RECORD_PATH).exists()) {
            new File(SENSOR_RECORD_PATH).mkdirs();
        }
        File outputFile = new File(SENSOR_RECORD_PATH, string + "_sensor_" + machineName + ".txt");
        new SensorLoggingAsyncTask().execute(eventSet, outputFile);
    }
}
