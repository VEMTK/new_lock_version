package com.weather.lock.clean;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.weather.lock.R;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by xlc on 2016/12/6.
 */
public class ShortCutActivity extends Activity implements CoreService.OnPeocessActionListener {

    RelativeLayout mRelativeLayout, ainm_layout;

    private Rect rect;

    ImageView cleanLightImg;

    ImageView point_img;

    ImageView clean_icon_img;

    private CoreService mCoreService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(ShortCutActivity.this);
            mCoreService.cleanAllProcess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_cut);

        bindService(new Intent(getApplicationContext(), CoreService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);

        ainm_layout = (RelativeLayout) findViewById(R.id.ainm_layout);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.mRelativeLayout);

        cleanLightImg = (ImageView) findViewById(R.id.clean_light_img);

        point_img = (ImageView) findViewById(R.id.point_img);

        clean_icon_img = (ImageView) findViewById(R.id.clean_icon_img);


        point_img.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.zoom_anim));

        clean_icon_img.startAnimation(AnimationUtils.loadAnimation(this,

                R.anim.rotate_anim));

        cleanLightImg.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.rotate_anim));


    }

    @Override
    public void onScanStarted(Context context) {

    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {

    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, final long cacheSize) {


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message message = Message.obtain();

                Bundle bundle = new Bundle();

                bundle.putLong("result", cacheSize);

                message.setData(bundle);

                handler.sendMessage(message);


            }
        }).start();
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long rs = msg.getData().getLong("result");
            dissmissAnimation(rs);


        }
    };

    private void dissmissAnimation(final Long rs) {

        point_img.setAnimation(null);

        point_img.setVisibility(View.GONE);

        // point_img.setImageResource(R.drawable.s_light);

        final ScaleAnimation z_animation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        z_animation.setDuration(1500);

        ainm_layout.startAnimation(z_animation);

        TranslateAnimation animation = new TranslateAnimation(0, getResources().getDisplayMetrics().widthPixels, 0, 0);

        animation.setInterpolator(new AccelerateInterpolator());

        animation.setDuration(1500);

        AnimationSet mAnimationSet = new AnimationSet(false);
        mAnimationSet.addAnimation(z_animation);
        mAnimationSet.setFillAfter(true);
        mAnimationSet.addAnimation(animation);
        ainm_layout.startAnimation(mAnimationSet);

        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {

                if (rs > 0) {
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.clean_msg), convertStorage(rs)), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.clean_now), Toast.LENGTH_LONG).show();
                }
                ainm_layout.setVisibility(View.GONE);

                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }


    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}