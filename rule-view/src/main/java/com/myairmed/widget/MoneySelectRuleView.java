package com.myairmed.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;

/**
 * MoneySelectRuleView
 * Amount selection tape measure control
 */
public class MoneySelectRuleView extends View {

    private static final boolean LOG_ENABLE = BuildConfig.DEBUG;

    /**
     * Sliding threshold
     */
    private final int TOUCH_SLOP;
    /**
     * Inertial sliding minimum and maximum speed
     */
    private final int MIN_FLING_VELOCITY;
    private final int MAX_FLING_VELOCITY;
    
    private int bgColor;
    private int gradationColor;
    private float gradationHeight;
    private float gradationShortLen;
    private float gradationLongLen;
    private float gradationShortWidth;
    private float gradationLongWidth;
    private float gradationValueGap;
    private float gradationTextSize;
    private int gradationTextColor;

    private float balanceTextSize;
    private int indicatorColor;
    private float unitGap;

    private String balanceText;
    private float balanceGap;

    private int maxValue;
    private int currentValue;
    private int balanceValue;
    private int valueUnit;
    private int valuePerCount;

    
    private float mCurrentDistance;
    private int mWidthRangeValue;
    private int mRangeDistance;

    private int mWidth, mHeight, mHalfWidth;
    private Paint mPaint;
    private TextPaint mTextPaint;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private OnValueChangedListener mListener;

    public interface OnValueChangedListener {
        /**
         * Called when the value changes
         * @param newValue The new value after the change
         */
        void onValueChanged(int newValue);

    }

    public MoneySelectRuleView(Context context) {
        this(context, null);
    }

    public MoneySelectRuleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoneySelectRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        // Initialize final constants, initial values must be assigned in the construction
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        TOUCH_SLOP = viewConfiguration.getScaledTouchSlop();
        MIN_FLING_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_FLING_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();

        calculateValues();
        init(context);
    }

    private void calculateValues() {
        mCurrentDistance = (float) currentValue / valueUnit * unitGap;
        mRangeDistance = (int) (maxValue / valueUnit * unitGap);
        mWidthRangeValue = (int) (mWidth / unitGap * valueUnit);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(gradationColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(balanceTextSize);
        mTextPaint.setColor(gradationTextColor);

        mScroller = new Scroller(context);

        mVelocityTracker = VelocityTracker.obtain();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MoneySelectRuleView);
        bgColor = ta.getColor(R.styleable.MoneySelectRuleView_zjun_bgColor, Color.parseColor("#F5F5F5"));
        gradationColor = ta.getColor(R.styleable.MoneySelectRuleView_zjun_gradationColor, Color.LTGRAY);
        gradationHeight = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationHeight, dp2px(40));
        gradationShortLen = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationShortLen, dp2px(6));
        gradationLongLen = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationLongLen, gradationShortLen * 2);
        gradationShortWidth = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationShortWidth, 1);
        gradationLongWidth = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationLongWidth, gradationShortWidth);
        gradationValueGap = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationValueGap, dp2px(8));
        gradationTextSize = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_gradationTextSize, sp2px(12));
        gradationTextColor = ta.getColor(R.styleable.MoneySelectRuleView_zjun_textColor, Color.GRAY);
        indicatorColor = ta.getColor(R.styleable.MoneySelectRuleView_zjun_indicatorLineColor, Color.parseColor("#eb4c1c"));
        balanceTextSize = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_balanceTextSize, sp2px(10));
        unitGap = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_unitGap, dp2px(6));
//        balanceText = ta.getString(R.styleable.MoneySelectRuleView_msrv_balanceText);
//        if (TextUtils.isEmpty(balanceText)) {
//            balanceText = context.getString(R.string.balance_text);
//        }
        balanceGap = ta.getDimension(R.styleable.MoneySelectRuleView_msrv_balanceGap, dp2px(4));
        maxValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_maxValue, 150);
        currentValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_currentValue, 0);
        balanceValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_balanceValue, 0);
        valueUnit = ta.getInt(R.styleable.MoneySelectRuleView_msrv_valueUnit, 1);
        valuePerCount = ta.getInt(R.styleable.MoneySelectRuleView_msrv_valuePerCount, 10);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHalfWidth = mWidth >> 1;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = dp2px(60);
            gradationHeight = dp2px(40);
        }

        mWidthRangeValue = (int) (mWidth / unitGap * valueUnit);

        setMeasuredDimension(mWidth, mHeight);
    }

    private int mDownX, mDownY;
    private int mLastX, mLastY;
    private boolean mIsMoving;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        logD("onTouchEvent: action=%d", action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsMoving = false;
                mDownX = x;
                mDownY = y;
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final int dx = x - mLastX;
                if (!mIsMoving) {
                    final int dy = y - mLastY;
                    if (Math.abs(x - mDownX) <= TOUCH_SLOP || Math.abs(dx) < Math.abs(dy)) {
                        break;
                    }
                    mIsMoving = true;
                }
                mCurrentDistance -= dx;
                computeValue();
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsMoving) {
                    break;
                }
                // Calculation speed
                mVelocityTracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY);
                // Get the current horizontal speed
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                logD("up: xVelocity=%d", xVelocity);
                if (Math.abs(xVelocity) < MIN_FLING_VELOCITY) {
                    // Sliding scale
                    scrollToGradation();
                } else {
                    // Inertial sliding.
                    mScroller.fling((int) mCurrentDistance, 0, -xVelocity, 0, 0, mRangeDistance, 0, 0);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()) {
                    // End the sliding and set the value to the end value immediately
                    mScroller.abortAnimation();
                }
                break;
            default: break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    /**
     * Swipe to the nearest scale
     */
    private void scrollToGradation() {
        // Nearest ticks
        currentValue = Math.round(mCurrentDistance / unitGap) * valueUnit;
        // Check boundary
        currentValue = Math.min(maxValue, Math.max(0, currentValue));
        // Calculate the new scale position
        mCurrentDistance = currentValue / valueUnit * unitGap;
        logD("scrollToGradation: currentValue=%d, mCurrentDistance=%f", currentValue, mCurrentDistance);
        if (mListener != null) {
            mListener.onValueChanged(currentValue);
        }
        invalidate();
    }

    /**
     * Check the distance and recalculate the current value
     */
    private void computeValue() {
        logD("computeValue: mRangeDistance=%d, mCurrentDistance=%f", mRangeDistance, mCurrentDistance);
        mCurrentDistance = Math.min(mRangeDistance, Math.max(0, mCurrentDistance));
        currentValue = (int)(mCurrentDistance / unitGap) * valueUnit;
        if (mListener != null) {
            mListener.onValueChanged(currentValue);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // background
        canvas.drawColor(bgColor);
        // Scale value
        drawRule(canvas);
        // Draw pointer
        drawIndicator(canvas);
    }

    /**
     * Draw scale, amount, and remaining amount
     */
    private void drawRule(Canvas canvas) {
        canvas.save();
        canvas.translate(0, gradationHeight);

        // reference line
        mPaint.setStrokeWidth(gradationShortWidth);
        canvas.drawLine(0, 0, mWidth, 0, mPaint);

        // Scale, value
        final int expend = 3 * valueUnit;
        // Start scale
        int start = (int) ((mCurrentDistance - mHalfWidth) / unitGap) * valueUnit;
        start = Math.max(0, start - expend);
        int end = Math.min(maxValue, (start + expend) + mWidthRangeValue + expend);
        float startOffset = mHalfWidth - (mCurrentDistance - start / valueUnit * unitGap);
        final int perCount = valuePerCount * valueUnit;
        // Remaining amount: round down
        final int balance = balanceValue / valueUnit * valueUnit;
        logD("drawRule: mCurrentDistance=%f, start=%d, end=%d, startOffset=%f, perCount=%d",
                mCurrentDistance, start, end, startOffset, perCount);
        while (start <= end) {
            if (start % perCount == 0) {
                // Scale
                mPaint.setStrokeWidth(gradationLongWidth);
                canvas.drawLine(startOffset, 0, startOffset, -gradationLongLen, mPaint);

                // Numerical value
                mTextPaint.setTextSize(gradationTextSize);
                mTextPaint.setColor(gradationTextColor);
                String text = Integer.toString(start);
                float textWidth = mTextPaint.measureText(text);
                canvas.drawText(text, startOffset - textWidth * .5f, -(gradationLongLen + gradationValueGap), mTextPaint);
            } else {
                mPaint.setStrokeWidth(gradationShortWidth);
                canvas.drawLine(startOffset, 0, startOffset, -gradationShortLen, mPaint);
            }

            // balance
            if (start == balance) {
                mPaint.setColor(indicatorColor);
                canvas.drawLine(startOffset, 0, startOffset, -gradationLongLen, mPaint);
                mPaint.setColor(gradationColor);

                mTextPaint.setTextSize(balanceTextSize);
                mTextPaint.setColor(indicatorColor);
//                float textWidth = mTextPaint.measureText(balanceText);
//                canvas.drawText(balanceText, startOffset - textWidth * .5f, balanceGap + balanceTextSize, mTextPaint);
                mTextPaint.setColor(gradationColor);
            }

            start += valueUnit;
            startOffset += unitGap;
        }

        canvas.restore();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) {
                // The end has been reached: slide to the tick mark
                scrollToGradation();
            } else {
                mCurrentDistance = mScroller.getCurrX();
                computeValue();
            }
        }
    }

    /**
     * Draw pointer
     */
    private void drawIndicator(Canvas canvas) {
        mPaint.setColor(indicatorColor);
        canvas.drawLine(mHalfWidth, 0, mHalfWidth, gradationHeight, mPaint);
        mPaint.setColor(gradationColor);
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

    public int getValue() {
        return currentValue;
    }

    /**
     * Settings
     * Note: No callback is required here, otherwise the original data will be changed
     */
//    public void setValue(float value) {
//        // Round down
//        this.currentValue = (int) value / valueUnit * valueUnit;
//        currentValue = Math.min(maxValue, Math.max(0, currentValue));
//        if (!mScroller.isFinished()) {
//            mScroller.forceFinished(true);
//        }
//        if (mListener != null) {
//            mListener.onValueChanged(currentValue);
//        }
//        calculateValues();
//        postInvalidate();
//    }

    public int getBalance() {
        return balanceValue;
    }

    public void setBalance(float balance) {
        this.balanceValue = (int) balance / valueUnit * valueUnit;
        postInvalidate();
    }

    public void setValue(int maxValue, float curValue, int balanceValue, int valueUnit, int valuePerCount) {
        if ( curValue < 0 || curValue > maxValue) {
            throw new IllegalArgumentException(String.format("The given values are invalid, check firstly: " +
                    "minValue=%f, maxValue=%f, curValue=%s", maxValue, curValue));
        }
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }

        this.maxValue = (int) maxValue;
        this.currentValue = (int) curValue;
        this.balanceValue=balanceValue;
        this.valueUnit=valueUnit;
        this.valuePerCount=valuePerCount;
        if (mListener != null) {
            mListener.onValueChanged(currentValue);
        }
        calculateValues();
        postInvalidate();
    }



    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.mListener = listener;
    }
}

//maxValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_maxValue, 150);
//        currentValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_currentValue, 0);
//        balanceValue = ta.getInt(R.styleable.MoneySelectRuleView_msrv_balanceValue, 0);
//        valueUnit = ta.getInt(R.styleable.MoneySelectRuleView_msrv_valueUnit, 1);
//        valuePerCount = ta.getInt(R.styleable.MoneySelectRuleView_msrv_valuePerCount, 10);