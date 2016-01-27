/*
 * Copyright (C) 2016 Frederik Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package link.fls;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;

import java.util.Random;

public class SwipeStack extends ViewGroup {

    public static final int DEFAULT_ANIMATION_DURATION = 300;
    public static final int DEFAULT_STACK_SIZE = 3;
    public static final int DEFAULT_STACK_SPACING = 30;
    public static final int DEFAULT_STACK_ROTATION = 8;
    public static final float DEFAULT_SWIPE_ROTATION = 30;
    public static final float DEFAULT_SWIPE_OPACITY = 1;
    public static final boolean DEFAULT_DISABLE_HW_ACCELERATION = true;

    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_CURRENT_INDEX = "currentIndex";

    private Adapter mAdapter;
    private Random mRandom;

    private int mAnimationDuration;
    private int mCurrentViewIndex;
    private int mNumberOfStackedViews;
    private int mViewSpacing;
    private int mViewRotation;
    private float mSwipeRotation;
    private float mSwipeOpacity;
    private boolean mDisableHwAcceleration;
    private boolean mIsFirstLayout = true;

    private View mTopView;
    private SwipeHelper mSwipeHelper;
    private DataSetObserver mDataObserver;
    private SwipeStackListener mListener;
    private SwipeProgressListener mProgressListener;

    public SwipeStack(Context context) {
        this(context, null);
    }

    public SwipeStack(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeStack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(attrs);
        initialize();
    }

    private void readAttributes(AttributeSet attributeSet) {
        TypedArray attrs = getContext().obtainStyledAttributes(attributeSet, R.styleable.SwipeStack);

        try {
            mAnimationDuration =
                    attrs.getInt(R.styleable.SwipeStack_animation_duration,
                            DEFAULT_ANIMATION_DURATION);
            mNumberOfStackedViews =
                    attrs.getInt(R.styleable.SwipeStack_stack_size, DEFAULT_STACK_SIZE);
            mViewSpacing =
                    attrs.getInt(R.styleable.SwipeStack_stack_spacing, DEFAULT_STACK_SPACING);
            mViewRotation =
                    attrs.getInt(R.styleable.SwipeStack_stack_rotation, DEFAULT_STACK_ROTATION);
            mSwipeRotation =
                    attrs.getFloat(R.styleable.SwipeStack_swipe_rotation, DEFAULT_SWIPE_ROTATION);
            mSwipeOpacity =
                    attrs.getFloat(R.styleable.SwipeStack_swipe_opacity, DEFAULT_SWIPE_OPACITY);
            mDisableHwAcceleration =
                    attrs.getBoolean(R.styleable.SwipeStack_disable_hw_acceleration,
                            DEFAULT_DISABLE_HW_ACCELERATION);
        } finally {
            attrs.recycle();
        }
    }

    private void initialize() {
        mRandom = new Random();

        setClipToPadding(false);
        setClipChildren(false);

        mSwipeHelper = new SwipeHelper(this);
        mSwipeHelper.setAnimationDuration(mAnimationDuration);
        mSwipeHelper.setRotation(mSwipeRotation);
        mSwipeHelper.setOpacityEnd(mSwipeOpacity);

        mDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                invalidate();
                requestLayout();
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_CURRENT_INDEX, mCurrentViewIndex - getChildCount());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentViewIndex = bundle.getInt(KEY_CURRENT_INDEX);
            state = bundle.getParcelable(KEY_SUPER_STATE);
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mAdapter == null || mAdapter.isEmpty()) {
            mCurrentViewIndex = 0;
            removeAllViewsInLayout();
            return;
        }

        for (int x = getChildCount();
             x < mNumberOfStackedViews && mCurrentViewIndex < mAdapter.getCount();
             x++) {
            addNextView();
        }

        reorderItems();
        setupTopView();

        mIsFirstLayout = false;
    }

    private void addNextView() {
        if (mCurrentViewIndex < mAdapter.getCount()) {
            View bottomView = mAdapter.getView(mCurrentViewIndex, null, this);
            bottomView.setTag(R.id.new_view, true);

            if (!mDisableHwAcceleration) {
                bottomView.setLayerType(LAYER_TYPE_HARDWARE, null);
            }

            if (mViewRotation > 0) {
                bottomView.setRotation(mRandom.nextInt(mViewRotation) - (mViewRotation / 2));
            }

            int width = getWidth() - (getPaddingLeft() + getPaddingRight());
            int height = getHeight() - (getPaddingTop() + getPaddingBottom());

            LayoutParams params = bottomView.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
            }

            bottomView.measure(MeasureSpec.EXACTLY | width, MeasureSpec.EXACTLY | height);
            addViewInLayout(bottomView, 0, params, true);

            mCurrentViewIndex++;
        }
    }

    private void reorderItems() {
        int childCount = getChildCount();

        for (int x = 0; x < childCount; x++) {
            View childView = getChildAt(x);

            int childX = (getWidth() - childView.getMeasuredWidth()) / 2;
            childView.layout(
                    childX,
                    getPaddingTop(),
                    childX + childView.getMeasuredWidth(),
                    getPaddingTop() + childView.getMeasuredHeight());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childView.setTranslationZ(x);
            }

            boolean isNewView = (boolean) childView.getTag(R.id.new_view);
            float distanceToViewAbove = ((getChildCount() - 1) * mViewSpacing) - (x * mViewSpacing);
            float newPositionY = distanceToViewAbove + getPaddingTop();

            if (!mIsFirstLayout) {

                if (isNewView) {
                    childView.setTag(R.id.new_view, false);
                    childView.setAlpha(0);
                }

                childView.animate()
                        .y(newPositionY)
                        .alpha(1)
                        .setDuration(mAnimationDuration);

            } else {
                childView.setTag(R.id.new_view, false);
                childView.setY(newPositionY);
            }
        }
    }

    private void setupTopView() {
        mSwipeHelper.unregisterObservedView();
        mTopView = getChildAt(getChildCount() - 1);
        mSwipeHelper.registerObservedView(mTopView);
    }

    private void removeTopView() {
        if (mTopView != null) {
            removeView(mTopView);
            mTopView = null;
        }

        if (getChildCount() == 0) {
            mListener.onStackEmpty();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void onSwipeStart() {
        if (mProgressListener != null) mProgressListener.onSwipeStart(getCurrentPosition());
    }

    public void onSwipeProgress(float progress) {
        if (mProgressListener != null)
            mProgressListener.onSwipeProgress(getCurrentPosition(), progress);
    }

    public void onSwipeEnd() {
        if (mProgressListener != null) mProgressListener.onSwipeEnd(getCurrentPosition());
    }

    public void onViewSwipedToLeft() {
        if (mListener != null) mListener.onViewSwipedToLeft(getCurrentPosition());
        removeTopView();
    }

    public void onViewSwipedToRight() {
        if (mListener != null) mListener.onViewSwipedToRight(getCurrentPosition());
        removeTopView();
    }

    public int getCurrentPosition() {
        return mCurrentViewIndex - getChildCount();
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter pAdapter) {
        if (mAdapter != null) mAdapter.unregisterDataSetObserver(mDataObserver);
        mAdapter = pAdapter;
        mAdapter.registerDataSetObserver(mDataObserver);
    }

    public void setListener(SwipeStackListener listener) {
        mListener = listener;
    }

    public void setSwipeProgressListener(SwipeProgressListener listener) {
        mProgressListener = listener;
    }

    public View getTopView() {
        return mTopView;
    }

    public void swipeTopViewToRight() {
        if (getChildCount() == 0) return;
        mSwipeHelper.swipeViewToRight();
    }

    public void swipeTopViewToLeft() {
        if (getChildCount() == 0) return;
        mSwipeHelper.swipeViewToLeft();
    }

    public void resetStack() {
        mCurrentViewIndex = 0;
        removeAllViewsInLayout();
        requestLayout();
    }

    public interface SwipeStackListener {
        void onViewSwipedToLeft(int position);

        void onViewSwipedToRight(int position);

        void onStackEmpty();
    }

    public interface SwipeProgressListener {
        void onSwipeStart(int position);

        void onSwipeProgress(int position, float progress);

        void onSwipeEnd(int position);
    }
}
