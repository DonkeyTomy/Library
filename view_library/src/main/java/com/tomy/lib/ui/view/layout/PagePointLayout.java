package com.tomy.lib.ui.view.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tomy.lib.ui.R;


/**@author Tomy
 * Created by Tomy on 14-3-3.
 */
public class PagePointLayout extends LinearLayout {
    private Context mContext;
    private int mPrePageIndex = 0;
    public PagePointLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.PagePointLayout);
        int pointCount = types.getInt(R.styleable.PagePointLayout_point_count, 1);
        setPagePoint(pointCount);
        types.recycle();
    }

    public void setPagePoint(int count) {
        if (getChildCount() != 0) {
            removeAllViews();
        }
        int index = Math.abs(getChildCount() - count);
        while ((index--) > 0) {
            ImageView view = new ImageView(mContext);
            view.setImageResource(R.drawable.pagepoint_no_select);
            LayoutParams params = new LayoutParams(12, 12);
            params.leftMargin = params.rightMargin = 5;
            addView(view, getChildCount(), params);
        }
        if (getChildCount() > 0) {
            ((ImageView) getChildAt(0)).setImageResource(R.drawable.pagepoint_selected);
        }
        mPrePageIndex = 0;
    }

    public void setPageIndex(int index) {
        if (index >= getChildCount() || index == mPrePageIndex) {
            return;
        }
        ((ImageView) getChildAt(mPrePageIndex)).setImageResource(R.drawable.pagepoint_no_select);
        ((ImageView) getChildAt(index)).setImageResource(R.drawable.pagepoint_selected);
        mPrePageIndex = index;
    }

}
