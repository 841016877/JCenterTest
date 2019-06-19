package com.zyh.test.jcentertest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zyh.test.simplevideoplayer.VideoRecordManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoRecordManager.getInstance().startRecord("", this);
    }
}
