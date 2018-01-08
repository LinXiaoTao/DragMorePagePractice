package top.wefor.dragmorepagepractice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;


/**
 * 拖动查看更多 Behavior
 * 依赖的 View id 为 {@link R.id#start_page}
 * Created on 2017/11/15 上午10:29.
 * leo linxiaotao1993@vip.qq.com
 */
public class DragLoadMoreBehavior extends CoordinatorLayout.Behavior<View> {

    private static final boolean DEBUG = true;

    private CallBack mCallBack;

    private View mDependencyView;
    private ViewOffsetHelper mDependencyViewHelper;
    private ViewOffsetHelper mChildViewHelper;
    private boolean mIsReady = false;
    private View mChildView;
    /**
     * 是否开始嵌套滚动
     */
    private boolean mIsBeginNestedScroll = false;
    /**
     * 是否开始拖动
     */
    private boolean mIsBeingDragged = false;
    /**
     * 显示和还原更多页面的临界值
     */
    private int mDragThreshold;
    /**
     * 当更多页面显示时，最大 Top
     */
    private int mMaxCanScrollTop;
    private AppBarLayout mAppBarLayout;
    private int mShowMaxOffset, mShowMinOffset;
    private int mHideMaxOffset, mHideMinOffset;
    private int mTouchSlop = -1;
    private int mActivePointerId = INVALID_POINTER;
    private int mLastMotionY;
    private int mLastMotionX;
    private ValueAnimator mOffsetAnimator;
    /**
     * 当前是否显示 more page
     */
    private boolean mShowMorePage = false;
    private View mChildScrollView;
    private boolean enableSwitcher = false;

    private static final int INVALID_POINTER = -1;
    private int mScrollChildPosition = 0;

    //用于防止布局不必要的layout。
    private int mLastTop, mLastMoreTop;
    private final int MINE_DIFF_LAYOUT = 5;

    //记录当前动画是否在自动滚动。
    private boolean mOnAnimScrolling;


    public DragLoadMoreBehavior() {
    }

    public DragLoadMoreBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragLoadMoreBehavior setCallBack(CallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public void disableSwitcher() {
        enableSwitcher = false;
//        fling();
    }

    public void enableSwitcher() {
        enableSwitcher = true;
    }

    public void showMorePage(boolean showMorePage) {
        if (mShowMorePage == showMorePage || !mIsReady) {
            return;
        }
        if (mShowMorePage) {
            if (!showMorePage) {
                //hide
                animateOffsetTo(mChildView, mHideMaxOffset, mHideMinOffset, mHideMaxOffset, false);
            } else {
                //show
                animateOffsetTo(mChildView, mShowMinOffset, mShowMinOffset, mShowMaxOffset, true);
            }
        } else {
            if (showMorePage) {
                //show
                animateOffsetTo(mChildView, mShowMinOffset, mShowMinOffset, mShowMaxOffset, true);
            } else {
                //hide
                animateOffsetTo(mChildView, mHideMaxOffset, mHideMinOffset, mHideMaxOffset, false);
            }
        }
    }

    public boolean isShowMorePage() {
        return mShowMorePage;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        mChildView = child;
        if (dependency instanceof AppBarLayout) {
            mAppBarLayout = (AppBarLayout) dependency;
        }
        if (dependency.getId() == R.id.start_page) {
            mDependencyView = dependency;
            //more page 默认不需要嵌套滚动，防止与 AppBarLayout 产生冲突
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                child.setNestedScrollingEnabled(false);
            }
            return true;
        }
        return false;
    }

    public void setDragThreshold(int pix) {
        mDragThreshold = pix;
    }

    public void reSetChildScrollView(int scrollChildPosition) {
        mScrollChildPosition = scrollChildPosition;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if (mDependencyView != null) {
            if (mDragThreshold == 0) {
                mDragThreshold = (int) Math.min(child.getMeasuredHeight() / 2f, parent.getMeasuredHeight() / 5f);
            }
            mMaxCanScrollTop = 0;
            if (mAppBarLayout != null) {
                //兼容 AppBarLayout
                mMaxCanScrollTop = mAppBarLayout.getHeight() - mAppBarLayout.getTotalScrollRange();
            }

            //view help
            if (mDependencyViewHelper == null) {
                mDependencyViewHelper = new ViewOffsetHelper(mDependencyView);
            }
            if (mChildViewHelper == null) {
                mChildViewHelper = new ViewOffsetHelper(child);
            }

            if (mShowMorePage) {
                //纠正位置
                final int dependencyViewTop = mDependencyViewHelper.getLayoutTop() + mDependencyViewHelper.getTopAndBottomOffset();
                final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
                final int top = dependencyViewTop + params.topMargin;
                final int bottom = top + mDependencyView.getMeasuredHeight() + params.bottomMargin;
                if (DEBUG) {
                    Log.d("lxt", "onLayoutChild more: top: %d" + top + " " + bottom);
                }
                if (Math.abs(mLastMoreTop - top) > MINE_DIFF_LAYOUT) {
                    mLastMoreTop = top;
                    mDependencyView.layout(mDependencyView.getLeft(), top, mDependencyView.getRight(), bottom);
                    Log.d("lxt", "onLayoutChild more MINE_DIFF_LAYOUT: top: %d" + top + " " + bottom);
                }
            } else if (!mOnAnimScrolling) {
                //view help
                mDependencyViewHelper.resetOffset();
                mChildViewHelper.resetOffset();

                //将 more page 布局到 start page 下面
                final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
                final int top = mDependencyView.getBottom() + params.topMargin;
                final int bottom = top + child.getMeasuredHeight() + params.bottomMargin;
                if (DEBUG) {
                    Log.d("lxt", "onLayoutChild: top: %d" + top + " " + bottom);
                }
                if (Math.abs(mLastTop - top) > MINE_DIFF_LAYOUT) {
                    mLastTop = top;
                    child.layout(parent.getPaddingLeft() + params.leftMargin, top,
                            parent.getWidth() - parent.getPaddingRight() - params.rightMargin, bottom);
                    Log.d("lxt", "onLayoutChild MINE_DIFF_LAYOUT: top: %d" + top + " " + bottom);
                }
                return true;
            }
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        return offsetChildAsNeeded() || super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout
                                               coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        boolean start = enableSwitcher && isTouchTypeScroll(type) && isVerticalAxis(axes) && mDependencyView != null && !mShowMorePage;
        return start || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                  @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (mIsBeginNestedScroll) {
            //当开始嵌套滚动事件时，预消费滚动事件
            consumed[1] = dy;
            offsetBy(-dy, mHideMinOffset, mHideMaxOffset);
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                               @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        //dy > 0 表示向上的滚动，canScrollVertically(1) 表示能否向上滚动
        //当向上滚动并且 start page 不能向上滚动时，消费事件
        if (mShowMorePage) {
            return;
        }
        if (!enableSwitcher) {
            return;
        }
        if (dyUnconsumed > 0 && !mDependencyView.canScrollVertically(1) && !mIsBeginNestedScroll && isTouchTypeScroll(type)) {
            mIsBeginNestedScroll = true;
            if (DEBUG) {
                Log.d("lxt", "开始嵌套滚动");
            }
            mDependencyViewHelper.resetOffset();
            mChildViewHelper.resetOffset();

            mIsReady = true;
            mHideMaxOffset = 0;
            mHideMinOffset = -child.getMeasuredHeight();
            mShowMaxOffset = 0;
            mShowMinOffset = mMaxCanScrollTop - child.getTop();
        }

        if (mIsBeginNestedScroll) {
            offsetBy(-dyUnconsumed, mHideMinOffset, mHideMaxOffset);
        }

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                   @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        if (mIsBeginNestedScroll) {
            mIsBeginNestedScroll = false;
            checkPageThreshold(child);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {

        if (!enableSwitcher) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }

        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }


        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                mIsBeingDragged = false;
                if (parent.isPointInChildBounds(child, x, y)) {
                    mLastMotionY = y;
                    mLastMotionX = x;
                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }
                final int y = (int) ev.getY(pointerIndex);
                final int x = (int) ev.getX(pointerIndex);
                final int yDiff = mLastMotionY - y;
                final int xDiff = mLastMotionX - x;
                if (Math.abs(yDiff) > mTouchSlop && Math.abs(yDiff) > Math.abs(xDiff)) {
                    if (yDiff < 0 && mShowMorePage && !findChildScrollView(child).canScrollVertically(-1)) {
                        mIsBeingDragged = true;
                    }
                    if (mIsBeingDragged) {
                        mLastMotionY = y;
                        Log.d("lxt", "拦截事件");
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            }
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {

        if (!enableSwitcher) {
            return super.onTouchEvent(parent, child, ev);
        }

        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (parent.isPointInChildBounds(child, x, y)) {
                    mLastMotionY = y;
                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int yDiff = mLastMotionY - y;
                if (!mIsBeingDragged && Math.abs(yDiff) > mTouchSlop) {
                    if (yDiff < 0 && mShowMorePage && !findChildScrollView(child).canScrollVertically(-1)) {
                        mIsBeingDragged = true;
                    }
                    if (yDiff > 0) {
                        yDiff -= mTouchSlop;
                    } else {
                        yDiff += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    mLastMotionY = y;
                    offsetBy(-yDiff, mShowMinOffset, mShowMaxOffset);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    checkPageThreshold(child);
                }
                mActivePointerId = INVALID_POINTER;
                break;
            }
        }

        return true;
    }

    private void checkPageThreshold(View child) {
        if (mShowMorePage) {
            final int deltaOffset = Math.abs(mDependencyViewHelper.getTopAndBottomOffset() -
                    mShowMinOffset);
            if (deltaOffset >= mDragThreshold) {
                //hide
                animateOffsetTo(child, mHideMaxOffset, mHideMinOffset, mHideMaxOffset, false);
            } else {
                //show
                animateOffsetTo(child, mShowMinOffset, mShowMinOffset, mShowMaxOffset, true);
            }
        } else {
            int deltaOffset = Math.abs(mChildViewHelper.getTopAndBottomOffset() - mHideMaxOffset);
            if (deltaOffset >= mDragThreshold) {
                //show
                animateOffsetTo(child, mShowMinOffset, mShowMinOffset, mShowMaxOffset, true);
            } else {
                //hide
                animateOffsetTo(child, mHideMaxOffset, mHideMinOffset, mHideMaxOffset, false);
            }
        }
    }

    private View findChildScrollView(View child) {
        mChildScrollView = ViewChildUtil.findChildScrollView(child, mScrollChildPosition);
        Log.i("lxg", "findChildScrollView");
        if (mChildScrollView == null) {
            mChildScrollView = child;
        }
        return mChildScrollView;
    }

    private int mLastOffsetDy, mLastOffsetMin, mLastOffsetMax;

    private void offsetBy(int dy, int min, int max) {
        if (mLastOffsetDy == dy && mLastOffsetMin == min && mLastOffsetMax == max) {
            return;
        }
        mLastOffsetDy = dy;
        mLastOffsetMin = min;
        mLastOffsetMax = max;
        int newOffset = mDependencyViewHelper.getTopAndBottomOffset() + dy;
        newOffset = MathUtils.clamp(newOffset, min, max);
        mDependencyViewHelper.setTopAndBottomOffset(newOffset);
        mChildViewHelper.setTopAndBottomOffset(newOffset);

    }

    private void animateOffsetTo(final View child, final int offset, final int min, final int max,
                                 final boolean showMorePage) {
        if (mOffsetAnimator != null) {
            mOffsetAnimator.cancel();
        }
        final int distance = Math.abs(mDependencyViewHelper.getTopAndBottomOffset() - offset);
        if (distance == 0) {
            return;
        }
        final float distanceRatio = (float) distance / child.getHeight();
        final int duration = (int) ((distanceRatio + 1) * 150);

        if (mOffsetAnimator == null) {
            mOffsetAnimator = ValueAnimator.ofInt(mDependencyViewHelper.getTopAndBottomOffset(), offset);
            mOffsetAnimator.setInterpolator(new DecelerateInterpolator());
        } else {
            mOffsetAnimator.removeAllListeners();
        }
        mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int currentOffset = (int) animation.getAnimatedValue();
                offsetBy(currentOffset - mDependencyViewHelper.getTopAndBottomOffset(), min, max);
                Log.i("lxt", "addUpdateListener " + currentOffset + " " + min + " " + max);
            }
        });
        mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mOnAnimScrolling = false;
                mShowMorePage = showMorePage;
                Log.i("lxt", "showMorePage " + showMorePage);
                if (mCallBack != null) {
                    mCallBack.change(mShowMorePage);
                }
            }
        });
        mOffsetAnimator.setDuration(duration);
        mOffsetAnimator.setIntValues(mDependencyViewHelper.getTopAndBottomOffset(), offset);
        mOffsetAnimator.start();
        mOnAnimScrolling = true;
    }

    /**
     * 让 more page 跟随 page
     */
    private boolean offsetChildAsNeeded() {
        if (mDependencyView == null || mShowMorePage) {
            return false;
        }
        mDependencyViewHelper.refreshOffsets();
        boolean changed = mChildViewHelper.setTopAndBottomOffset(mDependencyViewHelper.getTopAndBottomOffset());
        changed |= mChildViewHelper.setTopAndBottomOffset(mChildViewHelper.getTopAndBottomOffset() -
                (mChildView.getTop() - mDependencyView.getBottom()));
        return changed;
    }

    /**
     * 是否为垂直方向
     *
     * @param axes 方向
     * @return true 为垂直方向
     */
    private boolean isVerticalAxis(int axes) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    /**
     * 是否为 Touch 引起的滚动，不响应 Fling 的滚动
     *
     * @param type touch type
     * @return true 为 touch type
     */
    private boolean isTouchTypeScroll(int type) {
        return type == ViewCompat.TYPE_TOUCH;
    }

    public interface CallBack {
        void change(boolean showMorePage);
    }

}
