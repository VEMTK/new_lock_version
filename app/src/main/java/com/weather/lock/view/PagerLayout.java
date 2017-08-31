package com.weather.lock.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.weather.lock.listener.CameraClicklistener;
import com.weather.lock.listener.ScrollListener;

/**
 * Created by wangjie on 2016/11/8.
 * <p>
 * 1. 照http://www.tuicool.com/articles/InYjEn6实现弹性归位
 * 2. 修改up事件的位置
 * 3. 锁屏时禁止下拉，只允许上拉
 * 4. 设置下拉锁屏
 */
public class PagerLayout extends RelativeLayout {

    private Scroller mScroller;
    private GestureDetector mGestureDetector;

    private ScrollListener scrollListener = null;

    private boolean canScroll = false;

    private boolean camera_click = false;


    public void setClicklistener(CameraClicklistener clicklistener) {
        this.clicklistener = clicklistener;
    }

    private CameraClicklistener clicklistener;


    public void setCanScroll(Boolean can) {
        this.canScroll = can;
    }

    private int height = 0;

    private int width = 0;


    private int dipx = 0;

    public void setScrollListener(ScrollListener listener) {
        this.scrollListener = listener;
    }

    // 视图容器

    // 手指按下时y轴坐标
    private float mDownY = 0;

    // 视图高度
    private int mViewHeight = 0;

    public PagerLayout(Context context) {
        this(context, null);
    }

    public PagerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerLayout(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        // 初始化手势检测器
        mGestureDetector = new GestureDetector(context, new GestureListenerImpl());

        height = context.getResources().getDisplayMetrics().heightPixels;

        width = context.getResources().getDisplayMetrics().widthPixels;

        float scale = context.getResources().getDisplayMetrics().density;

        dipx = (int) (70 * scale + 0.5f);

        Log.e("tool", "dipx:" + dipx);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取视图高度
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须执行postInvalidate()从而调用computeScroll()
            //其实,在此调用invalidate();亦可
            postInvalidate();
        }
        super.computeScroll();
    }

    // 显示视图
    public void show() {

        prepareScroll(0, 0);
    }

    // 隐藏视图
    public void hide() {

        prepareScroll(0, mViewHeight);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 如果没有隐藏，则可以滑动

        switch (event.getAction()) {
            // 除了ACTION_UP，其他手势交给GestureDetector
            case MotionEvent.ACTION_DOWN:

                // 获取收按下时的y轴坐标
                mDownY = event.getY();

                float mDownX = event.getX();

                if (height - mDownY < dipx && width - mDownX < dipx) {

                    clicklistener.click_down();

                    camera_click = true;

                } else {
                    camera_click = false;
                }

                if (height - mDownY < 200) {

                    setCanScroll(true);

                } else {

                    setCanScroll(false);

                }
                return mGestureDetector.onTouchEvent(event);

            case MotionEvent.ACTION_UP:
                // 获取视图容器滚动的y轴距离
                int scrollY = this.getScrollY();
                // 未超过制定距离，则返回原来位置

                clicklistener.click_up();

                if (camera_click && scrollY < 30) {

                    clicklistener.click_camera();
                }

                if (scrollY < 300) {
                    // 准备滚动到原来位置
                    show();

                } else { // 超过指定距离，则上滑隐藏
                    // 隐藏
                    hide();

                    if (scrollListener != null)
                        scrollListener.scrollTop();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 获取当前滑动的y轴坐标

                if (!canScroll) return true;

                float curY = event.getY();
                // 获取移动的y轴距离
                float deltaY = curY - mDownY;
                // 阻止视图在原来位置时向下滚动
                if (deltaY < 0 || getScrollY() > 0) {
                    return mGestureDetector.onTouchEvent(event);
                } else {
                    return true;
                }
            default:
                //其余情况交给GestureDetector手势处理
                return mGestureDetector.onTouchEvent(event);
        }
        // }
        return super.onTouchEvent(event);
    }

    public void renew_layout() {
        prepareScroll(0, 0);
    }


    class GestureListenerImpl implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        //控制拉动幅度:
        //int disY=(int)((distanceY - 0.5)/2);
        //亦可直接调用:
        //smoothScrollBy(0, (int)distanceY);
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int disY = (int) ((distanceY - 0.5) / 2);
            beginScroll(0, disY);
            return false;
        }

        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }


    //滚动到目标位置
    protected void prepareScroll(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        beginScroll(dx, dy);
    }


    //设置滚动的相对偏移
    protected void beginScroll(int dx, int dy) {
        //第一,二个参数起始位置;第三,四个滚动的偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        //必须执行invalidate()从而调用computeScroll()
        invalidate();
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param charSequence
     */
    public void setText(int viewId, CharSequence charSequence) {
        TextView textView = (TextView) getView(viewId);
        textView.setText(charSequence);
    }

    /**
     * 设置文本颜色
     *
     * @param viewId
     * @param color
     */
    public void setTextColor(int viewId, int color) {
        TextView textView = (TextView) getView(viewId);
        textView.setTextColor(color);
    }

    /**
     * 设置文本字体大小
     *
     * @param viewId
     * @param textSize
     */
    public void setTextSize(int viewId, int textSize) {
        TextView textView = (TextView) getView(viewId);
        textView.setTextSize(textSize);
    }

    /**
     * 设置按钮点击事件
     *
     * @param viewId
     * @param listener
     */
    public void setButtonClickListener(int viewId, OnClickListener listener) {
        Button button = (Button) getView(viewId);
        button.setOnClickListener(listener);
    }


    /**
     * 设置背景颜色
     *
     * @param color
     */
    public void setBackgroundColor(int color) {
        this.setBackgroundColor(color);
    }


    /**
     * 设置背景图片资源id
     *
     * @param resId
     */
    public void setBackgroundResource(int resId) {
        this.setBackgroundResource(resId);
    }

    /**
     * 获取视图控件
     *
     * @param viewId
     * @return
     */
    public View getView(int viewId) {
        return this.findViewById(viewId);
    }

}
