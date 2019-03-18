package com.lishuanglong.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.Arrays;
import java.util.List;

/**
 * A WheelView for Android.
 *
 * @author lishuanglong
 * 2019/3/6
 */
public class WheelView extends View implements IDebug, IWheelView, Runnable {

    private static final String TAG = WheelView.class.getSimpleName();

    /**
     * 数据源
     */
    private List mData;

    /**
     * 数据项文本尺寸
     *
     * @see #setItemTextSize(int)
     */
    private int mItemTextSize;

    /**
     * 滚轮选择器中可见的数据项数量和滚轮选择器将会绘制的数据项数量
     *
     * @see #setVisibleItemCount(int)
     */
    private int mVisibleItemCount;
    private int mDrawnItemCount;

    /**
     * 当前被选中的数据项所显示的数据在数据源中的位置
     *
     * @see #setSelectedItemPosition(int)
     */
    private int mSelectedItemPosition;

    /**
     * 滚轮选择器的每一个数据项文本是否拥有相同的宽度
     *
     * @see #setSameWidth(boolean)
     */
    private boolean hasSameWidth;

    /**
     * 滚轮选择器中最宽或最高的文本在数据源中的位置
     */
    private int mTextMaxWidthPosition;

    /**
     * 最宽的文本
     *
     * @see #setMaximumWidthText(String)
     */
    private String mMaxWidthText;

    /**
     * 数据项文本颜色以及被选中的数据项文本颜色
     *
     * @see #setItemTextColor(int)
     * @see #setSelectedItemTextColor(int)
     */
    private int mItemTextColor;
    private int mSelectedItemTextColor;

    /**
     * 数据项之间间距
     *
     * @see #setItemSpace(int)
     */
    private int mItemSpace;

    /**
     * 数据是否循环展示
     *
     * @see #setCyclic(boolean)
     */
    private boolean isCyclic;

    /**
     * 是否显示指示器
     *
     * @see #setIndicator(boolean)
     */
    private boolean hasIndicator;

    /**
     * 指示器颜色
     *
     * @see #setIndicatorColor(int)
     */
    private int mIndicatorColor;

    /**
     * 指示器尺寸
     *
     * @see #setIndicatorSize(int)
     */
    private int mIndicatorSize;

    /**
     * 是否显示幕布
     *
     * @see #setCurtain(boolean)
     */
    private boolean hasCurtain;

    /**
     * 幕布颜色
     *
     * @see #setCurtainColor(int)
     */
    private int mCurtainColor;

    /**
     * 是否显示空气感效果
     *
     * @see #setAtmospheric(boolean)
     */
    private boolean hasAtmospheric;

    /**
     * 滚轮是否为卷曲效果
     *
     * @see #setCurved(boolean)
     */
    private boolean isCurved;

    /**
     * 数据项对齐方式
     *
     * @see #setItemAlign(int)
     */
    private int mItemAlign;

    /**
     * 来自assets的字体路径
     */
    private String fontPath;

    /**
     * 用于绘画 wheelview 内容的画笔
     */
    private Paint mPaint;

    /**
     * 用于处理滚动效果的工具类
     */
    private Scroller mScroller;

    /**
     * 滚轮滑动时的最小/最大速度
     */
    private int mMinimumVelocity = 50;
    private int mMaximumVelocity = 8000;

    /**
     * 点击与触摸的切换阀值
     */
    private int mTouchSlop = 8;

    private Rect mRectDrawn;
    private Rect mRectIndicatorHead;
    private Rect mRectIndicatorFoot;
    private Rect mRectCurrentItem;

    private Camera mCamera;
    private Matrix mMatrixRotate;
    private Matrix mMatrixDepth;

    /**
     * 单个文本最大宽高
     */
    private int mTextMaxWidth;
    private int mTextMaxHeight;

    /**
     * 是否开启debug
     */
    private boolean isDebug;

    /**
     * 滚轮选择器中心坐标
     */
    private int mWheelCenterX;
    private int mWheelCenterY;

    /**
     * 滚轮选择器绘制中心坐标
     */
    private int mDrawnCenterX;
    private int mDrawnCenterY;

    /**
     * 滚轮选择器内容区域高度的一半
     */
    private int mHalfWheelHeight;

    /**
     * 滚轮选择器单个数据项高度以及单个数据项一半的高度
     */
    private int mItemHeight;
    private int mHalfItemHeight;

    /**
     * 滚轮滑动时可以滑动到的最小/最大的Y坐标
     */
    private int mMinFlingY;
    private int mMaxFlingY;

    private OnItemSelectedListener mOnItemSelectedListener;
    private OnWheelChangeListener mOnWheelChangeListener;

    /**
     * 数据项对齐方式标识值
     *
     * @see #setItemAlign(int)
     */
    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_RIGHT = 2;

    /**
     * 滚动状态标识值
     *
     * @see OnWheelChangeListener#onWheelScrollStateChanged(int)
     */
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SCROLLING = 2;

    /**
     * 滚轮选择器视图区域在Y轴方向上的偏移值
     */
    private int mScrollOffsetY;

    /**
     * 滚轮选择器将会绘制的Item数量的一半
     */
    private int mHalfDrawnItemCount;

    /**
     * 确定当前滚动动画是由touchEvent还是setSelectedItemPosition触发。
     * 用户添加的eventListeners只会在touchEvents之后触发。
     */
    private boolean isTouchTriggered;

    /**
     * 当前被选中的数据项所显示的数据在数据源中的位置
     *
     * @see #getCurrentItemPosition()
     */
    private int mCurrentItemPosition;

    private VelocityTracker mTracker;

    /**
     * 是否为强制结束滑动
     */
    private boolean isForceFinishScroll;

    /**
     * 用户手指上一次触摸事件发生时事件Y坐标
     */
    private int mLastPointY;

    /**
     * 手指触摸屏幕时事件点的Y坐标
     */
    private int mDownPointY;

    /**
     * 是否为点击模式
     */
    private boolean isClick;

    private final Handler mHandler = new Handler();

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //根据宽高获取当前模式
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //根据宽高获取当前view的size
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算原始内容尺寸，宽高
        int resultWidth = mTextMaxWidth;
        int resultHeight = mTextMaxHeight * mVisibleItemCount + mItemSpace * (mVisibleItemCount - 1);

        // 如果开启弯曲效果则需要重新计算弯曲后的尺寸
        if (isCurved) {
            resultHeight = (int) (2 * resultHeight / Math.PI);
        }
        if (isDebug) {
            Log.d(TAG, "Wheel's content size is (" + resultWidth + ":" + resultHeight + ")");
        }


        // 考虑内边距对尺寸的影响
        resultWidth += getPaddingLeft() + getPaddingRight();
        resultHeight += getPaddingTop() + getPaddingBottom();
        if (isDebug) {
            Log.d(TAG, "Wheel's size is (" + resultWidth + ":" + resultHeight + ")");
        }

        //考虑父容器对尺寸的影响
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // 设置内容区域，左上右下
        mRectDrawn.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        if (isDebug) {
            Log.d(TAG, "Wheel's drawn rect size is (" + mRectDrawn.width() + ":" +
                    mRectDrawn.height() + ") and location is (" + mRectDrawn.left + ":" +
                    mRectDrawn.top + ")");
        }


        //获取内容区域中心坐标
        mWheelCenterX = mRectDrawn.centerX();
        mWheelCenterY = mRectDrawn.centerY();

        // 计算数据项绘制中心
        computeDrawnCenter();

        mHalfWheelHeight = mRectDrawn.height() / 2;

        mItemHeight = mRectDrawn.height() / mVisibleItemCount;
        mHalfItemHeight = mItemHeight / 2;

        // 初始化滑动最大坐标
        computeFlingLimitY();

        // 计算指示器绘制区域
        computeIndicatorRect();

        // 计算当前选中的数据项区域
        computeCurrentItemRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOnWheelChangeListener != null) {
            mOnWheelChangeListener.onWheelScrolled(mScrollOffsetY);
        }

        int drawnDataStartPos = -mScrollOffsetY / mItemHeight - mHalfDrawnItemCount;

        for (int drawnDataPos = drawnDataStartPos + mSelectedItemPosition, drawnOffsetPos = -mHalfDrawnItemCount;
             drawnDataPos < drawnDataStartPos + mSelectedItemPosition + mDrawnItemCount; drawnDataPos++, drawnOffsetPos++) {

            String data = "";
            if (isCyclic) {
                int actualPos = drawnDataPos % mData.size();
                actualPos = actualPos < 0 ? (actualPos + mData.size()) : actualPos;
                data = String.valueOf(mData.get(actualPos));
            } else {
                if (isPosInRang(drawnDataPos))
                    data = String.valueOf(mData.get(drawnDataPos));
            }

            mPaint.setColor(mItemTextColor);
            mPaint.setStyle(Paint.Style.FILL);

            int mDrawnItemCenterY = mDrawnCenterY + (drawnOffsetPos * mItemHeight) + mScrollOffsetY % mItemHeight;

            /*********************************************************************************/
            int distanceToCenter = 0;
            if (isCurved) {
                // 计算数据项绘制中心距离滚轮中心的距离比率
                float ratio = (mDrawnCenterY - Math.abs(mDrawnCenterY - mDrawnItemCenterY) - mRectDrawn.top) * 1.0F / (mDrawnCenterY - mRectDrawn.top);

                // 计算单位
                int unit = 0;
                if (mDrawnItemCenterY > mDrawnCenterY) {
                    unit = 1;
                } else if (mDrawnItemCenterY < mDrawnCenterY) {
                    unit = -1;
                }

                float degree = (-(1 - ratio) * 90 * unit);
                if (degree < -90) {
                    degree = -90;
                }
                if (degree > 90) {
                    degree = 90;
                }

                distanceToCenter = computeSpace((int) degree);

                int transX = mWheelCenterX;
                switch (mItemAlign) {
                    case ALIGN_LEFT:
                        transX = mRectDrawn.left;
                        break;
                    case ALIGN_RIGHT:
                        transX = mRectDrawn.right;
                        break;
                }
                int transY = mWheelCenterY - distanceToCenter;

                mCamera.save();
                mCamera.rotateX(degree);
                mCamera.getMatrix(mMatrixRotate);
                mCamera.restore();
                mMatrixRotate.preTranslate(-transX, -transY);
                mMatrixRotate.postTranslate(transX, transY);

                mCamera.save();
                mCamera.translate(0, 0, computeDepth((int) degree));
                mCamera.getMatrix(mMatrixDepth);
                mCamera.restore();
                mMatrixDepth.preTranslate(-transX, -transY);
                mMatrixDepth.postTranslate(transX, transY);

                mMatrixRotate.postConcat(mMatrixDepth);
            }
            /*********************************************************************************/


            if (hasAtmospheric) {
                int alpha = (int) ((mDrawnCenterY - Math.abs(mDrawnCenterY - mDrawnItemCenterY)) * 1.0F / mDrawnCenterY * 255);
                alpha = alpha < 0 ? 0 : alpha;
                mPaint.setAlpha(alpha);
            }

            // 根据卷曲与否计算数据项绘制Y方向中心坐标
            int drawnCenterY = isCurved ? mDrawnCenterY - distanceToCenter : mDrawnItemCenterY;

            // 判断是否需要为当前数据项绘制不同颜色
            if (mSelectedItemTextColor != -1) {
                canvas.save();
                if (isCurved) canvas.concat(mMatrixRotate);
                canvas.clipRect(mRectCurrentItem, Region.Op.DIFFERENCE);
                canvas.drawText(data, mDrawnCenterX, drawnCenterY, mPaint);
                canvas.restore();

                mPaint.setColor(mSelectedItemTextColor);
                canvas.save();
                if (isCurved) canvas.concat(mMatrixRotate);
                canvas.clipRect(mRectCurrentItem);
                canvas.drawText(data, mDrawnCenterX, drawnCenterY, mPaint);
                canvas.restore();
            } else {
                canvas.save();
                canvas.clipRect(mRectDrawn);
                if (isCurved) canvas.concat(mMatrixRotate);
                canvas.drawText(data, mDrawnCenterX, drawnCenterY, mPaint);
                canvas.restore();
            }
            if (isDebug) {
                canvas.save();
                canvas.clipRect(mRectDrawn);
                mPaint.setColor(0xFFEE3333);
                int lineCenterY = mWheelCenterY + (drawnOffsetPos * mItemHeight);
                canvas.drawLine(mRectDrawn.left, lineCenterY, mRectDrawn.right, lineCenterY, mPaint);
                mPaint.setColor(0xFF3333EE);
                mPaint.setStyle(Paint.Style.STROKE);
                int top = lineCenterY - mHalfItemHeight;
                canvas.drawRect(mRectDrawn.left, top, mRectDrawn.right, top + mItemHeight, mPaint);
                canvas.restore();
            }
        }

        // 是否需要绘制幕布
        if (hasCurtain) {
            mPaint.setColor(mCurtainColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mRectCurrentItem, mPaint);
        }
        // 是否需要绘制指示器
        if (hasIndicator) {
            mPaint.setColor(mIndicatorColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mRectIndicatorHead, mPaint);
            canvas.drawRect(mRectIndicatorFoot, mPaint);
        }
        if (isDebug) {
            mPaint.setColor(0x4433EE33);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, getPaddingLeft(), getHeight(), mPaint);
            canvas.drawRect(0, 0, getWidth(), getPaddingTop(), mPaint);
            canvas.drawRect(getWidth() - getPaddingRight(), 0, getWidth(), getHeight(), mPaint);
            canvas.drawRect(0, getHeight() - getPaddingBottom(), getWidth(), getHeight(), mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchTriggered = true;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                } else {
                    mTracker.clear();
                }
                mTracker.addMovement(event);

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    isForceFinishScroll = true;
                }
                mDownPointY = mLastPointY = (int) event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mDownPointY - event.getY()) < mTouchSlop) {
                    isClick = true;
                    break;
                }

                isClick = false;

                mTracker.addMovement(event);

                if (mOnWheelChangeListener != null) {
                    mOnWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                }

                // 滚动内容
                float move = event.getY() - mLastPointY;
                if (Math.abs(move) < 1) {
                    break;
                }

                mScrollOffsetY += move;
                mLastPointY = (int) event.getY();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                if (isClick && !isForceFinishScroll) {
                    break;
                }

                mTracker.addMovement(event);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                    mTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                } else {
                    mTracker.computeCurrentVelocity(1000);
                }

                // 根据速度判断是该滚动还是滑动
                isForceFinishScroll = false;

                int velocity = (int) mTracker.getYVelocity();

                if (Math.abs(velocity) > mMinimumVelocity) {
                    mScroller.fling(0, mScrollOffsetY, 0, velocity, 0, 0, mMinFlingY, mMaxFlingY);
                    mScroller.setFinalY(mScroller.getFinalY() + computeDistanceToEndPoint(mScroller.getFinalY() % mItemHeight));
                } else {
                    mScroller.startScroll(0, mScrollOffsetY, 0, computeDistanceToEndPoint(mScrollOffsetY % mItemHeight));
                }

                // 校正坐标
                if (!isCyclic) {
                    if (mScroller.getFinalY() > mMaxFlingY) {
                        mScroller.setFinalY(mMaxFlingY);
                    } else if (mScroller.getFinalY() < mMinFlingY) {
                        mScroller.setFinalY(mMinFlingY);
                    }
                }

                mHandler.post(this);
                if (mTracker != null) {
                    mTracker.recycle();
                    mTracker = null;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (mTracker != null) {
                    mTracker.recycle();
                    mTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void run() {
        if (mData == null || mData.size() == 0) {
            return;
        }
        if (mScroller.isFinished() && !isForceFinishScroll) {
            if (mItemHeight == 0) {
                return;
            }
            int position = (-mScrollOffsetY / mItemHeight + mSelectedItemPosition) % mData.size();
            position = position < 0 ? position + mData.size() : position;
            if (isDebug) {
                Log.d(TAG, position + ":" + mData.get(position) + ":" + mScrollOffsetY);
            }

            mCurrentItemPosition = position;
            if (mOnItemSelectedListener != null && isTouchTriggered) {
                mOnItemSelectedListener.onItemSelected(this, mData.get(position), position);
            }

            if (mOnWheelChangeListener != null && isTouchTriggered) {
                mOnWheelChangeListener.onWheelSelected(position);
                mOnWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            }
        }
        if (mScroller.computeScrollOffset()) {
            if (mOnWheelChangeListener != null) {
                mOnWheelChangeListener.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
            }
            mScrollOffsetY = mScroller.getCurrY();
            postInvalidate();
            mHandler.postDelayed(this, 16);
        }
    }

    @Override
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public int getVisibleItemCount() {
        return mVisibleItemCount;
    }

    @Override
    public void setVisibleItemCount(int count) {
        mVisibleItemCount = count;
        updateVisibleItemCount();
        requestLayout();
    }

    @Override
    public boolean isCyclic() {
        return isCyclic;
    }

    @Override
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        computeFlingLimitY();
        invalidate();
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    @Override
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    @Override
    public void setSelectedItemPosition(int position) {
        setSelectedItemPosition(position, true);
    }


    @Override
    public int getCurrentItemPosition() {
        return mCurrentItemPosition;
    }

    @Override
    public List getData() {
        return mData;
    }

    @Override
    public void setData(List data) {
        if (data == null) {
            throw new NullPointerException("WheelPicker's data can not be null!");
        }

        mData = data;

        // 重置位置
        if (mSelectedItemPosition > data.size() - 1 || mCurrentItemPosition > data.size() - 1) {
            mSelectedItemPosition = mCurrentItemPosition = data.size() - 1;
        } else {
            mSelectedItemPosition = mCurrentItemPosition;
        }
        mScrollOffsetY = 0;
        computeTextSize();
        computeFlingLimitY();
        requestLayout();
        invalidate();
    }

    public void setSameWidth(boolean hasSameWidth) {
        this.hasSameWidth = hasSameWidth;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public boolean hasSameWidth() {
        return hasSameWidth;
    }


    @Override
    public void setOnWheelChangeListener(OnWheelChangeListener listener) {
        mOnWheelChangeListener = listener;
    }

    @Override
    public String getMaximumWidthText() {
        return mMaxWidthText;
    }

    @Override
    public void setMaximumWidthText(String text) {
        if (null == text)
            throw new NullPointerException("Maximum width text can not be null!");
        mMaxWidthText = text;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getMaximumWidthTextPosition() {
        return mTextMaxWidthPosition;
    }

    @Override
    public void setMaximumWidthTextPosition(int position) {
        if (!isPosInRang(position))
            throw new ArrayIndexOutOfBoundsException("Maximum width text Position must in [0, " +
                    mData.size() + "), but current is " + position);
        mTextMaxWidthPosition = position;
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    @Override
    public void setSelectedItemTextColor(int color) {
        mSelectedItemTextColor = color;
        computeCurrentItemRect();
        invalidate();
    }

    @Override
    public int getItemTextColor() {
        return mItemTextColor;
    }

    @Override
    public void setItemTextColor(int color) {
        mItemTextColor = color;
        invalidate();
    }

    @Override
    public int getItemTextSize() {
        return mItemTextSize;
    }

    @Override
    public void setItemTextSize(int size) {
        mItemTextSize = size;
        mPaint.setTextSize(mItemTextSize);
        computeTextSize();
        requestLayout();
        invalidate();
    }

    @Override
    public int getItemSpace() {
        return mItemSpace;
    }

    @Override
    public void setItemSpace(int space) {
        mItemSpace = space;
        requestLayout();
        invalidate();
    }

    @Override
    public void setIndicator(boolean hasIndicator) {
        this.hasIndicator = hasIndicator;
        computeIndicatorRect();
        invalidate();
    }

    @Override
    public boolean hasIndicator() {
        return hasIndicator;
    }

    @Override
    public int getIndicatorSize() {
        return mIndicatorSize;
    }

    @Override
    public void setIndicatorSize(int size) {
        mIndicatorSize = size;
        computeIndicatorRect();
        invalidate();
    }

    @Override
    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    @Override
    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        invalidate();
    }

    @Override
    public void setCurtain(boolean hasCurtain) {
        this.hasCurtain = hasCurtain;
        computeCurrentItemRect();
        invalidate();
    }

    @Override
    public boolean hasCurtain() {
        return hasCurtain;
    }

    @Override
    public int getCurtainColor() {
        return mCurtainColor;
    }

    @Override
    public void setCurtainColor(int color) {
        mCurtainColor = color;
        invalidate();
    }

    @Override
    public void setAtmospheric(boolean hasAtmospheric) {
        this.hasAtmospheric = hasAtmospheric;
        invalidate();
    }

    @Override
    public boolean hasAtmospheric() {
        return hasAtmospheric;
    }

    @Override
    public boolean isCurved() {
        return isCurved;
    }

    @Override
    public void setCurved(boolean isCurved) {
        this.isCurved = isCurved;
        requestLayout();
        invalidate();
    }

    @Override
    public int getItemAlign() {
        return mItemAlign;
    }

    @Override
    public void setItemAlign(int align) {
        mItemAlign = align;
        updateItemTextAlign();
        computeDrawnCenter();
        invalidate();
    }

    @Override
    public Typeface getTypeface() {
        if (mPaint != null) {
            return mPaint.getTypeface();
        }
        return null;
    }

    @Override
    public void setTypeface(Typeface tf) {
        if (mPaint != null) {
            mPaint.setTypeface(tf);
        }

        //计算文本大小
        computeTextSize();

        //重新绘制，刷新view
        requestLayout();
        invalidate();
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        //wheelview数据，默认R.array.WheelArrayDefault
        int idData = a.getResourceId(R.styleable.WheelView_wheel_data, 0);
        mData = Arrays.asList(getResources().getStringArray(idData == 0 ? R.array.WheelArrayDefault : idData));
        //item文字大小，默认24sp
        mItemTextSize = a.getDimensionPixelSize(R.styleable.WheelView_wheel_item_text_size, getResources().getDimensionPixelSize(R.dimen.WheelItemTextSize));
        //可见item的数量，默认7条
        mVisibleItemCount = a.getInt(R.styleable.WheelView_wheel_visible_item_count, 7);
        //选择item位置，默认Position 0
        mSelectedItemPosition = a.getInt(R.styleable.WheelView_wheel_selected_item_position, 0);
        //所有item项的宽度是否都一样,默认false
        hasSameWidth = a.getBoolean(R.styleable.WheelView_wheel_same_width, false);
        //滚轮选择器中最宽或最高的文本在数据源中的位置,默认-1
        mTextMaxWidthPosition = a.getInt(R.styleable.WheelView_wheel_maximum_width_text_position, -1);
        //最宽的文本string
        mMaxWidthText = a.getString(R.styleable.WheelView_wheel_maximum_width_text);
        //被选中的数据项文本颜色,默认-1
        mSelectedItemTextColor = a.getColor(R.styleable.WheelView_wheel_selected_item_text_color, -1);
        //数据项文本颜色,默认0xFF888888
        mItemTextColor = a.getColor(R.styleable.WheelView_wheel_item_text_color, 0xFF888888);
        //数据项之间间距,默认12dp
        mItemSpace = a.getDimensionPixelSize(R.styleable.WheelView_wheel_item_space, getResources().getDimensionPixelSize(R.dimen.WheelItemSpace));
        //数据是否循环展示,默认false
        isCyclic = a.getBoolean(R.styleable.WheelView_wheel_cyclic, false);
        //是否显示指示器,默认false
        hasIndicator = a.getBoolean(R.styleable.WheelView_wheel_indicator, false);
        //指示器颜色,默认0xFFEE3333
        mIndicatorColor = a.getColor(R.styleable.WheelView_wheel_indicator_color, 0xFFEE3333);
        //指示器尺寸,默认2dp
        mIndicatorSize = a.getDimensionPixelSize(R.styleable.WheelView_wheel_indicator_size, getResources().getDimensionPixelSize(R.dimen.WheelIndicatorSize));
        //是否显示幕布,默认false
        hasCurtain = a.getBoolean(R.styleable.WheelView_wheel_curtain, false);
        //幕布颜色,默认0x88FFFFFF
        mCurtainColor = a.getColor(R.styleable.WheelView_wheel_curtain_color, 0x88FFFFFF);
        //是否显示空气感效果,默认false
        hasAtmospheric = a.getBoolean(R.styleable.WheelView_wheel_atmospheric, false);
        //滚轮是否为卷曲效果,默认false
        isCurved = a.getBoolean(R.styleable.WheelView_wheel_curved, false);
        //数据项对齐方式,默认居中显示
        mItemAlign = a.getInt(R.styleable.WheelView_wheel_item_align, ALIGN_CENTER);
        //来自assets的字体路径
        fontPath = a.getString(R.styleable.WheelView_wheel_font_path);
        a.recycle();

        //可见数据项改变后更新与之相关的参数
        updateVisibleItemCount();

        //创建画笔，设置画笔的FLAG以及文本大小
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mPaint.setTextSize(mItemTextSize);

        //如果字体路径不为空的话，就设置字体
        if (fontPath != null) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
            setTypeface(typeface);
        }

        // 更新文本对齐方式
        updateItemTextAlign();

        // 计算文本尺寸
        computeTextSize();

        //初始化 Scroller，Scroller是一个专门用于处理滚动效果的工具类
        mScroller = new Scroller(getContext());

        //如果当前sdk版本大于等于1.6，就获取view的相关阈值
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            ViewConfiguration conf = ViewConfiguration.get(getContext());
            //获得允许执行fling （抛）的最小速度值
            mMinimumVelocity = conf.getScaledMinimumFlingVelocity();
            //获得允许执行fling （抛）的最大速度值
            mMaximumVelocity = conf.getScaledMaximumFlingVelocity();
            //满足这个像素距离，可以认为用户在滚动中
            mTouchSlop = conf.getScaledTouchSlop();
        }

        mRectDrawn = new Rect();
        mRectIndicatorHead = new Rect();
        mRectIndicatorFoot = new Rect();
        mRectCurrentItem = new Rect();

        mCamera = new Camera();

        mMatrixRotate = new Matrix();
        mMatrixDepth = new Matrix();

    }

    /**
     * 更新显示item的数量，如果小于2就抛异常，如果大于2不为奇数的话，让它变成奇数
     */
    private void updateVisibleItemCount() {

        if (mVisibleItemCount < 2)
            throw new ArithmeticException("Wheel's visible item count can not be less than 2!");

        // 确保滚轮选择器可见数据项数量为奇数
        if (mVisibleItemCount % 2 == 0) {
            mVisibleItemCount += 1;
        }

        mDrawnItemCount = mVisibleItemCount + 2;
        mHalfDrawnItemCount = mDrawnItemCount / 2;

    }

    /**
     * 计算文本大小
     */
    private void computeTextSize() {

        //初始化文本最大宽、高
        mTextMaxWidth = mTextMaxHeight = 0;

        if (hasSameWidth) { //如果全部 item 的宽度都一样，则测量第一个item就可以了
            mTextMaxWidth = (int) mPaint.measureText(String.valueOf(mData.get(0)));
        } else if (isPosInRang(mTextMaxWidthPosition)) {//如果最宽的文本的位置索引没有越界，那么就测量这个item
            mTextMaxWidth = (int) mPaint.measureText(String.valueOf(mData.get(mTextMaxWidthPosition)));
        } else if (!TextUtils.isEmpty(mMaxWidthText)) {//如果给出的最宽文本不为空，就测量它
            mTextMaxWidth = (int) mPaint.measureText(mMaxWidthText);
        } else {//条件都不满足的情况下，我们就自己找出最大最宽的那一条，并且测量
            for (Object obj : mData) {
                String text = String.valueOf(obj);
                int width = (int) mPaint.measureText(text);
                mTextMaxWidth = Math.max(mTextMaxWidth, width);
            }
        }

        //根据 mPaint.getFontMetrics() 得到的结果，算出文本最大高度
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        mTextMaxHeight = (int) (metrics.bottom - metrics.top);
    }

    /**
     * 检查 position 是否越界
     */
    private boolean isPosInRang(int position) {
        return position >= 0 && position < mData.size();
    }

    /**
     * 设置更新文本对其方式
     */
    private void updateItemTextAlign() {
        switch (mItemAlign) {
            case ALIGN_LEFT:
                mPaint.setTextAlign(Paint.Align.LEFT);
                break;
            case ALIGN_RIGHT:
                mPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            default:
                mPaint.setTextAlign(Paint.Align.CENTER);
                break;
        }
    }

    /**
     * 根据 mode 得出最终的宽高
     */
    private int measureSize(int mode, int sizeExpect, int sizeActual) {
        int realSize;
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect;
        } else {
            realSize = sizeActual;
            if (mode == MeasureSpec.AT_MOST)
                realSize = Math.min(realSize, sizeExpect);
        }
        return realSize;
    }

    /**
     * 计算数据项绘制中心
     */
    private void computeDrawnCenter() {
        switch (mItemAlign) {
            case ALIGN_LEFT:
                mDrawnCenterX = mRectDrawn.left;
                break;
            case ALIGN_RIGHT:
                mDrawnCenterX = mRectDrawn.right;
                break;
            default:
                mDrawnCenterX = mWheelCenterX;
                break;
        }
        mDrawnCenterY = (int) (mWheelCenterY - ((mPaint.ascent() + mPaint.descent()) / 2));
    }

    /**
     * 初始化滑动最大坐标
     */
    private void computeFlingLimitY() {
        int currentItemOffset = mSelectedItemPosition * mItemHeight;
        mMinFlingY = isCyclic ? Integer.MIN_VALUE : -mItemHeight * (mData.size() - 1) + currentItemOffset;
        mMaxFlingY = isCyclic ? Integer.MAX_VALUE : currentItemOffset;
    }

    /**
     * 计算指示器绘制区域
     */
    private void computeIndicatorRect() {

        if (!hasIndicator) {
            return;
        }

        int halfIndicatorSize = mIndicatorSize / 2;
        int indicatorHeadCenterY = mWheelCenterY + mHalfItemHeight;
        int indicatorFootCenterY = mWheelCenterY - mHalfItemHeight;

        mRectIndicatorHead.set(mRectDrawn.left, indicatorHeadCenterY - halfIndicatorSize, mRectDrawn.right, indicatorHeadCenterY + halfIndicatorSize);
        mRectIndicatorFoot.set(mRectDrawn.left, indicatorFootCenterY - halfIndicatorSize, mRectDrawn.right, indicatorFootCenterY + halfIndicatorSize);
    }

    /**
     * 计算当前选中的数据项区域
     */
    private void computeCurrentItemRect() {

        if (!hasCurtain && mSelectedItemTextColor == -1) {
            return;
        }

        mRectCurrentItem.set(mRectDrawn.left, mWheelCenterY - mHalfItemHeight, mRectDrawn.right, mWheelCenterY + mHalfItemHeight);
    }

    /**
     * 计算间隔
     */
    private int computeSpace(int degree) {
        return (int) (Math.sin(Math.toRadians(degree)) * mHalfWheelHeight);
    }

    /**
     * 计算纵深
     */
    private int computeDepth(int degree) {
        return (int) (mHalfWheelHeight - Math.cos(Math.toRadians(degree)) * mHalfWheelHeight);
    }

    /**
     * 计算距离到结束点
     */
    private int computeDistanceToEndPoint(int remainder) {
        if (Math.abs(remainder) > mHalfItemHeight) {
            if (mScrollOffsetY < 0) {
                return -mItemHeight - remainder;
            } else {
                return mItemHeight - remainder;
            }
        } else {
            return -remainder;
        }
    }

    public void setSelectedItemPosition(int position, final boolean animated) {
        isTouchTriggered = false;
        //如果滚动条处于运动状态，无论“动画”参数如何，我们都会进行非动画处理
        if (animated && mScroller.isFinished()) {
            int length = getData().size();
            int itemDifference = position - mCurrentItemPosition;
            if (itemDifference == 0) {
                return;
            }

            //找到最短的路径，如果它是循环的
            if (isCyclic && Math.abs(itemDifference) > (length / 2)) {
                itemDifference += (itemDifference > 0) ? -length : length;
            }
            mScroller.startScroll(0, mScroller.getCurrY(), 0, (-itemDifference) * mItemHeight);
            mHandler.post(this);
        } else {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            position = Math.min(position, mData.size() - 1);
            position = Math.max(position, 0);
            mSelectedItemPosition = position;
            mCurrentItemPosition = position;
            mScrollOffsetY = 0;
            computeFlingLimitY();
            requestLayout();
            invalidate();
        }
    }
}
