package com.lishuanglong.example;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.lishuanglong.view.WheelPicker;

import java.util.ArrayList;
import java.util.List;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Main_Activity";

    private WheelPicker mWheelPicker;
//    private WheelView mWheelPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);

        setContentView(R.layout.activity_main);
        mWheelPicker = findViewById(R.id.wheelPicker);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            data.add("测试：" + i);
        }
        mWheelPicker.setData(data);

//        mWheelPicker.setData(data);
//        mWheelPicker.setCurved(false);//开启弯曲
//        mWheelPicker.setIndicator(true);//开启指示器
//        mWheelPicker.setAtmospheric(false);//显示空气感效果
//        mWheelPicker.setItemSpace(dip2px(10));//设置item之间的间隔
//        mWheelPicker.setCurtain(true);//开启幕布
//        mWheelPicker.setIndicatorSize(dip2px(3));
//        mWheelPicker.setItemTextSize(dip2px(25));
//        mWheelPicker.setSelectedItemPosition(data.size()/2,false);
//        mWheelPicker.setCyclic(false);
//        mWheelPicker.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(WheelView picker, Object data, int position) {
//                Log.d(TAG,"MainActivity --> " + position);
//            }
//        });




    }


    private int dip2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    private int px2dip(int px) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }


    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
