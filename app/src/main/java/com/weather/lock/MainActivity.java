package com.weather.lock;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.weather.lock.fragment.CloseFragment;
import com.weather.lock.fragment.MainFragment;
import com.weather.lock.task.CheckAppThemeTask;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.Util;
import com.weather.lock.view.BlurringView;

import java.util.List;

/**
 * Created by xlc on 2017/2/13.
 */
public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;

    private ImageView imageView;

    private BlurringView mBlurringView;

    private MyApp app;

    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    private long time;

    int app_status;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Util.lock_Screen(this,true);

        time = System.currentTimeMillis();

        app = (MyApp) MainActivity.this.getApplication();

        app_status = SharedUtil.getInstance(this).get_int(SharedUtil.APP_THEME_STAUS, 0);

        if (app_status == 1) {

            setTheme(R.style.wallpaper_theme);

        } else {

            setTheme(R.style.App_Theme_1);
        }
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码

        Util.setSystemBar(this);

        setContentView(R.layout.base_activity);

        /**********毛玻璃效果************/
        mBlurringView = (BlurringView) findViewById(R.id.blurring_view);

        View blurredView = findViewById(R.id.blurred_view);

        // Give the blurring view a reference to the blurred view.
        mBlurringView.setBlurredView(blurredView);

        /**********毛玻璃效果结束************/

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        imageView = (ImageView) findViewById(R.id.base_img);

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {

                    finish();

                    overridePendingTransition(0, R.anim.fade_out);

                    /***发送自定义解锁广播***/

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sendBroadcast(new Intent(Util.LOCK_WEATHER_ACTION));
                        }
                    }).start();

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (app_status == 0) imageView.setImageBitmap(Util.getWallpapperBitmap(this));

        new CheckAppThemeTask(MainActivity.this).execute();

        Log.e("lock", "MainActivity中onCreate时间：" + (System.currentTimeMillis() - time));

    }

    @Override
    protected void onResume() {
        super.onResume();

        LogUtil.info("base activity onResume");


        viewPager.setCurrentItem(1);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {

            return 2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public int getItemPosition(Object object) {

            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new CloseFragment();

                case 1:
                    return new MainFragment();
            }
            return null;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 删除快捷方式
     */
    public static void deleteShortCut(Context context) {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        /**删除和创建需要对应才能找到快捷方式并成功删除**/
        Intent intent = new Intent();
        intent.setClass(context, context.getClass());
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        context.sendBroadcast(shortcut);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Util.lock_Screen(this,false);

    }


}
