package com.xml.library.ad;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;
import com.xml.library.services.B;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;

import java.util.Random;

/**
 * Created by xlc on 2016/12/29.
 */
public class Ab extends Handler {

    private Context mContext;

    private AdView adView = null;

    private WindowManager windowManager = null;

    private RelativeLayout mrLayout;

    private int status = 1;

    public Ab(Context c) {
        this.mContext = c;
        windowManager = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
    }

    public static void sendMsg(Ab a, int banner, int what) {
        Message message_scr_1 = Message.obtain();
        message_scr_1.arg1 = banner;
        message_scr_1.what = what;
        a.sendMessage(message_scr_1);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case B.CLEAR:
                removeView();
                break;
            case B.ADMOB_BANNER:
                LogUtil.info(B.TAG, "展示Admob Banner");
                status = msg.arg1;
                LogUtil.info(B.TAG, " catch top flag(0:no 1:yes) ==" + status);
                if (mrLayout != null) {
                    if (mrLayout.getParent() != null) {
                        return;
                    }
                }
                SPreferencesUtil.getInstance(mContext).save_long(SPreferencesUtil.IN_ADMOB_TIME, System.currentTimeMillis());
                new Asy().executeOnExecutor(HttpUtil.executorService);
                break;
        }
    }

    class Asy extends AsyncTask<Void, Integer, T> {
        @Override
        protected T doInBackground(Void... voids) {

            return DataBaseManager.getInstance(mContext).get_setData(3);
        }

        @Override
        protected void onPostExecute(T t) {
            super.onPostExecute(t);
            if (t != null) showAdmobBanner(t);
        }
    }

    private synchronized void showAdmobBanner(T t) {

        try {
            LogUtil.info(B.TAG, "展示Admob Banner 代码执行");

            LogUtil.info(B.TAG, "展示Admob Banner AdID:" + t.getAid());

            Log.i("Alog", "in Banner AdID:" + t.getAid());

            adView = new AdView(mContext);

            adView.setAdUnitId(t.getAid());

            adView.setAdSize(AdSize.BANNER);

            adView.loadAd(new AdRequest.Builder().build());

            /*****统计请求次数*****/
            StatService.onEvent(mContext, "admob_banner", "request_admob_banner", 1);

            /***开始统计请求的时长事件***/
            StatService.onEventStart(mContext, "admob_banner", "request_admob_banner");

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // TODO Auto-generated method stub
                    super.onAdClosed();
                    if (adView != null) {
                        adView.destroy();
                    }
                }
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // TODO Auto-generated method stub
                    super.onAdFailedToLoad(errorCode);
                    LogUtil.info("Alog", "ad banner error type:" + errorCode);
                    //请求失败事件
                    StatService.onEvent(mContext, "admob_banner", "request_admob_fail", 1);
                    /**结束请求时长统计*/
                    StatService.onEventEnd(mContext, "admob_banner", "request_admob_banner");
                }

                @Override
                public void onAdLoaded() {
                    // TODO Auto-generated method stub
                    if (adView.isShown()) {
                        LogUtil.info("Adlog", "Admob banner is show ");
                        return;
                    }
                    /**请求成功统计***/
                    StatService.onEvent(mContext, "admob_banner", "request_admob_success", 1);
                    /**结束请求时长统计*/
                    StatService.onEventEnd(mContext, "admob_banner", "request_admob_banner");

                    WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

                    wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;

                    wmParams.format = PixelFormat.RGBA_8888;

                    wmParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    /**
                     * 随机显示在屏幕上下
                     */
                    if (new Random().nextInt(2) == 0) {

                        wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

                        LogUtil.info(B.TAG, "show admob banner on Top");

                    } else {
                        LogUtil.info(B.TAG, "show admob banner on Bottom");

                        wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

                    }
                    wmParams.x = 0;

                    wmParams.y = 0;

                    wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;

                    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                    try {
                        mrLayout = addClose(mContext, adView);

                        if (mrLayout != null) {

                            windowManager.addView(mrLayout, wmParams);

                            StatService.onEvent(mContext, "admob_banner", "show_admob_banner", 1);
                            /***开始统计显示时长**/
                            StatService.onEventStart(mContext, "admob_banner", "show_admob_banner");
                        }

                    } catch (Exception e) {
                        // TODO: handle exception

                        e.printStackTrace();
                    }
                    // 20秒后自动消失
                    if (status == 0) {

                        Ab.this.sendEmptyMessageDelayed(B.CLEAR, 20 * 1000);
                    }
                    /* 保存次数 */
                    DataBaseManager.getInstance(mContext).update_counts();

                    super.onAdLoaded();
                }

                @Override
                public void onAdLeftApplication() {
                    // TODO Auto-generated method stub
                    super.onAdLeftApplication();
                    /***统计点击事件**/
                    StatService.onEvent(mContext, "admob_banner", "click_admob_banner", 1);
                    //  MyHelpUtil.saveAdmobBannerStatus(context);
                    removeView();
                }

                @Override
                public void onAdOpened() {
                    // TODO Auto-generated method stub
                    super.onAdOpened();

                    //Abactivity.startAbActivity(mContext);
                }
            });

        } catch (Exception e) {
            Log.i("Alog", "banner er:" + e.getMessage());
        }
    }

    private void removeView() {
        if (adView != null) {
            adView.destroy();
        }
        if (mrLayout != null && mrLayout.isShown()) {
            windowManager.removeView(mrLayout);
            mrLayout = null;
        }
    }

    @SuppressWarnings("ResourceType")
    private RelativeLayout addClose(final Context context, View view) {
        if (view == null) return null;

        RelativeLayout relativeLayout = new RelativeLayout(context);

        RelativeLayout.LayoutParams lyoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        relativeLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));

        relativeLayout.setLayoutParams(lyoutParams);

        view.setId(0x223);
        // Button
        view.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        int w = view.getMeasuredHeight();

        // //closeImageView
        // ------------------------------------------------------------//
        RelativeLayout.LayoutParams CloselayoutLayoutParams = new RelativeLayout.LayoutParams(w / 3, w / 3);

        CloselayoutLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, view.getId());

        TextView mTextView = new TextView(context);

        mTextView.setGravity(Gravity.CENTER);

        mTextView.setText("X");

        mTextView.setBackgroundColor(Color.BLACK);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeView();

                StatService.onEvent(context, "admob_banner", "cancel_admob_banner", 1);
            }
        });
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        // 推迟5秒显示动画
        alphaAnimation.setStartOffset(5000);
        // 动画显示时间为2秒
        alphaAnimation.setDuration(2000);

        mTextView.setAnimation(alphaAnimation);

        RelativeLayout.LayoutParams viewlLayoutParams = new RelativeLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        relativeLayout.addView(view, viewlLayoutParams);

        relativeLayout.addView(mTextView, CloselayoutLayoutParams);

        return relativeLayout;

    }

}
