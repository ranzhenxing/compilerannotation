package com.tianque.compilerannotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tianque.libannotations.BindView;
import com.tianque.libapi.InjectHelper;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv_test)
    TextView tvTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectHelper.inject(this);
        tvTest.setText("注解后设置的名字");
    }
}
