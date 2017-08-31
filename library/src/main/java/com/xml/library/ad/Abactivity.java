//package com.xml.library.ad;
//
//import android.annotation.TargetApi;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//
///**
// * Created by xlc on 2017/2/10.
// */
//public class Abactivity extends Activity {
//
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static void startAbActivity(Context context) {
//
//        Intent intent = new Intent(context, Abactivity.class);
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        context.startActivity(intent);
//
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        finish();
//
//
//    }
//}
