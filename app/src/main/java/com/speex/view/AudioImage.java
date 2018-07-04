package com.speex.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.speex.Speex;
import com.speex.helper.FileHelper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressLint("AppCompatCustomView")
public class AudioImage extends ImageView implements View.OnTouchListener {

    private Vibrator vibrator;

    private OnTouchCallBack callBack;

    private long system_time;

    private int last_action;

    private int minBufferSize;

    private FileOutputStream fileOut;

    private boolean isAudio;

    public AudioImage(Context context) {
        this(context, null);
    }

    public AudioImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        minBufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        Toast.makeText(getContext(), ""+minBufferSize, Toast.LENGTH_SHORT).show();
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                vibrator.vibrate(100);
                system_time = System.currentTimeMillis();
                last_action = MotionEvent.ACTION_DOWN;
                startRecord();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (last_action != MotionEvent.ACTION_DOWN) {
                    stopRecord();
                    break;
                }
                last_action = -1;

                if (System.currentTimeMillis() - system_time < 2000)
                    Toast.makeText(getContext(), "录音时间小于2秒", Toast.LENGTH_SHORT).show();
                else
                    finishRecord();
                if (null != callBack)
                    callBack.cancleOrUp();


                break;

        }
        return true;
    }

    private void finishRecord() {
        isAudio = false;
        if(null != callBack)
            callBack.finishRecord();
        last_action = -1;
    }

    private void stopRecord() {
        if (null != callBack)
            callBack.stopRecord();
        isAudio = false;
    }

    private void startRecord() {
        if (null != callBack)
            callBack.startRecord();
        isAudio = true;
        try {
            fileOut = new FileOutputStream(FileHelper.getHelper().getAudi0File());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new AudioThread();
    }

    public void setCallBack(OnTouchCallBack callBack) {
        this.callBack = callBack;
    }

    public interface OnTouchCallBack {
        void startRecord();

        void cancleOrUp();

        void stopRecord();

        void finishRecord();
    }

    class AudioThread extends Thread {

        public AudioThread() {
            start();
        }

        @Override
        public void run() {
        	Speex speex = Speex.getInstance();
        	
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

            try {
                audioRecord.startRecording();
            } catch (Exception e) {
                e.printStackTrace();
                audioRecord.release();
                stopRecord();
                return;
            }

//            AudioAddThread addThread = new AudioAddThread(fileOut);
//            addThread.setRun(true);
//            addThread.start();

            while (isAudio) {
                short[] shorts = new short[speex.getEncodeSize()];
                int read = audioRecord.read(shorts, 0, speex.getEncodeSize());
                if (0 < read) {
//                	short[] shorts2 = shorts.clone();
                    try {
                    	byte[] bytes = speex.encodeSpeex(shorts);

//                        short[] bytes2 = speex.decodeSpeex(bytes);
//                        System.out.println(" getDecodeSize:" + speex.getDecodeSize());
//
//                        for(int i = 0;i < speex.getDecodeSize() ; i++) {
//                            //System.out.println("short string" + bytes2[i] + " getDecodeSize:" + speex.getDecodeSize());
//                            fileOut.write(bytes2[i]);
//                        }
                        System.out.println("Decode frame 1!");
//                        for(int i = 0;i<bytes.length;i++){
//                       	 System.out.println("bytesstring"+bytes[i]);
//                        }
                        fileOut.write(bytes,0,bytes.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    addThread.addFormData(bytes2);
                }
            }
//            addThread.setRun(false);
            audioRecord.stop();
            audioRecord.release();
        }
    }

    /**
     * 将16位的short转换成byte数组
     *
     * @param s
     *            short
     * @return byte[] 长度为2
     * */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }
}

