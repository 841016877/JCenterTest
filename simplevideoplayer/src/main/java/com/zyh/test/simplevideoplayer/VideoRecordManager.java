package com.zyh.test.simplevideoplayer;

import android.app.Activity;
import android.content.Intent;

/**
 * 描述
 * 作者 Zhang Yonghui
 * 创建日期 2019/4/22
 */
public class VideoRecordManager {
    private static VideoRecordManager videoRecordManager;
    public static final String K_VIDEO_SAVE_PATH = "videoSavePath";
    public static final String K_VIDEO_THUMB_PATH = "videoThumbPath";
    public static final String K_VIDEO_QUALITY = "videoQuality";
    public static final int VIDEOQUALITY_LOW = 1;
    public static final int VIDEOQUALITY_MIDDLE = 2;
    public static final int VIDEOQUALITY_HIGH = 3;
    public static final int REQUEST_CODE = 1001;

    private VideoRecordManager(){}

    public static VideoRecordManager getInstance(){
        if (videoRecordManager == null) {
            synchronized (VideoRecordManager.class) {
                videoRecordManager = new VideoRecordManager();
            }
        }
        return videoRecordManager;
    }

    /**
     * 开启录制
     * @param videoPath 小视频录制后存储位置
     * @param videoQuality 视频质量
     * @param activity
     */
    public void startRecord(String videoPath, int videoQuality, Activity activity){
        Intent intent = new Intent(activity, VideoRecordActivity.class);
        intent.putExtra(K_VIDEO_SAVE_PATH, videoPath);
        intent.putExtra(K_VIDEO_QUALITY, videoQuality);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 开启录制
     * @param videoPath 小视频录制后存储位置
     * @param activity
     */
    public void startRecord(String videoPath, Activity activity){
        startRecord(videoPath, VIDEOQUALITY_LOW, activity);
    }

}