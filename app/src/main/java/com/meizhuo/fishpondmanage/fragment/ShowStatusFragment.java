package com.meizhuo.fishpondmanage.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meizhuo.fishpondmanage.R;
import com.meizhuo.fishpondmanage.activity.MainActivity;
import com.meizhuo.fishpondmanage.utils.NetworkUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowStatusFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowStatusFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView mtvTem;
    private TextView mtvTemStatus;
    private TextView mtvTur;
    private TextView mtvTurStatus;
    private TextView mtvOxy;
    private TextView mtvOxyStatus;
    private TextView mtvPH;
    private TextView mtvPHStatus;
    private TextView mtvWaterLevel;
    private TextView mtvWaterLevelStatus;
    private TextView mtvLocation;
    private TextView mtvSensorDepth;
    private Button mbtnConnectMonitor;

    private RefreshReceiver refreshReceiver;

    public static float mMaxTemp, mMinTemp, mMaxTur, mMinTur, mMaxOxy, mMinOxy, mMaxPH, mMinPH, mMaxWaterLevel, mMinWaterLevel;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private boolean hasWarn = false;//用来判断是否已有警告通知的flag

    public ShowStatusFragment() {
        // Required empty public constructor
    }

    /**
     * 设置上下限
     *
     * @param maxTemp
     * @param minTemp
     * @param maxTur
     * @param minTur
     * @param maxOxy
     * @param minOxy
     * @param maxPH
     * @param minPH
     * @param maxWaterLevel
     * @param minWaterLevel
     */
    public static void setMinMax(float maxTemp, float minTemp, float maxTur, float minTur, float maxOxy, float minOxy,
                                 float maxPH, float minPH, float maxWaterLevel, float minWaterLevel) {
        mMaxTemp = maxTemp;
        mMinTemp = minTemp;
        mMaxTur = maxTur;
        mMinTur = minTur;
        mMaxOxy = maxOxy;
        mMinOxy = minOxy;
        mMaxPH = maxPH;
        mMinPH = minPH;
        mMaxWaterLevel = maxWaterLevel;
        mMinWaterLevel = minWaterLevel;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowStatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowStatusFragment newInstance(String param1, String param2) {
        ShowStatusFragment fragment = new ShowStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("refresh_status_fragment");
        refreshReceiver = new RefreshReceiver();
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(refreshReceiver, intentFilter); //注册广播
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(refreshReceiver); //取消注册广播
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("data");
            if (response != null && (!response.equals(""))) {
                String datas[] = null;
                String regex = "\\s+";
                datas = response.split(regex); //分割字符串

                mtvSensorDepth.setText(datas[1]);
                mtvTem.setText(datas[2]);
                mtvTur.setText(datas[3]);
                mtvOxy.setText(datas[4]);
                mtvPH.setText(datas[5]);
                mtvWaterLevel.setText(datas[6]);
                try {
                    float depth = Float.parseFloat(datas[1]);
                    float tem = Float.parseFloat(datas[2]);
                    float tur = Float.parseFloat(datas[3]);
                    float oxy = Float.parseFloat(datas[4]);
                    float ph = Float.parseFloat(datas[5]);
                    float lev = Float.parseFloat(datas[6]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "onResponse: " + e.toString());
                    Context c = getContext();
                    if (c != null) {
                        Toast.makeText(context, "传入的数据格式有误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            judge(Float.valueOf(mtvTem.getText().toString()), Float.valueOf(mtvTur.getText().toString()),
                    Float.valueOf(mtvOxy.getText().toString()), Float.valueOf(mtvPH.getText().toString()),
                    Float.valueOf(mtvWaterLevel.getText().toString()));
        }
    }

    /**
     * 更新数据后判断是否异常，异常时发通知
     */
    private void judge(float tem, float tur, float oxy, float ph, float waterLevel) {
        Context context = getContext();
        //得到通知管理者
        NotificationManager notificationManager = null;
        if (context != null) {
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        judgeTemp(tem, context, notificationManager);
        judgeTur(tur, context, notificationManager);
        judgeOxy(oxy, context, notificationManager);
        judgePH(ph, context, notificationManager);
        judgeWaterLevel(waterLevel, context, notificationManager);

    }

    private void judgeWaterLevel(float waterLevel, Context context, NotificationManager notificationManager) {
        if (waterLevel > mMaxWaterLevel || waterLevel < mMinWaterLevel) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvWaterLevelStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvWaterLevelStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null && notificationManager != null && !hasWarn) {
                makeNotification(notificationManager, context);
            }
        } else { //数据正常
            mtvWaterLevelStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvWaterLevelStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
//                notificationManager.cancel(1); //数据恢复正常，取消通知
            }
            hasWarn = false;
        }
    }

    private void judgePH(float ph, Context context, NotificationManager notificationManager) {
        if (ph > mMaxPH || ph < mMinPH) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvPHStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvPHStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null && notificationManager != null && !hasWarn) {
                makeNotification(notificationManager, context);
            }
        } else { //数据正常
            mtvPHStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvPHStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
//                notificationManager.cancel(1); //数据恢复正常，取消通知
            }
            hasWarn = false;
        }
    }

    private void judgeOxy(float oxy, Context context, NotificationManager notificationManager) {
        if (oxy > mMaxOxy || oxy < mMinOxy) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvOxyStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvOxyStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null && notificationManager != null && !hasWarn) {
                makeNotification(notificationManager, context);
            }
        } else { //数据正常
            mtvOxyStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvOxyStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
//                notificationManager.cancel(1); //数据恢复正常，取消通知
            }
            hasWarn = false;
        }
    }

    private void judgeTur(float tur, Context context, NotificationManager notificationManager) {
        if (tur > mMaxTur || tur < mMinTur) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTurStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvTurStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null && notificationManager != null && !hasWarn) {
                makeNotification(notificationManager, context);
            }
        }  else { //数据正常
            mtvTurStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTurStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
//                notificationManager.cancel(1); //数据恢复正常，取消通知
            }
            hasWarn = false;
        }
    }

    private void makeNotification(NotificationManager notificationManager, Context context) {
        //创建一个通知
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("鱼塘养殖系统数据异常")
                .setContentText("请尽快处理")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .build();
                notificationManager.notify(1, notification); //弹出通知
        hasWarn = true;
    }

    private void judgeTemp(float tem, Context context, NotificationManager notificationManager) {
        Log.i(TAG, "judgeTemp: MaxTemp:" + mMaxTemp);
        Log.i(TAG, "judgeTemp: CurTemp:" + tem);
        if (tem > mMaxTemp || tem < mMinTemp) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTemStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvTemStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null && notificationManager != null && !hasWarn) {
                makeNotification(notificationManager, context);
            }
        } else { //数据正常
            mtvTemStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTemStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
//                notificationManager.cancel(1); //数据恢复正常，取消通知
            }
            hasWarn = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_status, container, false);

        mtvTem = view.findViewById(R.id.tv_temperature_value);
        mtvTemStatus = view.findViewById(R.id.tv_temperature_state);
        mtvTur = view.findViewById(R.id.tv_turbidity_value);
        mtvTurStatus = view.findViewById(R.id.tv_turbidity_state);
        mtvOxy = view.findViewById(R.id.tv_oxygen_value);
        mtvOxyStatus = view.findViewById(R.id.tv_oxygen_state);
        mtvPH = view.findViewById(R.id.tv_ph_value);
        mtvPHStatus = view.findViewById(R.id.tv_ph_state);
        mtvWaterLevel = view.findViewById(R.id.tv_water_level_value);
        mtvWaterLevelStatus = view.findViewById(R.id.tv_water_level_state);
        mtvLocation = view.findViewById(R.id.tv_location_value);
        mtvSensorDepth = view.findViewById(R.id.tv_sensor_depth);
        mbtnConnectMonitor = view.findViewById(R.id.btn_connect_monitor);

        mbtnConnectMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction("connect_monitor"); //连接视频监控
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onFragmentInteraction(String str);
    }


}
