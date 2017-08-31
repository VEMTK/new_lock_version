package com.weather.lock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.formats.NativeAdView;
import com.weather.lock.db.DataBaseManager;
import com.weather.lock.entity.Location;
import com.weather.lock.listener.Adlistener;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.Util;
import com.xml.library.modle.T;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xlc on 2017/2/10.
 */
public class SettingActivity extends Activity {

    private ListView locationList;

    private EditText editText;

    private LinearLayout search_btn;

    // private List<String> data_list;

    private ListViewAdapter listViewAdapter;

    private static final String TEMP_STATUS = "temp_status";

    private SharedUtil sharedUtil = null;

    private TextView temp_c, temp_f, search_tip;

    private String editext_content;


    private TextView show_location_textview;

    private RelativeLayout delete_edit_content_layout, search_list_back;


    private LinearLayout loaction_show_layout, location_click_layout, search_list_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedUtil = SharedUtil.getInstance(this);

        setContentView(R.layout.location_layout);

        //qury_data();

        initView();

        listViewAdapter = new ListViewAdapter();

        locationList.setAdapter(listViewAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initView() {

        if(sharedUtil.get_int(SharedUtil.LAYOUT_LEFT_RIGHT,0)==1) {

            ImageView setting_location_bcak = (ImageView) findViewById(R.id.set_location_back);

            setting_location_bcak.setImageBitmap(Util.rotateBitmap(this,R.drawable.x_ic_back));

            ImageView location_icon = (ImageView) findViewById(R.id.location_icon);

            location_icon.setImageBitmap(Util.rotateBitmap(this,R.drawable.x_loaction_go));

            ImageView location_search_back = (ImageView) findViewById(R.id.location_search_back);

            location_search_back.setImageBitmap(Util.rotateBitmap(this,R.drawable.x_search_edit_back));

        }

        search_list_back = (RelativeLayout) findViewById(R.id.search_list_back);

        loaction_show_layout = (LinearLayout) findViewById(R.id.search_location_layout);

        location_click_layout = (LinearLayout) findViewById(R.id.location_layout_click);

        search_list_layout = (LinearLayout) findViewById(R.id.search_list_layout);


        delete_edit_content_layout = (RelativeLayout) findViewById(R.id.search_delete_btn);

        show_location_textview = (TextView) findViewById(R.id.show_location_text);

        show_location_textview.setText(sharedUtil.get_string(SharedUtil.LOCATION_NAME, getResources().getString(R.string.add_new_location)));

        search_tip = (TextView) findViewById(R.id.search_tip);

        locationList = (ListView) findViewById(R.id.location_list_view);

        editText = (EditText) findViewById(R.id.tvSearch);

        search_btn = (LinearLayout) findViewById(R.id.search_download_btn);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 先隐藏键盘
                    // 搜索，进行自己要的操作...
                    locationOnClick(search_btn);

                    return true;
                }
                return false;
            }
        });

        temp_c = (TextView) findViewById(R.id.temp_c);

        temp_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedUtil.save_int(SharedUtil.USER_SET_TEMP_UNIT, 1);

                temp_c.setTextColor(getResources().getColor(R.color.black));

                temp_f.setTextColor(getResources().getColor(R.color.white_gry));

                sharedUtil.save_int(TEMP_STATUS, 0);

                LogUtil.info("click temp_c");
            }
        });

        temp_f = (TextView) findViewById(R.id.temp_f);

        temp_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedUtil.save_int(SharedUtil.USER_SET_TEMP_UNIT, 1);

                LogUtil.info("click temp_f");

                temp_f.setTextColor(getResources().getColor(R.color.black));

                temp_c.setTextColor(getResources().getColor(R.color.white_gry));

                sharedUtil.save_int(TEMP_STATUS, 1);

            }
        });

        if (sharedUtil.get_int(TEMP_STATUS, 1) == 0) {

            temp_c.setTextColor(getResources().getColor(R.color.black));

            temp_f.setTextColor(getResources().getColor(R.color.white_gry));

        } else {
            temp_f.setTextColor(getResources().getColor(R.color.black));

            temp_c.setTextColor(getResources().getColor(R.color.white_gry));
        }


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                editext_content = editText.getText().toString();

                if (TextUtils.isEmpty(editext_content)) {

                    delete_edit_content_layout.setVisibility(View.INVISIBLE);

                    queryData("");

                } else {

                    delete_edit_content_layout.setVisibility(View.VISIBLE);

                    queryData(editext_content);
                }

            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus && TextUtils.isEmpty(editext_content)) {
                    LogUtil.info("获取到焦点并且输入框为空，查询全部数据");
                    queryData("");
                }
            }
        });

        // new GetAdTask().execute();
    }

    private NativeExpressAdView adView;

    private class GetAdTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... params) {

            synchronized (this) {
                T id2 = com.xml.library.db.DataBaseManager.getInstance(SettingActivity.this).get_setData(1);
                if (null == id2) {
                    return null;
                } else {

                    String sub_str=id2.getAid().trim();

                    return sub_str.substring(sub_str.indexOf("|") + 1);
                }
            }

        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LogUtil.info("获取广告id:" + s);

            LogUtil.info("lock","获取广告id:" + s);

            if (!TextUtils.isEmpty(s) ) {

                if (adView == null || !adView.isShown()) {

                    adView = new NativeExpressAdView(SettingActivity.this);

                    AdSize adSize = new AdSize(AdSize.FULL_WIDTH, getResources().getInteger(R.integer.ad_height));

                    adView.setAdSize(adSize);

                    adView.setAdUnitId(s);

                    AdRequest request = new AdRequest.Builder()
                            .addTestDevice("35F243B58C5EAE1C5A3666121893A9EE")
                            .build();
                    adView.loadAd(request);

                    adView.setAdListener(new Adlistener(getApplicationContext()));

                    loaction_show_layout.addView(adView);

                }

            }
        }
    }



    /***
     * 删除输入框内容
     */
    private void delete_editText() {

        editText.setText("");

//        int index = editText.getSelectionStart();   //获取Edittext光标所在位置
//        if (!editext_content.equals("") && index > 0) {//判断输入框不为空，执行删除
//            editText.getText().delete(index - 1, index);
//        }
    }


    public void locationOnClick(View view) {
        switch (view.getId()) {
            case R.id.search_download_btn:

                if (TextUtils.isEmpty(editext_content)) {

                    Toast.makeText(SettingActivity.this, "Input empty！", Toast.LENGTH_SHORT).show();

                } else {

                    dismissInputMethod();

                    search_tip.setVisibility(View.VISIBLE);

                    search_tip.setText(getResources().getString(R.string.query_city));

                    new GetCityNameAysTask().execute(editext_content);
                }

                break;

            case R.id.search_back:

                finish();

                break;

            case R.id.search_delete_btn:

                delete_editText();

                break;

            case R.id.location_layout_click:

                search_list_layout.setVisibility(View.VISIBLE);

                loaction_show_layout.setVisibility(View.GONE);

               // editText.setFocusable(true);

                editText.requestFocus();

                showInputMethod();

                break;

            case R.id.search_list_back:

                search_list_layout.setVisibility(View.GONE);

                loaction_show_layout.setVisibility(View.VISIBLE);

                dismissInputMethod();

                break;


        }
    }
    @Override
    public void finish() {
        super.finish();
        dismissInputMethod();
        overridePendingTransition(0, R.anim.fade_out_right);

    }
    /**
     * 隐藏键盘
     */
    private void dismissInputMethod() {

        InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        input.hideSoftInputFromWindow(editText.getWindowToken(), 0);


    }

    private void showInputMethod() {

        InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //input.toggleSoftInput(1, InputMethodManager.HIDE_NOT_ALWAYS);

        input.showSoftInput(editText,InputMethodManager.SHOW_FORCED);
    }

    /**
     * 插入数据
     */
    private void insertData(String tempName) {

        if (TextUtils.isEmpty(tempName)) {
            return;
        }
        DataBaseManager.getInstance(getApplicationContext()).insertsetData(tempName);
    }

    /**
     * 模糊查询数据
     */
    private void queryData(String tempName) {

        Cursor cursor = DataBaseManager.getInstance(getApplicationContext()).qury_data(tempName);

        try {

            List<Location> list = new ArrayList<>();

            while (cursor.moveToNext()) {

                Log.e("tool","查询保存的信息："+cursor.getString(cursor.getColumnIndex("city_name")));

                Location location = new Location("", cursor.getString(cursor.getColumnIndex("city_name")),"");

                list.add(location);
            }
            listViewAdapter.setMlist(list, true);

        } catch (Exception e) {

            Log.e("tool","查询："+e.getMessage());

        } finally {



            if (cursor != null) cursor.close();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // sendBroacat();

    }

    public void sendBroacat() {

        Intent intent = new Intent();

        intent.setAction("gps.location." + getPackageName());

        sendBroadcast(intent);

    }

    public class ListViewAdapter extends BaseAdapter {

        private LayoutInflater layoutInflater;

        private boolean search = false;

        public void setMlist(List<Location> list, boolean s) {
            this.search = s;
            mlist = new ArrayList<>();
            mlist.addAll(list);
            notifyDataSetChanged();
        }

        private List<Location> mlist;

        public ListViewAdapter() {
            mlist = new ArrayList<>();
            layoutInflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public int getCount() {
            return mlist.isEmpty() ? 0 : mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return mlist.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;

            if (convertView == null) {

                convertView = layoutInflater.inflate(R.layout.location_item, null);

                viewHolder = new ViewHolder();

                viewHolder.textView = (TextView) convertView.findViewById(R.id.city_name);

                viewHolder.delete_img = (RelativeLayout) convertView.findViewById(R.id.delete_btn);

                convertView.setTag(viewHolder);//绑定ViewHolder对象

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (search) {
                viewHolder.delete_img.setVisibility(View.VISIBLE);

                viewHolder.delete_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DataBaseManager.getInstance(SettingActivity.this).deleteDataByCity(mlist.get(position).getLocation());

                        mlist.remove(position);

                        notifyDataSetChanged();
                    }
                });

            } else {
                viewHolder.delete_img.setVisibility(View.GONE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!search) {

                        sharedUtil.save_string(SharedUtil.LOCATION_CODE, mlist.get(position).getWoeid());

                        sharedUtil.save_string(SharedUtil.LOCATION_NAME, mlist.get(position).getLocation());

                        sharedUtil.save_string(SharedUtil.K_LOCATION,mlist.get(position).getFrist_location());

                        sharedUtil.save_int(SharedUtil.USER_SER_LOACTION, 1);

                        show_location_textview.setText(mlist.get(position).getLocation());

                        locationOnClick(search_list_back);

                    } else {

                        editText.setText(mlist.get(position).getLocation());

                        locationOnClick(search_btn);

                        dismissInputMethod();
                    }
                }
            });

            viewHolder.textView.setText(mlist.get(position).getLocation());

            return convertView;

        }

        public final class ViewHolder {

            public TextView textView;

            public RelativeLayout delete_img;
        }
    }

    class GetCityNameAysTask extends AsyncTask<String, Integer, List<Location>> {

        @Override
        protected List<Location> doInBackground(String... params) {

            List<Location> list = new ArrayList<>();

            Document doc = null;

            String url = "http://sugg.us.search.yahoo.net/gossip-gl-location/?appid=weather&output=xml&command=" + Uri.encode(params[0]);

            try {

                doc = Jsoup.parse(new URL(url), 5000);

                Elements elements = doc.select("s");

                for (int i = 0; i < elements.size(); i++) {

                    String decode_url = elements.get(i).attr("d");

                    String deconde_k=elements.get(i).attr("k");

                    String woied = decode_url.substring(decode_url.indexOf("&woeid=") + 7, decode_url.indexOf("&lon"));

                    String cityName = decode_url.substring(decode_url.lastIndexOf("=") + 1);

                    LogUtil.info( "cityName:" + cityName);

                    Log.e("tool","deconde_k:"+deconde_k);

                    Location location = new Location(woied, cityName,deconde_k);

                    list.add(location);
                }
            } catch (Exception e) {

                Log.e("Adlog", "error:" + e.getMessage());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Location> locations) {
            super.onPostExecute(locations);

            if (locations.size() > 0) {

                Log.e("tool","插入数据："+editext_content);

                insertData(editext_content);

                listViewAdapter.setMlist(locations, false);

                search_tip.setVisibility(View.GONE);

            } else {
                search_tip.setText(getResources().getString(R.string.query_city_empty));
            }

        }
    }

    public void reStartApp(String vaule)
    {

        Intent intent = getApplicationContext().getPackageManager() .getLaunchIntentForPackage(getApplicationContext().getPackageName());

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

    }

}

