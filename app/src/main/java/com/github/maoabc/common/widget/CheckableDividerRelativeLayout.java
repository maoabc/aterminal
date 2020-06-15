
package com.github.maoabc.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;

public class CheckableDividerRelativeLayout extends DividerRelativeLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private boolean mChecked = false;

    public CheckableDividerRelativeLayout(Context context) {
        super(context);
    }

    public CheckableDividerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableDividerRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean b) {
        if (b != mChecked) {
            mChecked = b;
            refreshDrawableState();
        }
    }


    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (drawableState != null && isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
