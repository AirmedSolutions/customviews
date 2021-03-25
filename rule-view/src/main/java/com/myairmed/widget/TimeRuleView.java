package com.myairmed.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * TimeRuleView
 *
 * Time ruler control
 * (Imitate the time selection control in "Fluorite Cloud Video")
 *
 * Features:
 *  - You can choose any time within a day (00:00 ~ 24:00), accurate to the second level
 *  - Multiple time blocks can be displayed
 *  - Support sliding and inertial sliding
 *  - Support zoom interval
 *  - Supports continuous switching between sliding and zooming
 *
 * Ideas：
 *  - Time drawing ideas reference {@link RuleView}
 *  - Time zoom, using zoom gesture detector ScaleGestureDetector
 *  - Level estimation method of zooming: enter the default ratio of 1, according to the number of seconds and width occupied every time,
 *    the width range of each level can be estimated, and then divide by the width corresponding to the default level to calculate the zoom ratio
 *  - Inertial sliding, using VelocityTracker
 *  - The continuous operation between zooming and sliding, the condition for the start and end of ScaleGestureDetector is to press and release the second finger,
 *    So onTouchEvent() should use getActionMasked() to monitor the DOWN(ACTION_POINTER_DOWN) and UP(ACTION_POINTER_UP) events of the second finger,
 *    MOVE is the same
 *  - Time block, composed of start time and end time, just use an ordered set to load
 *
 */
public class TimeRuleView extends View {

    private static final boolean LOG_ENABLE = BuildConfig.DEBUG;
    public static final int MAX_TIME_VALUE = 24 * 3600;
    
    private int bgColor;
    /**
     * Scale color
     */
    private int gradationColor;
    /**
     * The height of the time block
     */
    private float partHeight;
    /**
     * The color of the time block
     */
    private int partColor;
    /**
     * Scale width
     */
    private float gradationWidth;
    /**
     * The length of seconds, minutes, and time scale
     */
    private float secondLen;
    private float minuteLen;
    private float hourLen;
    /**
     * Scale value color, size, and distance from time
     */
    private int gradationTextColor;
    private float gradationTextSize;
    private float gradationTextGap;

    /**
     * Current time, unit: s
     */
    private @IntRange(from = 0, to = MAX_TIME_VALUE) int currentTime;
    /**
     * Pointer color
     */
    private int indicatorColor;
    /**
     * The side length of the triangle on the pointer
     */
    private float indicatorTriangleSideLen;
    /**
     * Pointer width
     */
    private float indicatorWidth;
    
    /**
     * The unit second value corresponding to the smallest unit, a total of four levels: 10s, 1min, 5min, 15min
     * Index values corresponding to {@link #mPerTextCounts} and {@link #mPerCountScaleThresholds}
     *
     * Can be combined and optimized into an array
     */
    private static final int[] mUnitSeconds = {
            10,     10,     10,     10,
            60,     60,
            5*60,   5*60,
            15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60
    };

    /**
     * Numerical display interval. A total of 13 levels, the maximum value of the first level, not including
     */
    @SuppressWarnings("all")
    private static int[] mPerTextCounts = {
            60,         60,         2 * 60,     4 * 60, // 10s/unit: 最大值, 1min, 2min, 4min
            5 * 60,     10 * 60, // 1min/unit: 5min, 10min
            20 * 60,    30 * 60, // 5min/unit: 20min, 30min
            3600,       2 * 3600,   3 * 3600,   4 * 3600,   5 * 3600,   6 * 3600 // 15min/unit
    };

    /**
     * The threshold corresponding to {@link #mPerTextCounts}, between this threshold and the previous threshold, the interval value corresponding to this threshold is used
     * For example: 1.5f represents the threshold corresponding to 4*60, if mScale >= 1.5f && mScale <1.8f, use 4*60
     *
     * These values are all estimated
     */
    @SuppressWarnings("all")
    private float[] mPerCountScaleThresholds = {
            6f,     3.6f,   1.8f,   1.5f, // 10s/unit: 最大值, 1min, 2min, 4min
            0.8f,     0.4f,   // 1min/unit: 5min, 10min
            0.25f,  0.125f, // 5min/unit: 20min, 30min
            0.07f,  0.04f,  0.03f,  0.025f, 0.02f,  0.015f // 15min/unit: 1h, 2h, 3h, 4h, 5h, 6h
    };
    /**
     * The default mScale is 1
     */
    private float mScale = 1;
    /**
     * The interval corresponding to 1s is better to estimate
     */
    private final float mOneSecondGap = dp2px(12) / 60f;
    /**
     * The interval corresponding to the current minimum unit second value
     */
    private float mUnitGap = mOneSecondGap * 60;
    /**
     * Default index value
     */
    private int mPerTextCountIndex = 4;
    /**
     * 一The number of seconds represented by the grid. Default 1min
     */
    private int mUnitSecond = mUnitSeconds[mPerTextCountIndex];
    
    /**
     * Half the width of the numeric text: the time format is "00:00", so the length is fixed
     */
    private final float mTextHalfWidth;

    private final int SCROLL_SLOP;
    private final int MIN_VELOCITY;
    private final int MAX_VELOCITY;
    
    /**
     * The distance between the current time and 00:00
     */
    private float mCurrentDistance;


    private Paint mPaint;
    private TextPaint mTextPaint;
    private Path mTrianglePath;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    /**
     * Zoom gesture detector
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private int mWidth, mHeight;
    private int mHalfWidth;

    private int mInitialX;
    private int mLastX, mLastY;
    private boolean isMoving;
    private boolean isScaling;

    private List<TimePart> mTimePartList;
    private OnTimeChangedListener mListener;

    public interface OnTimeChangedListener{
        void onTimeChanged(int newTimeValue);
    }

    /**
     * Time slice
     */
    public static class TimePart{
        /**
         * Start time, unit: s, value range ∈[0, 86399]
         *  0       —— 00:00:00
         *  86399   —— 23:59:59
         */
        public int startTime;

        /**
         * End time, must be greater than {@link #startTime}
         */
        public int endTime;
    }

    public TimeRuleView(Context context) {
        this(context, null);
    }

    public TimeRuleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        init(context);
        initScaleGestureDetector(context);

        mTextHalfWidth = mTextPaint.measureText("00:00") * .5f;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        SCROLL_SLOP = viewConfiguration.getScaledTouchSlop();
        MIN_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();

        calculateValues();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeRuleView);
        bgColor = ta.getColor(R.styleable.TimeRuleView_zjun_bgColor, Color.parseColor("#EEEEEE"));
        gradationColor = ta.getColor(R.styleable.TimeRuleView_zjun_gradationColor, Color.GRAY);
        partHeight = ta.getDimension(R.styleable.TimeRuleView_trv_partHeight, dp2px(20));
        partColor = ta.getColor(R.styleable.TimeRuleView_trv_partColor, Color.parseColor("#F58D24"));
        gradationWidth = ta.getDimension(R.styleable.TimeRuleView_trv_gradationWidth, 1);
        secondLen = ta.getDimension(R.styleable.TimeRuleView_trv_secondLen, dp2px(3));
        minuteLen = ta.getDimension(R.styleable.TimeRuleView_trv_minuteLen, dp2px(5));
        hourLen = ta.getDimension(R.styleable.TimeRuleView_trv_hourLen, dp2px(10));
        gradationTextColor = ta.getColor(R.styleable.TimeRuleView_trv_gradationTextColor, Color.GRAY);
        gradationTextSize = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextSize, sp2px(12));
        gradationTextGap = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextGap, dp2px(2));
        currentTime = ta.getInt(R.styleable.TimeRuleView_trv_currentTime, 0);
        indicatorTriangleSideLen = ta.getDimension(R.styleable.TimeRuleView_trv_indicatorTriangleSideLen, dp2px(15));
        indicatorWidth = ta.getDimension(R.styleable.TimeRuleView_zjun_indicatorLineWidth, dp2px(1));
        indicatorColor = ta.getColor(R.styleable.TimeRuleView_zjun_indicatorLineColor, Color.RED);
        ta.recycle();
    }

    private void calculateValues() {
        mCurrentDistance = currentTime / mUnitSecond * mUnitGap;
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(gradationTextSize);
        mTextPaint.setColor(gradationTextColor);

        mTrianglePath = new Path();

        mScroller = new Scroller(context);
    }

    private void initScaleGestureDetector(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            /**
             * The zoom is triggered (it will be called 0 or more times),
             * If it returns true, it means that the current zoom event has been processed, and the detector will re-accumulate the zoom factor
             * Return false to continue accumulating the zoom factor.
             */
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                final float scaleFactor = detector.getScaleFactor();
                logD("onScale...focusX=%f, focusY=%f, scaleFactor=%f",
                        detector.getFocusX(), detector.getFocusY(), scaleFactor);

                final float maxScale = mPerCountScaleThresholds[0];
                final float minScale = mPerCountScaleThresholds[mPerCountScaleThresholds.length - 1];
                if (scaleFactor > 1 && mScale >= maxScale) {
                    // Zoomed in to the maximum
                    return true;
                } else if (scaleFactor < 1 && mScale <= minScale) {
                    // Has been reduced to the minimum
                    return true;
                }

                mScale *= scaleFactor;
                mScale = Math.max(minScale, Math.min(maxScale, mScale));
                mPerTextCountIndex = findScaleIndex(mScale);

                mUnitSecond = mUnitSeconds[mPerTextCountIndex];
                mUnitGap = mScale * mOneSecondGap * mUnitSecond;
                logD("onScale: mScale=%f, mPerTextCountIndex=%d, mUnitSecond=%d, mUnitGap=%f",
                        mScale, mPerTextCountIndex, mUnitSecond, mUnitGap);

                mCurrentDistance = (float) currentTime / mUnitSecond * mUnitGap;
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                logD("onScaleBegin...");
                isScaling = true;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
                logD("onScaleEnd...");
            }
        });

        // Adjust the minimum span value. The default value is 27mm (>=32mm of sw600dp), which is too large and the effect is not good
        Class clazz = ScaleGestureDetector.class;
        int newMinSpan = ViewConfiguration.get(context).getScaledTouchSlop();
        try {
            Field mMinSpanField = clazz.getDeclaredField("mMinSpan");
            mMinSpanField.setAccessible(true);
            mMinSpanField.set(mScaleGestureDetector, newMinSpan);
            mMinSpanField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dichotomy finds the index value corresponding to the zoom value
     */
    private int findScaleIndex(float scale) {
        final int size = mPerCountScaleThresholds.length;
        int min = 0;
        int max = size - 1;
        int mid = (min + max) >> 1;
        while (!(scale >= mPerCountScaleThresholds[mid] && scale < mPerCountScaleThresholds[mid - 1])) {
            if (scale >= mPerCountScaleThresholds[mid - 1]) {
                // Because the value goes to the cell and the index goes higher, it cannot be mid -1
                max = mid;
            } else {
                min = mid + 1;
            }
            mid = (min + max) >> 1;
            if (min >= max) {
                break;
            }
            if (mid == 0) {
                break;
            }
        }
        return mid;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Only handle the height of wrap_content, set to 80dp
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            mHeight = dp2px(60);
        }
        mHalfWidth = mWidth >> 1;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        final int actionMasked = event.getActionMasked();
        final int action = event.getAction();
        final int pointerCount = event.getPointerCount();
        logD("onTouchEvent: isScaling=%b, actionIndex=%d, pointerId=%d, actionMasked=%d, action=%d, pointerCount=%d",
                isScaling, actionIndex, pointerId, actionMasked, action, pointerCount);
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        mScaleGestureDetector.onTouchEvent(event);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                isMoving = false;
                mInitialX = x;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // As long as the second finger is pressed, sliding is prohibited
                isScaling = true;
                isMoving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isScaling) {
                    break;
                }
                int dx = x - mLastX;
                if (!isMoving) {
                    final int dy = y - mLastY;
                    if (Math.abs(x - mInitialX) <= SCROLL_SLOP || Math.abs(dx) <= Math.abs(dy)) {
                        break;
                    }
                    isMoving = true;
                }
                mCurrentDistance -= dx;
                computeTime();
                break;
            case MotionEvent.ACTION_UP:
                if (isScaling || !isMoving) {
                    break;
                }
                mVelocityTracker.computeCurrentVelocity(1000, MAX_VELOCITY);
                final int xVelocity = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= MIN_VELOCITY) {
                    // Inertial sliding
                    final int maxDistance = (int) (MAX_TIME_VALUE / mUnitGap * mUnitGap);
                    mScroller.fling((int) mCurrentDistance, 0, -xVelocity, 0, 0, maxDistance, 0, 0);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // One finger of the two was lifted to allow sliding. At the same time, assign the current position of the unlifted phone to the initial X
                isScaling = false;
                int restIndex = actionIndex == 0 ? 1 : 0;
                mInitialX = (int) event.getX(restIndex);
                break;
            default: break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    private void computeTime() {
        // No need to transfer float, it can definitely be divisible
        float maxDistance = MAX_TIME_VALUE / mUnitSecond * mUnitGap;
        // Limited scope
        mCurrentDistance = Math.min(maxDistance, Math.max(0, mCurrentDistance));
        currentTime = (int) (mCurrentDistance / mUnitGap * mUnitSecond);
        if (mListener != null) {
            mListener.onTimeChanged(currentTime);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Background
        canvas.drawColor(bgColor);

        // Scale
        drawRule(canvas);

        // period
        drawTimeParts(canvas);

        // Current time pointer
        drawTimeIndicator(canvas);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCurrentDistance = mScroller.getCurrX();
            computeTime();
        }
    }

    /**
     * Draw scale
     */
    private void drawRule(Canvas canvas) {
        // Move the canvas coordinate system
        canvas.save();
        canvas.translate(0, partHeight);
        mPaint.setColor(gradationColor);
        mPaint.setStrokeWidth(gradationWidth);

        // Scale
        int start = 0;
        float offset = mHalfWidth - mCurrentDistance;
        final int perTextCount = mPerTextCounts[mPerTextCountIndex];
        while (start <= MAX_TIME_VALUE) {
            // Scale
            if (start % 3600 == 0) {
                // Time degree
                canvas.drawLine(offset, 0, offset, hourLen, mPaint);
            } else if (start % 60 == 0) {
                // Subscale
                canvas.drawLine(offset, 0, offset, minuteLen, mPaint);
            } else{
                // Second scale
                canvas.drawLine(offset, 0, offset, secondLen, mPaint);
            }

            // Time value
            if (start % perTextCount == 0) {
                String text = formatTimeHHmm(start);
                canvas.drawText(text, offset - mTextHalfWidth, hourLen + gradationTextGap + gradationTextSize, mTextPaint);
            }

            start += mUnitSecond;
            offset += mUnitGap;
        }
        canvas.restore();
    }

    /**
     * Draw current time pointer
     */
    private void drawTimeIndicator(Canvas canvas) {
        // pointer
        mPaint.setColor(indicatorColor);
        mPaint.setStrokeWidth(indicatorWidth);
        canvas.drawLine(mHalfWidth, 0, mHalfWidth, mHeight, mPaint);

        // Equilateral triangle
        if (mTrianglePath.isEmpty()) {
            //
            final float halfSideLen = indicatorTriangleSideLen * .5f;
            mTrianglePath.moveTo(mHalfWidth - halfSideLen, 0);
            mTrianglePath.rLineTo(indicatorTriangleSideLen, 0);
            mTrianglePath.rLineTo(-halfSideLen, (float) (Math.sin(Math.toRadians(60)) * halfSideLen));
            mTrianglePath.close();
        }
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mTrianglePath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Draw time period
     */
    private void drawTimeParts(Canvas canvas) {
        if (mTimePartList == null) {
            return;
        }
        // Do not use rectangles, use straight lines to draw
        mPaint.setStrokeWidth(partHeight);
        mPaint.setColor(partColor);
        float start, end;
        final float halfPartHeight = partHeight * .5f;
        final float secondGap = mUnitGap / mUnitSecond;
        for (int i = 0, size = mTimePartList.size(); i < size; i++) {
            TimePart timePart = mTimePartList.get(i);
            start = mHalfWidth - mCurrentDistance + timePart.startTime * secondGap;
            end = mHalfWidth - mCurrentDistance + timePart.endTime * secondGap;
            canvas.drawLine(start, halfPartHeight, end, halfPartHeight, mPaint);
        }
    }

    /**
     * Format time HH:mm
     * @param timeValue specific time value
     * @return formatted string, eg: 3600 to 01:00
     */
    public static String formatTimeHHmm(@IntRange(from = 0, to = MAX_TIME_VALUE) int timeValue) {
        if (timeValue < 0){
            timeValue=0;
        }
        int hour = timeValue / 3600;
        int minute = timeValue % 3600 / 60;
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour).append(':');
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute);
        return sb.toString();
    }

    /**
     * Format time HH:mm:ss
     * @param timeValue specific time value
     * @return formatted string, eg: 3600 to 01:00
     */
    public static String formatTimeHHmmss(@IntRange(from = 0, to = MAX_TIME_VALUE) int timeValue) {
        int hour = timeValue / 3600;
        int minute = timeValue % 3600 / 60;
        int second = timeValue % 3600 % 60;
        StringBuilder sb = new StringBuilder();

        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour).append(':');

        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute);
        sb.append(':');

        if (second < 10) {
            sb.append('0');
        }
        sb.append(second);
        return sb.toString();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    @SuppressWarnings("all")
    private void logD(String format, Object... args) {
        if (LOG_ENABLE) {
            Log.d("MoneySelectRuleView", String.format("zjun@" + format, args));
        }
    }

    /**
     * Set time change monitoring event
     * @param listener monitor callback
     */
    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.mListener = listener;
    }

    /**
     * Set time block (segment) collection
     * @param timePartList Time block collection
     */
    public void setTimePartList(List<TimePart> timePartList) {
        this.mTimePartList = timePartList;
        postInvalidate();
    }

    /**
     * Set current time
     * @param currentTime current time
     */
    public void setCurrentTime(@IntRange(from = 0, to = MAX_TIME_VALUE) int currentTime) {
        this.currentTime = currentTime;
        calculateValues();
        postInvalidate();
    }
    
}
