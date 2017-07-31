package cn.hackill.bong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tuesda.walker.circlerefresh.R;

import java.util.Random;

/**
 * @author hackill
 * @date on 17/7/25 17:07
 */

public class CycProgressBar extends View {

    private static final String TAG = "CycProgressBar";

    public CycProgressBar(Context context) {
        this(context, null);
    }

    public CycProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context);
    }

    private Paint mProgressBgPaint = new Paint();
    private final RectF mCircleBounds = new RectF();
    private Paint mProgressPaint;
    private Paint mPointPaint;
    private int mCircleStrokeWidth = 20;

    private float progress = 85;
    private float mRadius;
    private float mSize;

    private final RectF mThumbTop = new RectF(), mThumbBottom = new RectF();
    private final RectF mRectFOne = new RectF(), mRectFTwo = new RectF(), mRectFThree = new RectF(), mRectFFour = new RectF();
    private Paint mContentPaint;
    private Paint mTestPaint;

    private Style mStyle = Style.Thumbnail;
    private Content mContent = new Content();
    private Context mContext;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(
                getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom(),
                heightMeasureSpec);
        final int width = getDefaultSize(
                getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(),
                widthMeasureSpec);

        int size = Math.min(height, width);

        mSize = size;

        setMeasuredDimension(size, size);

        initMeasure(size);
    }

    private void initControl(Context context) {
        this.mContext = context;
        initPaint();
        mContent.value = "52.7";
        mContent.unit = "公斤";

        initMeasure(20);
    }

    public void setContent(Content content, Style style) {
        this.mContent = content;
        this.mStyle = style;
        invalidate();
    }

    private void initMeasure(int viewSize) {


        mCircleStrokeWidth = (int) (viewSize * 0.06);

        mRadius = viewSize / 2 - mCircleStrokeWidth / 2 - 1.5f;

        mCircleBounds.set(-mRadius, -mRadius, mRadius, mRadius);

        mProgressBgPaint.setStrokeWidth(mCircleStrokeWidth);

        mProgressPaint.setStrokeWidth(mCircleStrokeWidth);

        mThumbTop.set(
                mSize * 0.1f,
                mSize * 0.15f,
                mSize * 0.90f,
                mSize * 0.75f);

        mThumbBottom.set(
                mSize * 0.25f,
                mSize * 0.55f,
                mSize * 0.75f,
                mSize * 0.9f);

        mRectFOne.set(
                mSize * 0.3f,
                mSize * 0.08f,
                mSize * 0.7f,
                mSize * 0.22f);

        mRectFTwo.set(
                mSize * 0.15f,
                mSize * 0.2f,
                mSize * 0.85f,
                mSize * 0.48f);

        mRectFThree.set(
                mSize * 0.12f,
                mSize * 0.35f,
                mSize * 0.88f,
                mSize * 0.76f);

        mRectFFour.set(
                mSize * 0.3f,
                mSize * 0.72f,
                mSize * 0.7f,
                mSize * 0.92f);

    }


    private void initPaint() {

        mProgressBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressBgPaint.setColor(Color.parseColor("#66FFFFFF"));
        mProgressBgPaint.setStyle(Paint.Style.STROKE);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(Color.parseColor("#FFFFFF"));
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.parseColor("#6039BC"));
        mPointPaint.setStyle(Paint.Style.FILL);

        mContentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mContentPaint.setColor(Color.WHITE);

        mTestPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTestPaint.setColor(Color.parseColor("#609540F6"));
        mTestPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw: ..... progress = " + progress);
        drawCircleRenderer(canvas);
        drawContentRenderer(canvas);

    }

    private void drawCircleRenderer(Canvas canvas) {

        canvas.translate(mSize / 2, mSize / 2);

        canvas.drawArc(mCircleBounds, 0, 360, false, mProgressBgPaint);

        float angle = progress * 3.6f;

        canvas.drawArc(mCircleBounds, -90, angle, false, mProgressPaint);

        float piAngle = (float) ((angle - 90) / 180 * Math.PI);

        if (progress <= 99 && progress > 0) {
            canvas.drawCircle(mRadius * (float) Math.cos(piAngle), mRadius * (float) Math.sin(piAngle), mCircleStrokeWidth / 4, mPointPaint);
        }
        canvas.translate(-mSize / 2, -mSize / 2);
    }

    private void drawContentRenderer(Canvas canvas) {

        if (mStyle == Style.Thumbnail) {

            mContentPaint = measurePaint(mContent.value, mThumbTop.width(), mContentPaint, mStyle.getValueSize(mSize));
            mContentPaint.setFakeBoldText(true);
            drawTextRenderer(canvas, mContent.value, mThumbTop, mContentPaint);

            mContentPaint.setFakeBoldText(false);
            mContentPaint = measurePaint(mContent.unit, mThumbBottom.width(), mContentPaint, mStyle.getUnitSize(mSize));
            drawTextRenderer(canvas, mContent.unit, mThumbBottom, mContentPaint);
        } else {
            Bitmap bitmap = BitmapUtil.drawableToBitmap(getResources().getDrawable(mContent.imgRes));

            drawBitmapRenderer(canvas, bitmap, mRectFOne);

            if (mStyle == Style.Sport || mStyle == Style.Weight) {

                drawTextRenderer(canvas,
                        mContent.addition,
                        mStyle.getAdditionValueSize(mSize),
                        mContent.additionUnit,
                        mStyle.getUnitSize(mSize),
                        mRectFTwo,
                        mContentPaint);
            } else {
                drawStarRenderer(canvas, 7, mStyle.getUnitSize(mSize), mRectFTwo);
            }

            drawTextRenderer(canvas,
                    mContent.value,
                    mStyle.getValueSize(mSize),
                    mContent.unit,
                    mStyle.getUnitSize(mSize),
                    mRectFThree,
                    mContentPaint);

            bitmap = BitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.btn_detail));

            drawBitmapRenderer(canvas, bitmap, mRectFFour);
        }
    }

    /**
     * drawText on RectF
     *
     * @param canvas
     * @param text
     * @param targetRectF
     * @param paint
     */
    private void drawTextRenderer(Canvas canvas, String text, RectF targetRectF, Paint paint) {
        if (canvas == null || paint == null || TextUtils.isEmpty(text) || targetRectF == null)
            return;

        float startX = targetRectF.centerX();

        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        float baseLine = (targetRectF.bottom + targetRectF.top - fontMetrics.bottom - fontMetrics.top) / 2;

        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, startX, baseLine, paint);

//        mTestPaint.setAlpha(50 + new Random().nextInt(150));
//        canvas.drawRect(targetRectF, mTestPaint);
    }

    private void drawTextRenderer(Canvas canvas, String text, float textSize, String unit, float unitSize, RectF targetRectF, Paint paint) {
        if (canvas == null || TextUtils.isEmpty(text) || TextUtils.isEmpty(unit) || targetRectF == null)
            return;

        paint.setFakeBoldText(false);
        // draw first
        paint.setTextSize(unitSize);
        float unitWidth = paint.measureText(unit);

        // find startPos
        float startX = targetRectF.centerX() - unitWidth / 2;

        paint = measurePaint(text, targetRectF.width() - unitWidth, paint, textSize);

        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        float baseLine = (targetRectF.bottom + targetRectF.top - fontMetrics.bottom - fontMetrics.top) / 2;

        paint.setTextAlign(Paint.Align.CENTER);

        paint.setFakeBoldText(true);
        canvas.drawText(text, startX, baseLine, paint);


        //draw second
        float textWidth = paint.measureText(text);
        startX = startX + textWidth / 2 + unitWidth / 2;

        paint.setFakeBoldText(false);
        paint.setTextSize(unitSize);
        canvas.drawText(unit, startX, baseLine - 8, paint);

//        mTestPaint.setAlpha(50 + new Random().nextInt(150));
//        canvas.drawRect(targetRectF, mTestPaint);
    }

    private void drawStarRenderer(Canvas canvas, int count, float unitSize, RectF targetRectF) {

        String value = "我";


        mContentPaint.setTextSize(unitSize);

        mContentPaint.setTextAlign(Paint.Align.LEFT);
        float width = mContentPaint.measureText(value);

        float startX = targetRectF.centerX() - width * 2.5f;

        canvas.drawText(value, startX, targetRectF.centerY() + width, mContentPaint);
        startX = targetRectF.centerX() - width * 0.5f;
        canvas.drawText(value, startX, targetRectF.centerY() + width, mContentPaint);
        startX = targetRectF.centerX() + width * 1.5f;
        canvas.drawText(value, startX, targetRectF.centerY() + width, mContentPaint);
        startX = targetRectF.centerX() - width * 1.5f;
        canvas.drawText(value, startX, targetRectF.centerY() - width / 2, mContentPaint);
        startX = targetRectF.centerX() + width * 0.5f;
        canvas.drawText(value, startX, targetRectF.centerY() - width / 2, mContentPaint);

//        mTestPaint.setAlpha(50 + new Random().nextInt(150));
//        canvas.drawRect(targetRectF, mTestPaint);
    }

    private void drawBitmapRenderer(Canvas canvas, Bitmap bitmap, RectF targetRectF) {

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, targetRectF.centerX() - bitmap.getWidth() / 2, targetRectF.centerY() - bitmap.getHeight() / 2, new Paint());
        }

//        mTestPaint.setAlpha(50 + new Random().nextInt(150));
//        canvas.drawRect(targetRectF, mTestPaint);
    }

    private Paint measurePaint(String text, float width, Paint paint, float defaultSize) {

        paint.setTextSize(defaultSize);
        float cuLength = paint.measureText(text);

        while (cuLength > width && defaultSize > 10) {
            defaultSize = defaultSize - 2;
            paint.setTextSize(defaultSize);
            cuLength = paint.measureText(text);
            Log.d(TAG, "measurePaint: de " + defaultSize + ", wi = " + cuLength + ", width = " + width);
        }
        Log.i(TAG, "measurePaint: text = " + text + ", width =" + width + ", currentLength = " + cuLength + ", size = " + defaultSize);
        return paint;
    }


    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public float getProgress() {
        return this.progress;
    }

    public enum Style {
        Thumbnail,
        Sleep,
        Sport,
        Weight;

        public float getValueSize(float width) {
            switch (this) {
                case Thumbnail:
                    return width * 0.35f;
                case Sleep:
                case Sport:
                case Weight:
                    return width * 0.28f;
            }
            return width * 0.1f;
        }

        public float getAdditionValueSize(float width) {
            switch (this) {
                case Thumbnail:
                    return width * 0.2f;
                case Sleep:
                case Sport:
                case Weight:
                    return width * 0.12f;
            }
            return width * 0.12f;
        }

        public float getUnitSize(float width) {
            switch (this) {
                case Thumbnail:
                    return width * 0.13f;
                case Sleep:
                case Sport:
                case Weight:
                    return width * 0.05f;
            }
            return width * 0.13f;
        }

    }


}


