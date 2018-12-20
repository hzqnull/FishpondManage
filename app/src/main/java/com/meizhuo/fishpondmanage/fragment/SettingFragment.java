package com.meizhuo.fishpondmanage.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.meizhuo.fishpondmanage.R;
import com.meizhuo.fishpondmanage.activity.MainActivity;
import com.meizhuo.fishpondmanage.utils.NetworkUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.Random;

import okhttp3.Call;
import okhttp3.Response;

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

    private Button mbtnSetTemp;
    private Button mbtnSetTur;
    private Button mbtnSetOxygen;
    private Button mbtnSetPH;
    private Button mbtnSetWaterDepth;
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
    private EditText metMinWaterDepth;
    private EditText metMaxWaterDepth;

    private Thread updateData; //更新数据的线程

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

        //创建更新数据的线程
        updateData = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if ( !MainActivity.stopUpdateData ) {
                        sendGetOkHttpRequest();
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

    /**
     * 发送请求，得到并设置上下限
     */
    private void sendGetOkHttpRequest() {
        OkHttpUtils.get()
                .url("http://111.230.38.90/test/wyu102")
                .addParams("str", "wyu") //测试用
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError: " + e.toString());
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        setMinMax();
                    }
                });
    }

    /**
     * 传入上下限，设置到界面
     */
    private void setMinMax() {
        Random random = new Random();
        metMaxTemp.setText(String.valueOf(random.nextInt(19) + 10));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mbtnSetTemp = view.findViewById(R.id.btn_set_temperature);
        mbtnSetTur = view.findViewById(R.id.btn_set_turbidity);
        mbtnSetOxygen = view.findViewById(R.id.btn_set_oxygen);
        mbtnSetPH = view.findViewById(R.id.btn_set_ph);
        mbtnSetWaterDepth = view.findViewById(R.id.btn_set_water_depth);
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
        metMinWaterDepth = view.findViewById(R.id.et_water_depth_min);
        metMaxWaterDepth = view.findViewById(R.id.et_water_depth_max);

        mbtnSetTemp.setOnClickListener(this);
        mbtnSetTur.setOnClickListener(this);
        mbtnSetOxygen.setOnClickListener(this);
        mbtnSetPH.setOnClickListener(this);
        mbtnSetWaterDepth.setOnClickListener(this);
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
        metMinWaterDepth.setOnFocusChangeListener(this);
        metMaxWaterDepth.setOnFocusChangeListener(this);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set_temperature: //设置温度警戒值
                sendPostOkHttpRequest("temmin", "temmax", metMinTemp.getText().toString(), metMaxTemp.getText().toString());
                break;
            case R.id.btn_set_turbidity:
                sendPostOkHttpRequest("turmin", "turmax", metMinTur.getText().toString(), metMaxTur.getText().toString());
                break;
            case R.id.btn_set_oxygen:
                sendPostOkHttpRequest("oxymin", "oxymax", metMinOxygen.getText().toString(), metMaxOxygen.getText().toString());
                break;
            case R.id.btn_set_ph:
                sendPostOkHttpRequest("phmin", "phmax", metMinPH.getText().toString(), metMaxPH.getText().toString());
                break;
            case R.id.btn_set_water_depth:

                break;
            case R.id.btn_set_oxygen_pump:

                break;
            case R.id.btn_set_lift_pump:

                break;
            case R.id.btn_set_waterflood_pump:

                break;

            case R.id.btn_set_feed:

                break;
            default:
                break;
        }

    }

    /**
     * 发送网络请求
     *
     * @param min 最小值key
     * @param max 最大值key
     * @param minValue 最小值value
     * @param maxValue 最大值value
     */
    private void sendPostOkHttpRequest(String min, String max, String minValue, String maxValue) {

            if ( !NetworkUtils.isNetworkConnected(getContext()) ) { //检查网络连接
                Toast.makeText(getContext(), "当前没有网络连接", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Double.parseDouble(minValue) < Double.parseDouble(maxValue)) {
            OkHttpUtils.post()
                    .url("http://111.230.38.90/test/wyu102"
                    )
                    .addParams(min, minValue)
                    .addParams(max, maxValue)
                    .build()
                    .execute(new Callback() {
                        @Override
                        public Object parseNetworkResponse(Response response, int id) throws Exception {
                            Log.e(TAG, "parseNetworkResponse: " + response.body().string());
                            return null;
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(getContext(), "设置失败", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onError: " + e.toString());
                        }

                        @Override
                        public void onResponse(Object response, int id) {
                            Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
                            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                if (imm.isActive()) {
                                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); //收起软键盘
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(getContext(), "输入的上下限有误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            MainActivity.stopUpdateData = true; //EditText得到焦点，停止更新数据，防止输入的数据被修改
        } else {
            MainActivity.stopUpdateData = false;//EditText失去焦点，继续更新
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
}
