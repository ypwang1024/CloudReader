package com.example.jingbin.cloudreader.view.webview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

/**
 * web进度条，原作者: cenxiaozhong，在此基础上修改
 * 优化：
 * 1. progress同时反返回两次100时出现两次
 * 2. 当一条进度没跑完，又点击其他链接开始第二次进度时，第二次进度不出现
 *
 * @author jingbin
 */
public class WebProgress extends FrameLayout {

    /**
     * 进度条颜色
     */
    private int mColor;
    /**
     * 进度条的画笔
     */
    private Paint mPaint;
    /**
     * 进度条动画
     */
    private Animator mAnimator;
    /**
     * 控件的宽度
     */
    private int mTargetWidth = 0;
    /**
     * 控件的高度
     */
    private int mTargetHeight;

    /**
     * 默认匀速动画最大的时长
     */
    public static final int MAX_UNIFORM_SPEED_DURATION = 8 * 1000;
    /**
     * 默认加速后减速动画最大时长
     */
    public static final int MAX_DECELERATE_SPEED_DURATION = 450;
    /**
     * 结束动画时长 ， Fade out 。
     */
    public static final int DO_END_ANIMATION_DURATION = 600;

    /**
     * 当前匀速动画最大的时长
     */
    private static int CURRENT_MAX_UNIFORM_SPEED_DURATION = MAX_UNIFORM_SPEED_DURATION;
    /**
     * 当前加速后减速动画最大时长
     */
    private static int CURRENT_MAX_DECELERATE_SPEED_DURATION = MAX_DECELERATE_SPEED_DURATION;
    /**
     * 默认的高度
     */
    public static int WEB_INDICATOR_DEFAULT_HEIGHT = 3;
    /**
     * 标志当前进度条的状态
     */
    private int TAG = 0;
    public static final int UN_START = 0;
    public static final int STARTED = 1;
    public static final int FINISH = 2;

    private float mTarget = 0f;

    private float mCurrentProgress = 0F;

    public WebProgress(Context context) {
        this(context, null);
    }

    public WebProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mPaint = new Paint();
        mColor = Color.parseColor("#1aad19");
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);

        mTargetWidth = context.getResources().getDisplayMetrics().widthPixels;
        mTargetHeight = dip2px(WEB_INDICATOR_DEFAULT_HEIGHT);
    }

    public void setColor(int color) {
        this.mColor = color;
        mPaint.setColor(color);
    }

    public void setColor(String color) {
        this.setColor(Color.parseColor(color));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);

        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (wMode == MeasureSpec.AT_MOST) {
            w = w <= getContext().getResources().getDisplayMetrics().widthPixels ? w : getContext().getResources().getDisplayMetrics().widthPixels;
        }
        if (hMode == MeasureSpec.AT_MOST) {
            h = mTargetHeight;
        }
        this.setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRect(0, 0, mCurrentProgress / 100 * Float.valueOf(this.getWidth()), this.getHeight(), mPaint);
    }

    public void show() {
        this.setVisibility(View.VISIBLE);
        mCurrentProgress = 0f;
        startAnim(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mTargetWidth = getMeasuredWidth();
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        if (mTargetWidth >= screenWidth) {
            CURRENT_MAX_DECELERATE_SPEED_DURATION = MAX_DECELERATE_SPEED_DURATION;
            CURRENT_MAX_UNIFORM_SPEED_DURATION = MAX_UNIFORM_SPEED_DURATION;
        } else {
            //取比值
            float rate = this.mTargetWidth / Float.valueOf(screenWidth);
            CURRENT_MAX_UNIFORM_SPEED_DURATION = (int) (MAX_UNIFORM_SPEED_DURATION * rate);
            CURRENT_MAX_DECELERATE_SPEED_DURATION = (int) (MAX_DECELERATE_SPEED_DURATION * rate);
        }
    }


    public void setProgress(float progress) {
        // fix 同时返回两个 100，产生两次进度条的问题；
        if (TAG == UN_START && progress == 100f) {
            setVisibility(View.GONE);
            return;
        }

        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
        if (progress < 95f) {
            return;
        }
        if (TAG != FINISH) {
            startAnim(true);
        }
    }

    public void hide() {
        TAG = FINISH;
    }

    private void startAnim(boolean isFinished) {

        float v = isFinished ? 100 : 95;

        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        mCurrentProgress = mCurrentProgress == 0f ? 0.00000001f : mCurrentProgress;

        if (!isFinished) {
            ValueAnimator mAnimator = ValueAnimator.ofFloat(mCurrentProgress, v);
            float residue = 1f - mCurrentProgress / 100 - 0.05f;
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration((long) (residue * CURRENT_MAX_UNIFORM_SPEED_DURATION));
            mAnimator.addUpdateListener(mAnimatorUpdateListener);
            mAnimator.start();
            this.mAnimator = mAnimator;
        } else {

            ValueAnimator segment95Animator = null;
            if (mCurrentProgress < 95f) {
                segment95Animator = ValueAnimator.ofFloat(mCurrentProgress, 95);
                float residue = 1f - mCurrentProgress / 100f - 0.05f;
                segment95Animator.setInterpolator(new LinearInterpolator());
                segment95Animator.setDuration((long) (residue * CURRENT_MAX_DECELERATE_SPEED_DURATION));
                segment95Animator.setInterpolator(new DecelerateInterpolator());
                segment95Animator.addUpdateListener(mAnimatorUpdateListener);
            }

            ObjectAnimator mObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
            mObjectAnimator.setDuration(DO_END_ANIMATION_DURATION);
            ValueAnimator mValueAnimatorEnd = ValueAnimator.ofFloat(95f, 100f);
            mValueAnimatorEnd.setDuration(DO_END_ANIMATION_DURATION);
            mValueAnimatorEnd.addUpdateListener(mAnimatorUpdateListener);

            AnimatorSet mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(mObjectAnimator, mValueAnimatorEnd);

            if (segment95Animator != null) {
                AnimatorSet mAnimatorSet1 = new AnimatorSet();
                mAnimatorSet1.play(mAnimatorSet).after(segment95Animator);
                mAnimatorSet = mAnimatorSet1;
            }
            mAnimatorSet.addListener(mAnimatorListenerAdapter);
            mAnimatorSet.start();
            mAnimator = mAnimatorSet;
        }

        TAG = STARTED;
        mTarget = v;

    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float t = (float) animation.getAnimatedValue();
            WebProgress.this.mCurrentProgress = t;
            WebProgress.this.invalidate();
        }
    };

    private AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            doEnd();
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /**
         * animator cause leak , if not cancel;
         */
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    private void doEnd() {
        if (TAG == FINISH && mCurrentProgress == 100f) {
            setVisibility(GONE);
            mCurrentProgress = 0f;
            this.setAlpha(1f);
        }
        TAG = UN_START;
    }

    public void reset() {
        mCurrentProgress = 0;
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
    }

    public void setProgress(int newProgress) {
        setProgress(Float.valueOf(newProgress));
    }

    /**
     * 为单独处理WebView进度条
     */
    public void setWebProgress(int newProgress) {
        if (newProgress == 0) {
            reset();
        } else if (newProgress > 0 && newProgress <= 10) {
            show();
        } else if (newProgress > 10 && newProgress < 95) {
            setProgress(newProgress);
        } else {
            setProgress(newProgress);
            hide();
        }
    }

    public LayoutParams offerLayoutParams() {
        return new LayoutParams(-1, mTargetHeight);
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}