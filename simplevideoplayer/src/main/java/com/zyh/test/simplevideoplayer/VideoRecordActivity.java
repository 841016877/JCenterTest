package com.zyh.test.simplevideoplayer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;


import com.zyh.test.simplevideoplayer.view.VideoProgressView;
import com.zyh.test.simplevideoplayer.view.VideoRecordSurface;

import java.io.File;

/**
 * 描述 视频录制页面
 * 作者 Zhang Yonghui
 * 创建日期 2019-04-16
 */
public class VideoRecordActivity extends Activity implements VideoRecordSurface.OnRecordListener {

    private static final String TAG = "VideoRecordActivity";
    private FrameLayout frameLayout;
    //播放进度
    private VideoProgressView videoProgressView;
    private int iTime;
    private VideoRecordSurface videoRecordSurface;
    private String videoSavePath;
    private OrientationSensorListener listener;
    private SensorManager sensorManager;
    private Sensor sensor;
    private CheckBox cb_record;
    private RelativeLayout includePlay;
    private VideoView videoView;
    private boolean isFirst = true;
    private ImageButton ibReturn, ibOk;
    private int videoQuality;
    private ObjectAnimator animationOk;
    private ObjectAnimator animationReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_activity_vedio_record);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener();
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        //必须传递的值
        videoSavePath = getIntent().getStringExtra(VideoRecordManager.K_VIDEO_SAVE_PATH);
        videoQuality = getIntent().getIntExtra(VideoRecordManager.K_VIDEO_QUALITY, VideoRecordManager.VIDEOQUALITY_LOW);
        File file = new File(videoSavePath);
        if (!file.exists()) {
            file.mkdir();
        }
        initView();
    }

    private void initView() {
        videoProgressView = (VideoProgressView) findViewById(R.id.libVideoRecorder_progress);
        includePlay = (RelativeLayout) findViewById(R.id.include_play);
        videoRecordSurface = new VideoRecordSurface(this, videoSavePath);
        frameLayout = (FrameLayout) findViewById(R.id.libVideoRecorder_fl);
        frameLayout.addView(videoRecordSurface);
        cb_record = (CheckBox) findViewById(R.id.cb_record);
        cb_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean flag) {
                if (flag) {
                    cb_record.setText("");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            videoRecordSurface.record(VideoRecordActivity.this, listener.getOrientationHintDegrees(), videoQuality);
                        }
                    }).start();
                    videoProgressView.setVisibility(View.VISIBLE);
                    videoProgressView.setDuration(videoRecordSurface.mRecordMaxTime * 1000, null);
                } else {
                    videoProgressView.stopDuration();
                    videoProgressView.setVisibility(View.GONE);
                    cb_record.setText("点击拍");
                    if (iTime <= videoRecordSurface.mRecordMiniTime) {
                        Toast.makeText(VideoRecordActivity.this, "录制时间太短", Toast.LENGTH_SHORT).show();
                        videoRecordSurface.stopRecord();
                        videoRecordSurface.repCamera();
                    } else if (iTime < videoRecordSurface.mRecordMaxTime) {
                        Log.d(TAG, "stop: "+ System.currentTimeMillis());
                        cb_record.setVisibility(View.GONE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                videoRecordSurface.stop();
                            }
                        }).start();
                    }
                }
            }
        });
    }

    /**
     * 录制结束回调事件
     */
    @Override
    public void onRecordFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cb_record.isChecked()) {
                    cb_record.setChecked(false);
                }
                includePlay.setVisibility(View.VISIBLE);
                frameLayout.removeAllViews();
                initPlayView();
            }
        });
    }

    /**
     * 初始化播放控件
     */
    private void initPlayView() {
        videoView = (VideoView) findViewById(R.id.libPlayVideo_videoView);
        ibReturn = (ImageButton) findViewById(R.id.ib_return);
        ibOk = (ImageButton) findViewById(R.id.ib_ok);
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float translationValue = (float) (displayMetrics.widthPixels * 0.24);
        animationOk = ObjectAnimator.ofFloat(ibOk, "translationX", translationValue);
        animationOk.setDuration(200);
        animationOk.start();
        animationReturn = ObjectAnimator.ofFloat(ibReturn, "translationX", -translationValue);
        animationReturn.setDuration(200);
        animationReturn.start();
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (isFirst) {
                    isFirst = false;
                    Toast.makeText(VideoRecordActivity.this, "播放该视频异常", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        //视频播放地址
        String mVideoPath = videoRecordSurface.getRecordDir();
        File file = new File(mVideoPath);
        if (file.exists()) {
            videoView.setVideoPath(file.getAbsolutePath());
            videoView.start();
        } else {
            Log.e("tag", "not found video " + mVideoPath);
        }
    }

    /**
     * 取消事件
     * @param view
     */
    public void cancel(View view) {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        animationOk.reverse();
        animationOk.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                cb_record.setVisibility(View.VISIBLE);
                includePlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animationReturn.reverse();
        videoRecordSurface = new VideoRecordSurface(this, videoSavePath);
        frameLayout.addView(videoRecordSurface);
    }

    /**
     * 确认事件
     * @param view
     */
    public void ok(View view) {
        if (videoRecordSurface == null) {
            return;
        }
        //视频首张照片
        String recordThumbDir = videoRecordSurface.getRecordThumbDir();
        //视频播放地址
        String recordMp4Dir = videoRecordSurface.getRecordDir();
        Intent intent = new Intent();
        intent.putExtra(VideoRecordManager.K_VIDEO_SAVE_PATH, recordMp4Dir);
        intent.putExtra(VideoRecordManager.K_VIDEO_THUMB_PATH, recordThumbDir);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(listener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onRecordProgress(int progress) {
        iTime = progress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        if (videoRecordSurface != null) {
            videoRecordSurface.stop();
        }
    }
}

