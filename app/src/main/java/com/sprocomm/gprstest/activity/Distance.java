package com.sprocomm.gprstest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sprocomm.gprstest.R;
import com.sprocomm.gprstest.utils.LogUtils;
import com.sprocomm.gprstest.utils.RssiUtil;


/**
 * Created by yuanbin.ning on 2017/6/9.
 */

public class Distance extends Activity {

    private EditText editText;
    private TextView disView;
    private Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance);
        initViews();
    }

    private void initViews() {
        editText = (EditText) findViewById(R.id.riss_edit);
        disView = (TextView) findViewById(R.id.distance_view);
        okBtn = (Button) findViewById(R.id.ok);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int riss = Integer.parseInt(editText.getText().toString());
                        double dis = RssiUtil.getDistance(riss);
                        disView.setText(String.format(getString(R.string.distance_value), dis));
                    } catch (NumberFormatException e) {
                    }
                } else {
                    disView.setText("距离约?米");
                }
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("rissValue", editText.getText().toString());
                LogUtils.d("Distance", editText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        int value = getIntent().getIntExtra("value", 0);
        if (value < 0) {
            editText.setText(String.valueOf(0 - value));
        }
    }
}
