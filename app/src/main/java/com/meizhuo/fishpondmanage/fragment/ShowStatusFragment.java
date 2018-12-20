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
    private TextView mtvWaterDepth;
    private TextView mtvWaterDepthStatus;
    private TextView mtvLocation;
    private TextView mtvSensorDepth;
    private Button mbtnConnectMonitor;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    Thread updateData; //更新数据线程
    private boolean hasWarn = false;//用来判断是否已有警告通知的flag
    private boolean exit = false; //标记是否fragment被销毁

    public ShowStatusFragment() {
        // Required empty public constructor
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

        //开启定时刷新线程
        updateData = new Thread(new Runnable() {
            @Override
            public void run() {
                while ( !exit ) {
                    if ( !MainActivity.stopUpdateData ) { //判断是否要刷新
                        sendOkHttpRequest();
                        Log.i(TAG, "run: " + "我刷。。。");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        updateData.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exit = true; //fragment被销毁，线程结束
    }

    /**
     * 发送请求并刷新数据
     */
    private void sendOkHttpRequest() {

        OkHttpUtils.get()
                .url("http://111.230.38.90/test/wyu102")
                .addParams("str", "wyu") //参数未定
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
//                        Log.i(TAG, "parseNetworkResponse: " + response.body().string()); //测试用。。。
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError: 出错啦\n" + e.toString() );
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        //和上面的parseNetworkResponse函数的response有什么区别

                        updateUI(); //刷新获得新数据后进行显示
                        //测试用
                        Random random = new Random();
                        int num = random.nextInt(24) + 15;
                        Log.i(TAG, "onResponse: " + "随机温度为" + num);
                        mtvTem.setText(String.valueOf(num));

                        double tem = Double.parseDouble(mtvTem.getText().toString());
                        double tur = Double.parseDouble(mtvTur.getText().toString());
                        double oxy = Double.parseDouble(mtvOxy.getText().toString());
                        double ph = Double.parseDouble(mtvPH.getText().toString());
                        double waterDepth = Double.parseDouble(mtvWaterDepth.getText().toString());
                        judge(tem, tur, oxy, ph, waterDepth); //刷新数据后判断数据是否异常
                    }
                });
    }

    /**
     * 更新数据后显示
     */
    private void updateUI() {
    }

    /**
     * 更新数据后判断是否异常，异常时发通知
     */
    private void judge(double tem, double tur, double oxy, double ph, double waterDepth) {
        Context context = getContext();
        //得到通知管理者
        NotificationManager notificationManager = null;
        if (context != null) {
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (tem > 33) { //数据异常
            if (isAdded()) { //activity重建后，没有context，使用getResource会报错
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTemStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_warn_bg)); //背景变红
                }
                mtvTemStatus.setText("异常");
            }
            //发送“数据异常”通知
            if (context != null) {
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

                if (notificationManager != null && !hasWarn) {
                    notificationManager.notify(1, notification); //弹出通知
                    hasWarn = true;
                }
            }

        } else { //数据正常
            mtvTemStatus.setText("正常");
            if (isAdded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mtvTemStatus.setBackground(getResources().getDrawable(R.drawable.shape_tv_bg)); //activity重建后，没有context，使用getResource会报错
                }
            }
            if (notificationManager != null) {
                notificationManager.cancel(1); //数据恢复正常，取消通知
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
        mtvWaterDepth = view.findViewById(R.id.tv_water_depth_value);
        mtvWaterDepthStatus = view.findViewById(R.id.tv_water_depth_state);
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
