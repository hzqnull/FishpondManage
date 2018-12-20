package com.meizhuo.fishpondmanage.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.meizhuo.fishpondmanage.R;
import com.meizhuo.fishpondmanage.fragment.MeFragment;
import com.meizhuo.fishpondmanage.fragment.SettingFragment;
import com.meizhuo.fishpondmanage.fragment.ShowStatusFragment;
import com.meizhuo.fishpondmanage.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements
        ShowStatusFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener,
        View.OnClickListener{

    private ViewPager viewPager;
    private List<Fragment> viewPagerList;
    private ImageView tabStatusImg;
    private ImageView tabSettingImg;
    private ImageView tabMeImg;
    private TextView tabStatusText;
    private TextView tabSettingText;
    private TextView tabMeText;
    private RelativeLayout tabStatus;
    private RelativeLayout tabSetting;
    private RelativeLayout tabMe;
    private TextView tvNoNetwork;

    private ShowStatusFragment showStatusFragment;
    private SettingFragment settingFragment;
    private MeFragment meFragment;

    private NetworkChangeReceiver receiver;

    public static boolean stopUpdateData; //是否停止更新数据的flag

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver); //取消注册网络变化监听器
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        //为ViewPager添加item
        showStatusFragment = new ShowStatusFragment();
        settingFragment = new SettingFragment();
        meFragment = new MeFragment();
        viewPagerList = new ArrayList<>();
        viewPagerList.add(showStatusFragment);
        viewPagerList.add(settingFragment);
        viewPagerList.add(meFragment);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return viewPagerList.size();
            }

            @Override
            public Fragment getItem(int i) {
                return viewPagerList.get(i);
            }
        });//为ViewPager设置适配器
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        tabStatusImg.setImageResource(R.drawable.ic_status_selected);
                        tabSettingImg.setImageResource(R.drawable.ic_setting);
                        tabMeImg.setImageResource(R.drawable.ic_me);
                        tabStatusText.setTextColor(Color.parseColor("#018ff1"));
                        tabSettingText.setTextColor(Color.parseColor("#dbdbdb"));
                        tabMeText.setTextColor(Color.parseColor("#dbdbdb"));
                        break;
                    case 1:
                        tabStatusImg.setImageResource(R.drawable.ic_status);
                        tabSettingImg.setImageResource(R.drawable.ic_setting_selected);
                        tabMeImg.setImageResource(R.drawable.ic_me);
                        tabStatusText.setTextColor(Color.parseColor("#dbdbdb"));
                        tabSettingText.setTextColor(Color.parseColor("#018ff1"));
                        tabMeText.setTextColor(Color.parseColor("#dbdbdb"));
                        break;
                    case 2:
                        tabStatusImg.setImageResource(R.drawable.ic_status);
                        tabSettingImg.setImageResource(R.drawable.ic_setting);
                        tabMeImg.setImageResource(R.drawable.ic_me_selected);
                        tabStatusText.setTextColor(Color.parseColor("#dbdbdb"));
                        tabSettingText.setTextColor(Color.parseColor("#dbdbdb"));
                        tabMeText.setTextColor(Color.parseColor("#018ff1"));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });//为ViewPager添加当前item变换监听器
        viewPager.setOffscreenPageLimit(viewPagerList.size());//为ViewPager设置缓存页数，若不设置，默认的为2，就会有一个会被重绘

        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        //启动程序默认选中第一个item
        viewPager.setCurrentItem(0);
        tabStatusImg.setImageResource(R.drawable.ic_status_selected);
        tabSettingImg.setImageResource(R.drawable.ic_setting);
        tabMeImg.setImageResource(R.drawable.ic_me);
        tabStatusText.setTextColor(Color.parseColor("#018ff1"));
        tabSettingText.setTextColor(Color.parseColor("#dbdbdb"));
        tabMeText.setTextColor(Color.parseColor("#dbdbdb"));

        if ( !NetworkUtils.isNetworkConnected(this) ) { //检查网络连接
            tvNoNetwork.setVisibility(View.VISIBLE); //显示当前无网络
        }

        //注册广播监听器，监听网络变化
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        viewPager = findViewById(R.id.view_pager);
        tabStatusImg = findViewById(R.id.tab_status_img);
        tabSettingImg = findViewById(R.id.tab_setting_img);
        tabMeImg = findViewById(R.id.tab_me_img);
        tabStatusText = findViewById(R.id.tab_status_tv);
        tabSettingText = findViewById(R.id.tab_setting_tv);
        tabMeText = findViewById(R.id.tab_me_tv);
        tabStatus = findViewById(R.id.tab_status);
        tabSetting = findViewById(R.id.tab_setting);
        tabMe = findViewById(R.id.tab_me);
        tvNoNetwork = findViewById(R.id.tv_no_network);

        tabStatus.setOnClickListener(this);
        tabSetting.setOnClickListener(this);
        tabMe.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab_status:
                if (showStatusFragment == null) {
                    showStatusFragment = new ShowStatusFragment();
                }
                viewPager.setCurrentItem(0);
                break;
            case R.id.tab_setting:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                }
                viewPager.setCurrentItem(1);
                break;
            case R.id.tab_me:
                if (meFragment == null) {
                    meFragment = new MeFragment();
                }
                viewPager.setCurrentItem(2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onFragmentInteraction(String str) {
        switch (str) {
            case "connect_monitor":
                if (!NetworkUtils.isNetworkConnected(this)) {
                    Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
                    break;
                }
                Toast.makeText(this, "视频连接", Toast.LENGTH_SHORT).show();
                break;
            case "set_temperature":
                Toast.makeText(this, "设置温度警戒值", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    /**
     * 监听网络变化
     */
    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(MainActivity.this)) { //有网
                tvNoNetwork.setVisibility(View.GONE); //无网络提示消失
                Log.i(TAG, "onReceive: " + "开始刷新");
                MainActivity.stopUpdateData = false;//定时刷新开启
            } else { //没网
                tvNoNetwork.setVisibility(View.VISIBLE);//顶部显示无网络
                Log.i(TAG, "onReceive: " + "停止刷新");
                MainActivity.stopUpdateData = true;//定时刷新关闭
            }
        }
    }
}
