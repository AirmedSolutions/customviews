package com.myairmed.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class ThermometerViewDefault extends View {

    private int viewBg; // background color
    private float unitTextSize; // unit text size
    private int unitTextColor; // unit text color
    private float scaleTextSize; // Scale value text size
    private int scaleTextColor; // Scale value text color
    private int maxScaleLineColor; // Long scale color
    private int midScaleLineColor; // Medium scale color
    private int minScaleLineColor; // Short scale color
    private float scaleLineWidth; // Scale line width
    private float maxLineWidth; // Long scale length
    private float midLineWidth; // Medium scale length
    private float minLineWidth; // short scale length
    private float spaceScaleWidth; // The distance between the scale and the thermometer, the distance between the scale and the text
    private int thermometerBg; // thermometer color
    private int thermometerShadowBg; // thermometer shadow color
    private float maxThermometerRadius; // The bottom radius of the thermometer
    private float minThermometerRadius; // The top radius of the thermometer
    private float maxMercuryRadius; // bottom radius of mercury
    private float minMercuryRadius; // Top radius of mercury
    private int leftMercuryBg; // Mercury background color on the left
    private int rightMercuryBg; // Mercury background color on the right
    private int leftMercuryColor; // Mercury color on the left
    private int rightMercuryColor; // Mercury color on the right
    private float maxScaleValue; // The maximum value of the
    private float minScaleValue; // The minimum value of the thermometer
    private float curScaleValue; // current scale value

    private int mWidth; // width
    private float mPaddingTop; // Top content indent

    private float leftTitleX; // X coordinate of left title
    private float rightTitleX; // X coordinate of right title
    private float titleY; // Y coordinate of title
    private float textWidth; // title width
    private float titleHeight; // title height
    private float scaleSpaceHeight; // scale interval
    private float sumScaleValue; // Total scale number
    private float thermometerTopX; // X coordinate of the center of the thermometer top
    private float thermometerTopY; // Y coordinate of the center of the thermometer top
    private float thermometerBottomX; // bottom of thermometer\ bottom of mercury X coordinate of circle center
    private float thermometerBottomY; // bottom of thermometer\ bottom of mercury Y coordinate of circle center
    private RectF thermometerRectF; // Thermometer cylinder area
    private RectF mercuryRectF; // The top area of ​​mercury, since the bottom is completely filled, a circle is drawn directly at the bottom
    private float leftMercuryLeft; // Left mercury left, equivalent to RectF.left
    private float leftMercuryRight; // Left Mercury right, equivalent to RectF.right
    private float rightMercuryLeft; // Right mercury left, equivalent to RectF.left
    private float rightMercuryRight; // Right Mercury right, equivalent to RectF.right
    private float mercuryTop; // Mercury top, equivalent to RectF.top
    private float mercuryBottom; // Mercury bottom, equivalent to RectF.bottom
    private float leftWaveLeft; // Left grows mercury left, which is equivalent to RectF.left
    private float leftWaveRight; // Left to grow mercury right, equivalent to RectF.right
    private float rightWaveLeft; // Right growth of mercury left, equivalent to RectF.left
    private float rightWaveRight; // Right grows mercury right, which is equivalent to RectF.right
    private float waveBottom; // Increase mercury bottom, equivalent to RectF.bottom

    private TextPaint mTextPaint;
    private Paint mLinePaint;

    private Paint mPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    public ThermometerViewDefault(Context context) {
        super(context);
        init(null);
    }

    public ThermometerViewDefault(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ThermometerViewDefault(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ThermometerViewDefault(Context context, ThermometerBuilder builder) {
        super(context);
        this.viewBg = builder.viewBg;
        this.unitTextSize = builder.unitTextSize;
        this.unitTextColor = builder.unitTextColor;
        this.scaleTextSize = builder.scaleTextSize;
        this.scaleTextColor = builder.scaleTextColor;
        this.maxScaleLineColor = builder.maxScaleLineColor;
        this.midScaleLineColor = builder.midScaleLineColor;
        this.minScaleLineColor = builder.minScaleLineColor;
        this.scaleLineWidth = builder.scaleLineWidth;
        this.maxLineWidth = builder.maxLineWidth;
        this.midLineWidth = builder.midLineWidth;
        this.minLineWidth = builder.minLineWidth;
        this.spaceScaleWidth = builder.spaceScaleWidth;
        this.thermometerBg = builder.thermometerBg;
        this.thermometerShadowBg = builder.thermometerShadowBg;
        this.maxThermometerRadius = builder.maxThermometerRadius;
        this.minThermometerRadius = builder.minThermometerRadius;
        this.maxMercuryRadius = builder.maxMercuryRadius;
        this.minMercuryRadius = builder.minMercuryRadius;
        this.leftMercuryBg = builder.leftMercuryBg;
        this.rightMercuryBg = builder.rightMercuryBg;
        this.leftMercuryColor = builder.leftMercuryColor;
        this.rightMercuryColor = builder.rightMercuryColor;
        this.maxScaleValue = builder.maxScaleValue;
        this.minScaleValue = builder.minScaleValue;
        this.curScaleValue = builder.curScaleValue;

        initConfig();
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThermometerView);
        viewBg = typedArray.getColor(R.styleable.ThermometerView_viewBg, Color.parseColor("#F5F5F5"));
        unitTextSize = typedArray.getDimension(R.styleable.ThermometerView_unitTextSize, 36f);
        unitTextColor = typedArray.getColor(R.styleable.ThermometerView_unitTextColor, Color.parseColor("#787878"));
        scaleTextSize = typedArray.getDimension(R.styleable.ThermometerView_scaleTextSize, 26f);
        scaleTextColor = typedArray.getColor(R.styleable.ThermometerView_scaleTextColor, Color.parseColor("#464646"));
        maxScaleLineColor = typedArray.getColor(R.styleable.ThermometerView_maxScaleLineColor, Color.parseColor("#787878"));
        midScaleLineColor = typedArray.getColor(R.styleable.ThermometerView_midScaleLineColor, Color.parseColor("#A9A9A9"));
        minScaleLineColor = typedArray.getColor(R.styleable.ThermometerView_minScaleLineColor, Color.parseColor("#A9A9A9"));
        scaleLineWidth = typedArray.getFloat(R.styleable.ThermometerView_scaleLineWidth, 1.5f);
        maxLineWidth = typedArray.getFloat(R.styleable.ThermometerView_maxLineWidth, 70f);
        midLineWidth = typedArray.getFloat(R.styleable.ThermometerView_midLineWidth, 50f);
        minLineWidth = typedArray.getFloat(R.styleable.ThermometerView_minLineWidth, 40f);
        spaceScaleWidth = typedArray.getFloat(R.styleable.ThermometerView_spaceScaleWidth, 30f);
        thermometerBg = typedArray.getColor(R.styleable.ThermometerView_thermometerBg, Color.WHITE);
        thermometerShadowBg = typedArray.getColor(R.styleable.ThermometerView_thermometerShadowBg, Color.parseColor("#F0F0F0"));
        maxThermometerRadius = typedArray.getFloat(R.styleable.ThermometerView_maxThermometerRadius, 80f);
        minThermometerRadius = typedArray.getFloat(R.styleable.ThermometerView_minThermometerRadius, 40f);
        maxMercuryRadius = typedArray.getFloat(R.styleable.ThermometerView_maxMercuryRadius, 60f);
        minMercuryRadius = typedArray.getFloat(R.styleable.ThermometerView_minMercuryRadius, 20f);
        leftMercuryBg = typedArray.getColor(R.styleable.ThermometerView_leftMercuryBg, Color.parseColor("#FFE6E0"));
        rightMercuryBg = typedArray.getColor(R.styleable.ThermometerView_rightMercuryBg, Color.parseColor("#FDE1DE"));
        leftMercuryColor = typedArray.getColor(R.styleable.ThermometerView_leftMercuryColor, Color.parseColor("#FF8063"));
        rightMercuryColor = typedArray.getColor(R.styleable.ThermometerView_rightMercuryColor, Color.parseColor("#F66A5C"));
        maxScaleValue = typedArray.getFloat(R.styleable.ThermometerView_maxScaleValue, 42f);
        minScaleValue = typedArray.getFloat(R.styleable.ThermometerView_minScaleValue, 35f);
        curScaleValue = typedArray.getFloat(R.styleable.ThermometerView_curScaleValue, 35f);
        typedArray.recycle();

        initConfig();
    }

    public void initConfig() {
        if (minThermometerRadius >= maxThermometerRadius) {
            throw new UnsupportedOperationException("The thermometer shape is set incorrectly.");
        }
        if (minMercuryRadius >= maxMercuryRadius || minMercuryRadius >= minThermometerRadius) {
            throw new UnsupportedOperationException("The mercury shape is set incorrectly.");
        }
        if (minScaleValue >= maxScaleValue) {
            throw new UnsupportedOperationException("The scale value is not set correctly.");
        }
        setResetCurValue(curScaleValue);

        sumScaleValue = (maxScaleValue - minScaleValue) * 10;

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(scaleLineWidth);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        String title = "℃";
        mTextPaint.setTextSize(unitTextSize);
        textWidth = Layout.getDesiredWidth(title, mTextPaint);
        Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
        titleHeight = -(float) (fmi.bottom + fmi.top);

        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Turn off hardware acceleration, otherwise the shadow will be invalid
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = this.getWidth();
        int mHeight = this.getHeight();
        mPaddingTop = getPaddingTop();
        float mPaddingBottom = getPaddingBottom();

        leftTitleX = mWidth / 2 - spaceScaleWidth - minThermometerRadius - maxLineWidth / 2 - textWidth / 2;
        rightTitleX = mWidth / 2 + spaceScaleWidth + minThermometerRadius + maxLineWidth / 2 - textWidth / 2;
        titleY = mPaddingTop + titleHeight;

        float mercuryHeight = mHeight - titleHeight - mPaddingTop - mPaddingBottom - minThermometerRadius - 2 * maxThermometerRadius;

        scaleSpaceHeight = mercuryHeight / sumScaleValue;

        thermometerTopX = thermometerBottomX = mWidth / 2;
        thermometerTopY = mPaddingTop + titleHeight + minThermometerRadius;
        thermometerBottomY = mHeight - mPaddingBottom - maxThermometerRadius;

        thermometerRectF = new RectF();
        thermometerRectF.left = mWidth / 2 - minThermometerRadius;
        thermometerRectF.top = mPaddingTop + titleHeight + minThermometerRadius;
        thermometerRectF.right = mWidth / 2 + minThermometerRadius;
        thermometerRectF.bottom = mHeight - mPaddingBottom - maxThermometerRadius;

        mercuryRectF = new RectF();
        mercuryRectF.left = mWidth / 2 - minMercuryRadius;
        mercuryRectF.top = mPaddingTop + titleHeight + minThermometerRadius;
        mercuryRectF.right = mWidth / 2 + minMercuryRadius;
        mercuryRectF.bottom = mPaddingTop + titleHeight + minThermometerRadius + 2 * minMercuryRadius;

        leftMercuryLeft = mWidth / 2 - minMercuryRadius;
        leftMercuryRight = rightMercuryLeft = mWidth / 2;
        rightMercuryRight = mWidth / 2 + minMercuryRadius;

        mercuryTop = mPaddingTop + titleHeight + minThermometerRadius + minMercuryRadius;
        mercuryBottom = mHeight - mPaddingBottom - maxThermometerRadius;

        leftWaveLeft = mWidth / 2 - maxMercuryRadius;
        leftWaveRight = rightWaveLeft = mWidth / 2;
        rightWaveRight = mWidth / 2 + maxMercuryRadius;

        waveBottom = mHeight - mPaddingBottom - (maxThermometerRadius - maxMercuryRadius);

        // Create a Bitmap yourself
        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (curScaleValue < minScaleValue || curScaleValue > maxScaleValue) {
            return;
        }

        canvas.drawColor(viewBg);

        drawScaleTitleText(canvas);
        drawScaleText(canvas);

        drawShapeBg(mPaint, canvas);
        drawShape(mPaint, bitmapCanvas);
        drawWaveShape(mPaint, bitmapCanvas);

        canvas.drawBitmap(bitmap, 0, 0, mPaint);
    }

    /**
     * Draw unit text
     */
    private void drawScaleTitleText(Canvas canvas) {
        mTextPaint.setColor(unitTextColor);
        mTextPaint.setTextSize(unitTextSize);
        canvas.drawText("℉", leftTitleX, titleY, mTextPaint);

        canvas.drawText("℃", rightTitleX, titleY, mTextPaint);
    }

    /**
     * Draw scale and text
     */
    private void drawScaleText(Canvas canvas) {
        /* Draw the scale and text on the left */
        for (int i = 0; i <= sumScaleValue; i++) {
            if (i % 10 == 0) {
                double curValue = (maxScaleValue - i / 10) * 1.8 + 32; // Convert Celsius to Fahrenheit
                String curValueStr = String.format("%.1f", curValue);

                mTextPaint.setColor(scaleTextColor);
                mTextPaint.setTextSize(scaleTextSize);
                float textWidth = Layout.getDesiredWidth(curValueStr, mTextPaint);
                Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
                float baselineY = -(float) (fmi.bottom + fmi.top);
                canvas.drawText(curValueStr,
                        mWidth / 2 - minThermometerRadius - 2 * spaceScaleWidth - maxLineWidth - textWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i + baselineY / 2, mTextPaint);

                mLinePaint.setColor(maxScaleLineColor);
                canvas.drawLine(mWidth / 2 - spaceScaleWidth - minThermometerRadius - maxLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 - spaceScaleWidth - minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            } else if (i % 5 == 0) {
                mLinePaint.setColor(midScaleLineColor);

                canvas.drawLine(mWidth / 2 - spaceScaleWidth - minThermometerRadius - midLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 - spaceScaleWidth - minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            } else {
                mLinePaint.setColor(minScaleLineColor);

                canvas.drawLine(mWidth / 2 - spaceScaleWidth - minThermometerRadius - minLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 - spaceScaleWidth - minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            }
        }

        /* Draw the scale and text on the right */
        for (int i = 0; i <= sumScaleValue; i++) {
            if (i % 10 == 0) {
                float curValue = maxScaleValue - i / 10;
                String curValueStr = String.format("%.0f", curValue);

                mTextPaint.setColor(scaleTextColor);
                mTextPaint.setTextSize(scaleTextSize);
                Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
                float baselineY = -(float) (fmi.bottom + fmi.top);
                canvas.drawText(curValueStr,
                        mWidth / 2 + minThermometerRadius + 2 * spaceScaleWidth + maxLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i + baselineY / 2,
                        mTextPaint);

                mLinePaint.setColor(maxScaleLineColor);
                canvas.drawLine(mWidth / 2 + spaceScaleWidth + minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 + spaceScaleWidth + minThermometerRadius + maxLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            } else if (i % 5 == 0) {
                mLinePaint.setColor(midScaleLineColor);

                canvas.drawLine(mWidth / 2 + spaceScaleWidth + minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 + spaceScaleWidth + minThermometerRadius + midLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            } else {
                mLinePaint.setColor(minScaleLineColor);

                canvas.drawLine(mWidth / 2 + spaceScaleWidth + minThermometerRadius,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i,
                        mWidth / 2 + spaceScaleWidth + minThermometerRadius + minLineWidth,
                        mPaddingTop + titleHeight + minThermometerRadius + scaleSpaceHeight * i, mLinePaint);
            }
        }
    }

    /**
     * Draw the shape of the thermometer
     *
     * @param shapePaint Paint
     * @param canvas     Canvas
     */
    private void drawShapeBg(Paint shapePaint, Canvas canvas) {
        shapePaint.setColor(thermometerBg);
        shapePaint.setShadowLayer(8, 0, 0, thermometerShadowBg);

        canvas.drawCircle(thermometerTopX, thermometerTopY, minThermometerRadius, shapePaint);
        canvas.drawCircle(thermometerBottomX, thermometerBottomY, maxThermometerRadius, shapePaint);
        canvas.drawRect(thermometerRectF, shapePaint);

        /* The following three sentences are to remove some unwanted shadows */
        shapePaint.clearShadowLayer();
        canvas.drawCircle(thermometerTopX, thermometerTopY, minThermometerRadius, shapePaint);
        canvas.drawCircle(thermometerBottomX, thermometerBottomY, maxThermometerRadius, shapePaint);
    }

    /**
     * Draw the mercury shape
     * Note: Must be combined with {@link #drawWaveShape(Paint, Canvas)} method
     *
     * @param shapePaint Paint
     * @param canvas     Canvas
     */
    private void drawShape(Paint shapePaint, Canvas canvas) {
        shapePaint.clearShadowLayer();

        shapePaint.setColor(leftMercuryBg);
        canvas.drawArc(mercuryRectF, 90, 180, true, shapePaint);

        canvas.drawRect(leftMercuryLeft, mercuryTop, leftMercuryRight, mercuryBottom, shapePaint);

        shapePaint.setColor(rightMercuryBg);
        canvas.drawArc(mercuryRectF, -90, 180, true, shapePaint);

        canvas.drawRect(rightMercuryLeft, mercuryTop, rightMercuryRight, mercuryBottom, shapePaint);

        canvas.drawCircle(thermometerBottomX, thermometerBottomY, maxMercuryRadius, shapePaint);

    }

    /**
     * Draw thermometer mercury height
     * Note: Must be combined with {@link #drawShape(Paint, Canvas)} method
     *
     * @param shapePaint Paint
     * @param canvas     Canvas
     */
    private void drawWaveShape(Paint shapePaint, Canvas canvas) {
        float waveTop = mPaddingTop + titleHeight + minThermometerRadius
                + (maxScaleValue - curScaleValue) * 10 * scaleSpaceHeight;

        shapePaint.setColor(leftMercuryColor);
        shapePaint.clearShadowLayer();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        canvas.drawRect(leftWaveLeft, waveTop, leftWaveRight, waveBottom, shapePaint);

        shapePaint.setColor(rightMercuryColor);
        shapePaint.clearShadowLayer();

        canvas.drawRect(rightWaveLeft, waveTop, rightWaveRight, waveBottom, shapePaint);
    }

    /**
     * Set temperature value
     *
     * @param curValue The current temperature value float(℃)
     */
    public void setCurValue(float curValue) {
        setResetCurValue(curValue);
        invalidate();
    }

    public float getCurValue() {

        return this.curScaleValue;
    }

    /**
     * Set temperature value
     *
     * @param curFValue current temperature value float(℉)
     */
    public void setCurFValue(float curFValue) {
        String curValueStr = String.format("%.0f", (curFValue - 32) / 1.8);
        setResetCurValue(Float.valueOf(curValueStr));
        invalidate();
    }

    /**
     * Reset (calibrate) temperature value
     *
     * @param curValue The current temperature value float(℃)
     */
    private void setResetCurValue(float curValue) {
        if (curValue < minScaleValue) {
            curValue = minScaleValue;
        }
        if (curValue > maxScaleValue) {
            curValue = maxScaleValue;
        }
        this.curScaleValue = curValue;
    }

    /**
     * Set the temperature value and start the animation (Interpolator: LinearInterpolator)
     *
     * @param newFValue new temperature value float(℉)
     */
    public void setFValueAndStartAnim(float newFValue) {
        String curValueStr = String.format("%.0f", (newFValue - 32) / 1.8);
        setValueAndStartAnim(Float.valueOf(curValueStr));
    }

    /**
     * Set the temperature value and start the animation (Interpolator: LinearInterpolator)
     *
     * @param newValue new temperature value float(℃)
     */
    public void setValueAndStartAnim(float newValue) {
        if (newValue < minScaleValue) {
            newValue = minScaleValue;
        }
        if (newValue > maxScaleValue) {
            newValue = maxScaleValue;
        }

        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(this, "curValue", curScaleValue, newValue);
        waveShiftAnim.setRepeatCount(0);
        waveShiftAnim.setDuration(500);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        waveShiftAnim.start();
    }

    /**
     * Constructor
     */
    public static class ThermometerBuilder {

        private final Context context;
        private int viewBg = Color.parseColor("#F5F5F5"); // background color
        private float unitTextSize = 36f; // Unit text size
        private int unitTextColor = Color.parseColor("#787878"); // Unit text color
        private float scaleTextSize = 26f; // Scale value text size
        private int scaleTextColor = Color.parseColor("#464646"); // Scale value text color
        private int maxScaleLineColor = Color.parseColor("#787878"); // Long scale color
        private int midScaleLineColor = Color.parseColor("#A9A9A9"); // Medium scale color
        private int minScaleLineColor = Color.parseColor("#A9A9A9"); // Short scale color
        private float scaleLineWidth = 1.5f; // scale line width
        private float maxLineWidth = 70f; // Long scale length
        private float midLineWidth = 50f; // Medium scale length
        private float minLineWidth = 40f; // short scale length
        private float spaceScaleWidth = 30f; // The distance between the scale and the thermometer, the distance between the scale and the text
        private int thermometerBg = Color.WHITE; // Thermometer color
        private int thermometerShadowBg = Color.parseColor("#F0F0F0"); // thermometer shadow color
        private float maxThermometerRadius = 80f; // The bottom radius of the thermometer
        private float minThermometerRadius = 40f; // The top radius of the thermometer
        private float maxMercuryRadius = 60f; // Mercury bottom radius
        private float minMercuryRadius = 20f; // Top radius of mercury
        private int leftMercuryBg = Color.parseColor("#FFE6E0"); // Mercury background color on the left
        private int rightMercuryBg = Color.parseColor("#FDE1DE"); // Mercury background color on the right
        private int leftMercuryColor = Color.parseColor("#FF8063"); // Mercury color on the left
        private int rightMercuryColor = Color.parseColor("#F66A5C"); // Mercury color on the right
        private float maxScaleValue = 42f; // The maximum value of the thermometer
        private float minScaleValue = 35f; // The minimum value of the thermometer
        private float curScaleValue = 35f; // current scale value

        public ThermometerBuilder(Context context) {
            this.context = context;
        }

        public ThermometerBuilder setViewBg(int viewBg) {
            this.viewBg = viewBg;
            return this;
        }

        public ThermometerBuilder setUnitTextSize(float unitTextSize) {
            this.unitTextSize = unitTextSize;
            return this;
        }

        public ThermometerBuilder setUnitTextColor(int unitTextColor) {
            this.unitTextColor = unitTextColor;
            return this;
        }

        public ThermometerBuilder setScaleTextSize(float scaleTextSize) {
            this.scaleTextSize = scaleTextSize;
            return this;
        }

        public ThermometerBuilder setScaleTextColor(int scaleTextColor) {
            this.scaleTextColor = scaleTextColor;
            return this;
        }

        public ThermometerBuilder setMaxScaleLineColor(int maxScaleLineColor) {
            this.maxScaleLineColor = maxScaleLineColor;
            return this;
        }

        public ThermometerBuilder setMidScaleLineColor(int midScaleLineColor) {
            this.midScaleLineColor = midScaleLineColor;
            return this;
        }

        public ThermometerBuilder setMinScaleLineColor(int minScaleLineColor) {
            this.minScaleLineColor = minScaleLineColor;
            return this;
        }

        public ThermometerBuilder setScaleLineWidth(float scaleLineWidth) {
            this.scaleLineWidth = scaleLineWidth;
            return this;
        }

        public ThermometerBuilder setMaxLineWidth(float maxLineWidth) {
            this.maxLineWidth = maxLineWidth;
            return this;
        }

        public ThermometerBuilder setMidLineWidth(float midLineWidth) {
            this.midLineWidth = midLineWidth;
            return this;
        }

        public ThermometerBuilder setMinLineWidth(float minLineWidth) {
            this.minLineWidth = minLineWidth;
            return this;
        }

        public ThermometerBuilder setSpaceScaleWidth(float spaceScaleWidth) {
            this.spaceScaleWidth = spaceScaleWidth;
            return this;
        }

        public ThermometerBuilder setThermometerBg(int thermometerBg) {
            this.thermometerBg = thermometerBg;
            return this;
        }

        public ThermometerBuilder setThermometerShadowBg(int thermometerShadowBg) {
            this.thermometerShadowBg = thermometerShadowBg;
            return this;
        }

        public ThermometerBuilder setMaxThermometerRadius(float maxThermometerRadius) {
            this.maxThermometerRadius = maxThermometerRadius;
            return this;
        }

        public ThermometerBuilder setMinThermometerRadius(float minThermometerRadius) {
            this.minThermometerRadius = minThermometerRadius;
            return this;
        }

        public ThermometerBuilder setMaxMercuryRadius(float maxMercuryRadius) {
            this.maxMercuryRadius = maxMercuryRadius;
            return this;
        }

        public ThermometerBuilder setMinMercuryRadius(float minMercuryRadius) {
            this.minMercuryRadius = minMercuryRadius;
            return this;
        }

        public ThermometerBuilder setLeftMercuryBg(int leftMercuryBg) {
            this.leftMercuryBg = leftMercuryBg;
            return this;
        }

        public ThermometerBuilder setRightMercuryBg(int rightMercuryBg) {
            this.rightMercuryBg = rightMercuryBg;
            return this;
        }

        public ThermometerBuilder setLeftMercuryColor(int leftMercuryColor) {
            this.leftMercuryColor = leftMercuryColor;
            return this;
        }

        public ThermometerBuilder setRightMercuryColor(int rightMercuryColor) {
            this.rightMercuryColor = rightMercuryColor;
            return this;
        }

        public ThermometerBuilder setMaxScaleValue(float maxScaleValue) {
            this.maxScaleValue = maxScaleValue;
            return this;
        }

        public ThermometerBuilder setMinScaleValue(float minScaleValue) {
            this.minScaleValue = minScaleValue;
            return this;
        }

        public ThermometerBuilder setCurScaleValue(float curScaleValue) {
            this.curScaleValue = curScaleValue;
            return this;
        }

        public ThermometerViewDefault builder() {
            return new ThermometerViewDefault(context, this);
        }

    }

}
