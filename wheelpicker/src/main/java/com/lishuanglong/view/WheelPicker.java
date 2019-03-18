package com.lishuanglong.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Scroller;

import java.util.List;

public class WheelPicker extends View {

    public static final String TAG = WheelPicker.class.getSimpleName();
    //数据集
    private List<String> mData;
    //用于画view内容的画笔
    private Paint mPaint;
    //用于弹性滑动
    private Scroller mScroller;
    //上一次手指触摸的y轴方向的坐标
    private float lastY = 0;

    public WheelPicker(Context context) {
        this(context, null);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //首先先把数据列表画出来
        if (mData != null && mData.size() > 0) {
            for (int i = 0; i < mData.size(); i++) {
                if (i == 0) {
                    canvas.drawText(mData.get(i), 0, 70, mPaint);
                } else {
                    canvas.drawText(mData.get(i), 0, i * 100 + 70, mPaint);
                }
            }
        }

        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        paint.setStrokeWidth(5);
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2 + 10, paint);

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        StringBuilder sb = new StringBuilder();
        int scrollY = getScrollY();
        int viewHeight = getHeight();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float y = event.getY();
                float offsetY = y - lastY;
                lastY = y;
                scrollBy(0, -(int) offsetY);

                break;
            case MotionEvent.ACTION_UP:
                //得到view content的总高度
                int maxViewHeight = 100 * mData.size() + 70;
                //得到view高度和y轴偏移量的和
                int viewHeightAndScrollY = viewHeight + scrollY;

                //从下往上滑动，如果view的高度+偏移量大于等于view content的总高度，就代表滑到底部了，需要恢复显示到最后一条数据
                if (viewHeightAndScrollY >= maxViewHeight) {
                    scrollTo(0, (maxViewHeight - viewHeight - 70));

//                    mScroller.startScroll(0,scrollY,0,(maxViewHeight - viewHeight - 70));


                }
                //从上往下滑，如果偏移量小于等于零就恢复到起点
                if (scrollY <= 0) {
                    scrollTo(0, 0);
                }
                break;
        }
        Log.d(TAG, sb.toString());
        return true;
    }


    /**
     * 设置数据
     */
    public WheelPicker setData(List<String> data) {
        this.mData = data;
        requestLayout();
        invalidate();
        return this;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //计算CurrX、CurrY，并检测是否完成滚动操作
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //请求重绘View树
            invalidate();
        }
    }

    private void init(Context context) {

        int textStrokeWidth = getResources().getDimensionPixelSize(R.dimen.text_stroke_width);
        int textSize = getResources().getDimensionPixelSize(R.dimen.text_size);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setStrokeWidth(textStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(textSize);

        //初始化用于弹性滑动的 Scroller
        mScroller = new Scroller(context);
    }


}
