package com.uni.dpriho200.mobdev4;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Dwarfius on 11/26/2016.
 */

class LinedEditText extends EditText {
    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        TypedArray typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.LinedEditText);
        mPaint.setColor(typedAttrs.getColor(R.styleable.LinedEditText_underlineColor, 0xFF000000));
        typedAttrs.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // getLineCount() can return 0 if the internal layout wasn't built (like in preview)
        int count = Math.max(getLineCount(), getHeight() / getLineHeight());
        int baseline = getLineBounds(0, mRect);
        for (int i = 0; i < count; i++) {
            canvas.drawLine(mRect.left, baseline + 1, mRect.right, baseline + 1, mPaint);
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}