package com.sunshanglei.camera.oneselfcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private String mCameraId = "0";//摄像头id（通常0代表后置摄像头，1代表前置摄像头）

    private Spinner spinner_pic_resolution;
    private Spinner spinner_sensor_sample_rate;
    private Spinner spinner_take_photo_interval;
    private Button btn_ok;

    private ArrayAdapter<String> pic_resolution_adapter;
    private ArrayAdapter<String> sensor_sample_rate_adapter;
    private ArrayAdapter<String> take_photo_interval_adapter;

    private int picResolutionIndex = 0;
    private int takePhotoInterval = 100;
    private int sensorSampleRate = SensorManager.SENSOR_DELAY_FASTEST;

    private List<String> sizeNameList;
    private List<Size> sizeList;

    private List<String> intervalNameList;
    private List<Integer> intervalList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        try {
            initViews();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void initViews() throws CameraAccessException {
        spinner_pic_resolution = (Spinner) findViewById(R.id.spinner_pic_resolution);
        spinner_sensor_sample_rate = (Spinner) findViewById(R.id.spinner_sensor_sample_rate);
        spinner_take_photo_interval = (Spinner) findViewById(R.id.spinner_take_photo_interval);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        getPicResolutionList();
        getCameraSizeList();

        pic_resolution_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizeNameList);
        pic_resolution_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_pic_resolution.setAdapter(pic_resolution_adapter);

        take_photo_interval_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervalNameList);
        take_photo_interval_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_take_photo_interval.setAdapter(take_photo_interval_adapter);

        sensor_sample_rate_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Constant.SENSOR_SAMPLE_RATE_NAME);
        sensor_sample_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner_sensor_sample_rate.setAdapter(sensor_sample_rate_adapter);

        spinner_pic_resolution.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                picResolutionIndex = arg2;
                //Toast.makeText(SettingActivity.this,picResolutionIndex+"",Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner_take_photo_interval.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                takePhotoInterval = intervalList.get(arg2);
                //Toast.makeText(SettingActivity.this,takePhotoInterval+"",Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner_sensor_sample_rate.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                sensorSampleRate = Constant.SENSOR_SAMPLE_RATE[arg2];
                //Toast.makeText(SettingActivity.this,sensorSampleRate+"",Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this,MainActivity.class);
//                private int picResolutionIndex = 0;
//                private int takePhotoInterval = 100;
//                private int sensorSampleRate = SensorManager.SENSOR_DELAY_FASTEST;
                intent.putExtra("picResolutionIndex",picResolutionIndex);
                intent.putExtra("takePhotoInterval",takePhotoInterval);
                intent.putExtra("sensorSampleRate",sensorSampleRate);
                startActivity(intent);
            }
        });
    }

    //获取相机支持的分辨率列表
    public void getCameraSizeList() throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // 获取指定摄像头的特性
        CameraCharacteristics characteristics
                = manager.getCameraCharacteristics(mCameraId);
        // 获取摄像头支持的配置属性
        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        sizeList = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
        Collections.sort(sizeList, new CompareSizesByArea());
        sizeNameList = new ArrayList<>();
        for (Size size : sizeList) {
            String name = size.getWidth() + "×" + size.getHeight();
            sizeNameList.add(name);
        }
    }

    //获取相机定时拍照的时间间隔列表
    public void getPicResolutionList(){
        intervalList = new ArrayList<>();
        intervalNameList = new ArrayList<>();
        for(int i=100;i<=2000;i+=100){
            intervalList.add(i);
            intervalNameList.add(i+"ms");
        }
    }

    // 为Size定义一个比较器Comparator
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
