package com.github.maoabc.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.github.maoabc.aterm.R;


public class DividerRelativeLayout extends RelativeLayout {
    private Paint paint;
    private float startX;

    public DividerRelativeLayout(Context context) {
        super(context);
        init(context, null);
    }

    public DividerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DividerRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DividerRelativeLayout);
        boolean b = a.getBoolean(R.styleable.DividerRelativeLayout_dividerEnable, false);
        if (b) {
            paint = new Paint();
            int color = a.getColor(R.styleable.DividerRelativeLayout_dividerColor, Color.LTGRAY);
            paint.setColor(color);
            startX = a.getDimension(R.styleable.DividerRelativeLayout_dividerStart, 0);
        }

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint != null) {
            canvas.drawLine(startX, getHeight() - 1, getWidth(), getHeight() - 1, paint);
        }
    }
}
