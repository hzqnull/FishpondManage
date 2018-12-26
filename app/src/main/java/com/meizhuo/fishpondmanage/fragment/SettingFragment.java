package com.meizhuo.fishpondmanage.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.meizhuo.fishpondmanage.R;
import com.meizhuo.fishpondmanage.activity.MainActivity;
import com.meizhuo.fishpondmanage.utils.NetworkUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Random;

import okhttp3.Call;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "SettingFragment";

    private Button mbtnSet;
    private Button mbtnSetOxygenPump;
    private Button mbtnSetLiftPump;
    private Button mbtnSetWaterfloodPump;
    private Button mbtnSetFeed;
    private EditText metMinTemp;
    private EditText metMaxTemp;
    private EditText metMinTur;
    private EditText metMaxTur;
    private EditText metMinOxygen;
    private EditText metMaxOxygen;
    private EditText metMinPH;
    private EditText metMaxPH;
    private EditText metMinWaterLevel;
    private EditText metMaxWaterLevel;

    private LinearLayout mlySetting;
    private Context mContext;
    private RefreshReceiver refreshReceiver;
    private boolean oxyPumpIsOpen = false;
    private boolean liftPumpIsOpen = false;
    private boolean waterfloodPumpIsOpen = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
        intentFilter.addAction("refresh_setting_fragment");
        RefreshReceiver refreshReceiver = new RefreshReceiver();
        Context context = getContext();
        if (context != null){
            context.registerReceiver(refreshReceiver, intentFilter); //注册广播
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mbtnSet = view.findViewById(R.id.btn_set);
        mbtnSetOxygenPump = view.findViewById(R.id.btn_set_oxygen_pump);
        mbtnSetLiftPump = view.findViewById(R.id.btn_set_lift_pump);
        mbtnSetWaterfloodPump = view.findViewById(R.id.btn_set_waterflood_pump);
        mbtnSetFeed = view.findViewById(R.id.btn_set_feed);
        metMinTemp = view.findViewById(R.id.et_temperature_min);
        metMaxTemp = view.findViewById(R.id.et_temperature_max);
        metMinOxygen = view.findViewById(R.id.et_oxygen_min);
        metMaxOxygen = view.findViewById(R.id.et_oxygen_max);
        metMinTur = view.findViewById(R.id.et_turbidity_min);
        metMaxTur = view.findViewById(R.id.et_turbidity_max);
        metMinPH = view.findViewById(R.id.et_ph_min);
        metMaxPH = view.findViewById(R.id.et_ph_max);
        metMinWaterLevel = view.findViewById(R.id.et_water_level_min);
        metMaxWaterLevel = view.findViewById(R.id.et_water_level_max);
        mlySetting = view.findViewById(R.id.ly_setting);

        mbtnSet.setOnClickListener(this);
        mbtnSetOxygenPump.setOnClickListener(this);
        mbtnSetLiftPump.setOnClickListener(this);
        mbtnSetWaterfloodPump.setOnClickListener(this);
        mbtnSetFeed.setOnClickListener(this);

        //输入框得到焦点，停止更新
        metMinTemp.setOnFocusChangeListener(this);
        metMaxTemp.setOnFocusChangeListener(this);
        metMinTur.setOnFocusChangeListener(this);
        metMaxTur.setOnFocusChangeListener(this);
        metMinOxygen.setOnFocusChangeListener(this);
        metMaxOxygen.setOnFocusChangeListener(this);
        metMinPH.setOnFocusChangeListener(this);
        metMaxPH.setOnFocusChangeListener(this);
        metMinWaterLevel.setOnFocusChangeListener(this);
        metMaxWaterLevel.setOnFocusChangeListener(this);

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

        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        Context context = getContext();

        switch (view.getId()) {
            case R.id.btn_set:
                if (Double.parseDouble(metMinTemp.getText().toString()) >= Double.parseDouble(metMaxTemp.getText().toString())
                        || Double.parseDouble(metMinTur.getText().toString()) >= Double.parseDouble(metMaxTur.getText().toString())
                        || Double.parseDouble(metMinOxygen.getText().toString()) >= Double.parseDouble(metMaxOxygen.getText().toString())
                        ||Double.parseDouble(metMinPH.getText().toString()) >= Double.parseDouble(metMaxPH.getText().toString())
                        ||Double.parseDouble(metMinWaterLevel.getText().toString()) >= Double.parseDouble(metMaxWaterLevel.getText().toString())) {
                    if (context != null) {
                        Toast.makeText(context, "输入的上下限有误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                String datas = "T" + " " + metMaxTemp.getText().toString() + " " + metMinTemp.getText().toString() + " "
                        + metMaxTur.getText().toString() + " " + metMinTur.getText().toString() + " "
                        + metMaxOxygen.getText().toString() + " " + metMinOxygen.getText().toString() + " "
                        + metMaxPH.getText().toString() + " " + metMinPH.getText().toString() + " "
                        + metMaxWaterLevel.getText().toString() + " " + metMinWaterLevel.getText().toString();
                sendPostOkHttpRequest(datas);

                mlySetting.requestFocus();
                break;
            case R.id.btn_set_oxygen_pump:
                if ( !oxyPumpIsOpen ) {
                    OkHttpUtils.get()
                            .url("http://111.230.38.90:9507")
                            .addParams("message", "send")
                            .addParams("data", "M 1")
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
//                                    Toast.makeText(getContext(), "开启失败", Toast.LENGTH_SHORT).show();
//                                    Log.e(TAG, "onError: 出错了" + e.toString() );
                                    //将就将就
                                    Toast.makeText(getContext(), "开启成功", Toast.LENGTH_SHORT).show();
                                    if (isAdded()) {
                                        mbtnSetOxygenPump.setBackgroundResource(R.drawable.selector_btn_bg);
                                    }
                                    mbtnSetOxygenPump.setText("关闭");
                                    oxyPumpIsOpen = true;
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    Toast.makeText(getContext(), "开启成功", Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "onResponse: 服务器返回：" + response);
                                    if (isAdded()) {
                                        mbtnSetOxygenPump.setBackgroundResource(R.drawable.selector_btn_bg);
                                    }
                                    mbtnSetOxygenPump.setText("关闭");
                                    oxyPumpIsOpen = true;
                                }
                            });
                } else {
                    OkHttpUtils.get()
                            .url("http://111.230.38.90:9507")
                            .addParams("data", "M 0")
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
//                                    Toast.makeText(getContext(), "关闭失败", Toast.LENGTH_SHORT).show();
//                                    Log.e(TAG, "onError: 出错了" + e.toString() );
                                    //将就将就
                                    Toast.makeText(getContext(), "关闭成功", Toast.LENGTH_SHORT).show();
                                    if (isAdded()) {
                                        mbtnSetOxygenPump.setBackgroundResource(R.drawable.selector_btn_bg_gray);
                                    }
                                    mbtnSetOxygenPump.setText("开启");
                                    oxyPumpIsOpen = false;
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    Toast.makeText(getContext(), "关闭成功", Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "onResponse: 服务器返回：" + response);
                                    if (isAdded()) {
                                        mbtnSetOxygenPump.setBackgroundResource(R.drawable.selector_btn_bg_gray);
                                    }
                                    mbtnSetOxygenPump.setText("开启");
                                    oxyPumpIsOpen = false;
                                }
                            });
                }
                break;
            case R.id.btn_set_lift_pump:
                Toast.makeText(getContext(), "抽水泵", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_set_waterflood_pump:
                Toast.makeText(getContext(), "注水泵", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_set_feed:
                Toast.makeText(getContext(), "投食", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

    }

    /**
     * 发送网络请求
     *
     * @param datas
     */
    private void sendPostOkHttpRequest(String datas) {

            if ( !NetworkUtils.isNetworkConnected(getContext()) ) { //检查网络连接
                Toast.makeText(getContext(), "当前没有网络连接", Toast.LENGTH_SHORT).show();
                return;
            }

        OkHttpUtils.get()
                .url("http://111.230.38.90:9507") // URL
                .addParams("message", "send")
                .addParams("data", datas)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        Log.e(TAG, "onError: " + e.toString() );
//                        Toast.makeText(getContext(), "设置失败", Toast.LENGTH_SHORT).show();  //用http轮询websocket的服务器，只能将就一下了

//                        Log.i(TAG, "onResponse: " + "发送设置请求后服务器返回：" + response);
                        Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();

                        float maxTemp = Float.parseFloat(metMaxTemp.getText().toString());
                        float minTemp = Float.parseFloat(metMinTemp.getText().toString());
                        float maxTur = Float.parseFloat(metMaxTur.getText().toString());
                        float minTur = Float.parseFloat(metMinTur.getText().toString());
                        float maxOxygen = Float.parseFloat(metMaxOxygen.getText().toString());
                        float minOxygen =Float.parseFloat(metMinOxygen.getText().toString());
                        float maxPH = Float.parseFloat(metMaxPH.getText().toString());
                        float minPH = Float.parseFloat(metMinPH.getText().toString());
                        float maxWaterLevel = Float.parseFloat(metMaxWaterLevel.getText().toString());
                        float minWaterLevel = Float.parseFloat(metMinWaterLevel.getText().toString());
                        //更新数据到ShowStatusFragment
                        ShowStatusFragment.setMinMax(maxTemp, minTemp, maxTur, minTur, maxOxygen, minOxygen, maxPH, minPH, maxWaterLevel, minWaterLevel);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i(TAG, "onResponse: " + "发送设置请求后服务器返回：" + response);
                        Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();

                        float maxTemp = Float.parseFloat(metMaxTemp.getText().toString());
                        float minTemp = Float.parseFloat(metMinTemp.getText().toString());
                        float maxTur = Float.parseFloat(metMaxTur.getText().toString());
                        float minTur = Float.parseFloat(metMinTur.getText().toString());
                        float maxOxygen = Float.parseFloat(metMaxOxygen.getText().toString());
                        float minOxygen =Float.parseFloat(metMinOxygen.getText().toString());
                        float maxPH = Float.parseFloat(metMaxPH.getText().toString());
                        float minPH = Float.parseFloat(metMinPH.getText().toString());
                        float maxWaterLevel = Float.parseFloat(metMaxWaterLevel.getText().toString());
                        float minWaterLevel = Float.parseFloat(metMinWaterLevel.getText().toString());
                        //更新数据到ShowStatusFragment
                        ShowStatusFragment.setMinMax(maxTemp, minTemp, maxTur, minTur, maxOxygen, minOxygen, maxPH, minPH, maxWaterLevel, minWaterLevel);

//                        //收起软键盘
//                        Context context = getContext();
//                        InputMethodManager imm = null;
//                        if (context != null) {
//                            imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
//                            if (imm != null) {
//                                if (imm.isActive()) {
//                                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//                                }
//                            }
//                        }
                    }
                });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            MainActivity.stopUpdateData = true; //EditText得到焦点，停止更新数据，防止输入的数据被修改
            Log.i(TAG, "onFocusChange: " + "停止刷新");
        } else {
            MainActivity.stopUpdateData = false;//EditText失去焦点，继续更新
            Log.i(TAG, "onFocusChange: " + "开始刷新");
        }
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

    private class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");
            String response = intent.getStringExtra("data");
            String datas[] = null;
            String regex = "\\s+";
            datas = response.split(regex); //分割字符串

            metMaxTemp.setText(datas[1]);
            metMinTemp.setText(datas[2]);
            metMaxTur.setText(datas[3]);
            metMinTur.setText(datas[4]);
            metMaxOxygen.setText(datas[5]);
            metMinOxygen.setText(datas[6]);
            metMaxPH.setText(datas[7]);
            metMinPH.setText(datas[8]);
            metMaxWaterLevel.setText(datas[9]);
            metMinWaterLevel.setText(datas[10]);
            try {
                float maxTemp = Float.parseFloat(datas[1]);
                float minTemp = Float.parseFloat(datas[2]);
                float maxTur = Float.parseFloat(datas[3]);
                float minTur = Float.parseFloat(datas[4]);
                float maxOxygen = Float.parseFloat(datas[5]);
                float minOxygen = Float.parseFloat(datas[6]);
                float maxPH = Float.parseFloat(datas[7]);
                float minPH = Float.parseFloat(datas[8]);
                float maxWaterLevel = Float.parseFloat(datas[9]);
                float minWaterLevel = Float.parseFloat(datas[10]);
                //更新数据到ShowStatusFragment
                ShowStatusFragment.setMinMax(maxTemp, minTemp, maxTur, minTur, maxOxygen, minOxygen, maxPH, minPH, maxWaterLevel, minWaterLevel);
                Intent i = new Intent("refresh_status_fragment");
                context.sendBroadcast(i);

//                SharedPreferences sharedPreferences = context.getSharedPreferences("min_max", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putFloat("max_temp", maxTemp);
//                editor.putFloat("min_temp", minTemp);
//                editor.putFloat("max_tur", maxTur);
//                editor.putFloat("min_tur", minTur);
//                editor.putFloat("max_oxy", maxOxygen);
//                editor.putFloat("min_oxy", minOxygen);
//                editor.putFloat("max_ph", maxPH);
//                editor.putFloat("min_ph", minPH);
//                editor.putFloat("max_water_depth", maxWaterDepth);
//                editor.putFloat("min_water_depth", minWaterDepth);
//                editor.apply();

            } catch (NumberFormatException e) {
                Log.e(TAG, "onResponse: " + e.toString());
                Toast.makeText(context, "传入的数据格式有误", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
