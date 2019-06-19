package com.zyh.test.simplevideoplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.zyh.test.simplevideoplayer.VideoRecordManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 描述
 * 作者 Zhang Yonghui
 * 创建日期 2019/4/18
 */
public class VideoRecordSurface extends SurfaceView implements SurfaceHolder.Callback, MediaRecorder.OnErrorListener {
    private final String TAG = this.getClass().getSimpleName();
    //最大时间
    public final int mRecordMaxTime = 60;
    //最小时间
    public final int mRecordMiniTime = 2;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    /**
     * 存储的路径
     */
    private File mRecordFile;
    private MediaRecorder mMediaRecorder;
    //开启时间
    private int mTimeCount = 1;
    private Timer mTimer;
    private OnRecordListener mOnRecordListener;
    /**
     * 默认路径
     */
    private String basePath;
    private Camera.Size size;
    private int videoQuality;

    public VideoRecordSurface(Context context, String videoSavePath) {
        this(context, null, videoSavePath);
    }

    public VideoRecordSurface(Context context, AttributeSet attrs, String videoSavePath) {
        this(context, attrs, -1, videoSavePath);
    }

    public VideoRecordSurface(Context context, AttributeSet attrs, int defStyleAttr, String videoSavePath) {
        super(context, attrs, defStyleAttr);
        basePath = videoSavePath;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        initCamera();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null) {
                mr.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void repCamera() {
        initCamera();
    }

    /**
     * 初始化摄像头
     *
     * @throws IOException
     * @author zwj
     * @date 2016-06-21
     */
    private void initCamera() {
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open();
            if (mCamera == null) {
                return;
            }
            initParameters();
            //竖屏显示
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.unlock();
        } catch (Exception e) {
            e.printStackTrace();
            freeCameraResource();
        }
    }

    private void initParameters() {
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        Camera.Parameters mParams = mCamera.getParameters();
        mParams.setPreviewSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        size = mParams.getPreviewSize();
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(mParams);
    }

    /**
     * 释放摄像头资源
     *
     * @author zwj
     * @date 2016-06-21
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * 开始录制视频
     *
     * onRecordListener 回调接口,结束和录像进度
     * videoQuality 视频质量
     * @author zwj
     * 2016-06-21
     */
    public void record(final OnRecordListener onRecordListener, int orientationHintDegrees, int videoQuality) {
        this.mOnRecordListener = onRecordListener;
        this.videoQuality = videoQuality;
        createRecordDir();
        try {
            initRecord(orientationHintDegrees);
            // 时间计数器重新赋值
            mTimeCount = 1;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 设置进度条
                    mOnRecordListener.onRecordProgress(mTimeCount);
                    // 达到指定时间，停止拍摄
                    if (mTimeCount >= mRecordMaxTime) {
                        stop();
                    }
                    mTimeCount++;
                }
            }, 0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getVideoThumbnail(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        Bitmap bitmap = null;
        Log.i("tag",filePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 初始化
     *
     * @author zwj
     * @date 2016-06-21
     */
    private void initRecord(int orientationHintDegrees) {
        try {
            if (mMediaRecorder == null) {
                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setOnErrorListener(this);
            } else {
                mMediaRecorder.reset();
            }
            //1.设置摄像头解锁，和MediaRecorder
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            //2.设置视频源
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置视频输出的格式和编码
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            int videoBitRate;
            int camcorderQuality;
            switch (videoQuality) {
                case VideoRecordManager.VIDEOQUALITY_HIGH:
                    videoBitRate = 5 * 1024 * 1024;
                    camcorderQuality = CamcorderProfile.QUALITY_2160P;
                    break;

                case VideoRecordManager.VIDEOQUALITY_MIDDLE:
                    videoBitRate = 2 * 1024 * 1024;
                    camcorderQuality = CamcorderProfile.QUALITY_1080P;
                    break;

                case VideoRecordManager.VIDEOQUALITY_LOW:
                default:
                    videoBitRate = 1024 * 1024;
                    camcorderQuality = CamcorderProfile.QUALITY_720P;
                    break;
            }
            CamcorderProfile mProfile = CamcorderProfile.get(camcorderQuality);
            //设置视频的分辨率
            mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mMediaRecorder.setAudioEncodingBitRate(64);
            // 设置编码比特率 与视频的清晰度有关 越高越清晰
            if (mProfile.videoBitRate > videoBitRate) {
                mMediaRecorder.setVideoEncodingBitRate(videoBitRate);
            } else {
                mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            }
            mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            // 通用性,兼容性较好
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // H264压缩率较高
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (orientationHintDegrees != 0   &&
                    orientationHintDegrees != 90  &&
                    orientationHintDegrees != 180 &&
                    orientationHintDegrees != 270){
                orientationHintDegrees = 90;
            }
            mMediaRecorder.setOrientationHint(orientationHintDegrees);// 输出旋转90度，保持竖屏录制
            //3.设置输出
            mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
            mMediaRecorder.prepare();

            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFirst = true;
    /**
     * 停止拍摄
     *
     * @author zwj
     * 2016-06-21
     */
    public void stop() {
        stopRecord();
        releaseRecord();
        freeCameraResource();
        if (mTimeCount > mRecordMiniTime) {
            if (mOnRecordListener != null) {
                if(isFirst){
                    mOnRecordListener.onRecordFinish();
                    isFirst = false;
                }
            }
        }
    }

    /**
     * 释放资源
     *
     * @author zwj
     * @date 2016-06-21
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 停止录制
     *
     * @author zwj
     * 2016-06-21
     */
    public void stopRecord() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mMediaRecorder != null) {
            // 设置后不会崩
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置视频路径
     */
    private void createRecordDir() {
        File sampleDir = new File(basePath);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        File videoFile = sampleDir;
        // 创建文件
        try {
            //mp4格式
            mRecordFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".mp4", videoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRecordDir() {
        if (mRecordFile == null) {
            return "";
        }
        return mRecordFile.getPath();
    }

    public String getRecordThumbDir() {
        if (mRecordFile == null) {
            return "";
        }
        return mRecordFile.getPath() + ".jpg";
    }


    /**
     * 录制完成回调接口
     */
    public interface OnRecordListener {
        void onRecordFinish();

        /**
         * 录制进度
         */
        void onRecordProgress(int progress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio = 1f * size.height / size.width;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / ratio);
        int wms = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(wms, hms);
    }
}

