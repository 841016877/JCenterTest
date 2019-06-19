package com.zyh.test.simplevideoplayer.view;

/**
 * 描述
 * 作者 Zhang Yonghui
 * 创建日期 2019/4/18
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.zyh.test.simplevideoplayer.R;


/**
 * Created by zwj on 2016/7/11.
 * 自定义视频录制进度条
 */
public class VideoProgressView extends View {
    private int mRingWidth;
    private int mRingColor;
    private float mRadius;
    private Paint mProgressPaint;//进度条的画笔
    /**
     * 进度条最大值
     */
    private int maxValue = 200;
    /**
     * 当前进度值
     */
    private int currentValue;
    /**
     * 每次扫过的角度，用来设置进度条圆弧所对应的圆心角，alphaAngle=(currentValue/maxValue)*360
     */
    private float alphaAngle;
    private OnFinishListener listener;
    private ValueAnimator animator;


    public VideoProgressView(Context context) {
        this(context, null);
    }

    public VideoProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundView);
        mRingWidth = typedArray.getDimensionPixelSize(R.styleable.RoundView_vrRingWidth, (int) dip2px(6f));
        mRingColor = typedArray.getColor(R.styleable.RoundView_vrRingColor, Color.parseColor("#CBCBCB"));
        mRadius = typedArray.getDimension(R.styleable.RoundView_vrRadius, 100);
        typedArray.recycle();
        init();
    }

    private void init() {
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setDither(true);
        mProgressPaint.setStrokeWidth(mRingWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 分别获取期望的宽度和高度，并取其中较小的尺寸作为该控件的宽和高,并且不超过屏幕宽高
        int widthPixels = this.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽
        int heightPixels = this.getResources().getDisplayMetrics().heightPixels;//获取屏幕高
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int hedight = MeasureSpec.getSize(heightMeasureSpec);
        int minWidth = Math.min(widthPixels, width);
        int minHedight = Math.min(heightPixels, hedight);
        setMeasuredDimension(Math.min(minWidth, minHedight), Math.min(minWidth, minHedight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int center = this.getWidth() / 2;
        int radius = center - mRingWidth / 2;
        // 绘制进度圆弧
        drawCircle(canvas, center, radius);
    }

    /**
     * 绘制进度圆弧
     *
     * @param canvas 画布对象
     * @param center 圆心的x和y坐标
     * @param radius 圆的半径
     */
    private void drawCircle(Canvas canvas, int center, int radius) {
        mProgressPaint.setShader(null);
        mProgressPaint.setColor(Color.parseColor("#00000000"));
        // 设置绘制的圆为空心
        mProgressPaint.setStyle(Paint.Style.STROKE);
        // 画底部的空心圆
        canvas.drawCircle(center, center, radius, mProgressPaint);
        // 圆的外接正方形
        RectF oval = new RectF(center - radius, center - radius, center + radius, center + radius);
        // 设置圆弧的颜色
        mProgressPaint.setColor(mRingColor);
        // 把每段圆弧改成圆角的
//        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        // 计算每次画圆弧时扫过的角度，这里计算要注意分母要转为float类型，否则alphaAngle永远为0
        alphaAngle = currentValue * 360.0f / maxValue * 1.0f;
        canvas.drawArc(oval, -90, alphaAngle, false, mProgressPaint);
    }

    /**
     * 按进度显示百分比，可选择是否启用数字动画
     *
     * duration 动画时长
     */
    public void setDuration(int duration, OnFinishListener listener) {
        this.listener = listener;
        if (animator != null) {
            animator.cancel();
        } else {
            animator = ValueAnimator.ofInt(0, maxValue);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    currentValue = (int) animation.getAnimatedValue();
                    //一般只是希望在View发生改变时对UI进行重绘。invalidate()方法系统会自动调用 View的onDraw()方法。
                    invalidate();
                    if (maxValue == currentValue && VideoProgressView.this.listener != null) {
                        VideoProgressView.this.listener.onFinish();
                    }
                }
            });
            animator.setInterpolator(new LinearInterpolator());
        }
        animator.setDuration(duration);
        animator.start();
    }

    public void stopDuration() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    public interface OnFinishListener {
        /**
         * 结束回调
         */
        void onFinish();
    }

    public static int px2dip(int pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (dipValue * scale + 0.5f);
    }

}