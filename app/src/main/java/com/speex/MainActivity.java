package com.speex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.speex.thread.AudioTruckThread;
import com.speex.view.AudioImage;
import com.speex.view.RecordAnimationView;

import java.util.ArrayList;

public class MainActivity  extends AppCompatActivity  implements AudioImage.OnTouchCallBack {

    private TextView textview;

    private RecordAnimationView main_record;

    private AudioImage main_audio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();

        Speex speex = Speex.getInstance();

        textview = (TextView) findViewById(R.id.textview);
        textview.setText(speex.getEncodeSize() + "**"+speex.getDecodeSize());

        main_record = (RecordAnimationView) findViewById(R.id.main_record);
        main_audio = (AudioImage) findViewById(R.id.main_audio);

        main_audio.setCallBack(this);
    }

    @Override
    public void startRecord() {
        main_record.startAnimation();
    }

    @Override
    public void cancleOrUp() {
        main_record.stopAnimation();
    }

    @Override
    public void stopRecord() {

    }

    @Override
    public void finishRecord() {
        new AudioTruckThread(this).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speex.getInstance().closeSpeex();
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }
}
