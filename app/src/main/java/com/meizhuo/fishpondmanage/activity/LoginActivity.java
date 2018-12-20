package com.meizhuo.fishpondmanage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.meizhuo.fishpondmanage.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login: //登录按钮的点击事件
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
            default:
                break;
        }
    }
}
