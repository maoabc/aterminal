package com.github.maoabc.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.appcompat.widget.AppCompatImageView;

public class LongPressRepeatImageView extends AppCompatImageView {

    private int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    private float mLastX;
    private float mLastY;

    public LongPressRepeatImageView(Context context) {
        super(context);
    }

    public LongPressRepeatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongPressRepeatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int mPressedCount;
    private boolean mTouchDown = false;

//    private final Handler mHandler = new Handler();

    private final Runnable mClickRunnable = new Runnable() {
        @Override
        public void run() {
            mPressedCount++;
            performClick();
//            Log.d("LongPress", "run: " + mPressedCount);
            postDelayed(this, ViewConfiguration.getKeyRepeatDelay());
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchDown = true;
                refreshDrawableState();

                mPressedCount = 0;
                mLastX = event.getX();
                mLastY = event.getY();


//                mHandler.postDelayed(mClickRunnable, 400);
                postDelayed(mClickRunnable, ViewConfiguration.getLongPressTimeout());


                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                removeCallbacks(mClickRunnable);
                mTouchDown = false;
                refreshDrawableState();
                if (mPressedCount == 0 && (Math.abs(event.getX() - mLastX) < 20
                        && Math.abs(event.getY() - mLastY) < 20)) {//没有长按，且手指在很小的范围移动，则当成单击事件
                    performClick();
                }

//                mHandler.removeCallbacks(mClickRunnable);
            }
        }
        return true;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (drawableState != null && mTouchDown) {
            mergeDrawableStates(drawableState, PRESSED_STATE_SET);
        }
        return drawableState;
    }

}
