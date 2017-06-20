package com.sprocomm.gprstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sprocomm.gprstest.Contants.GlobalContants;
import com.sprocomm.gprstest.libs.Config;

public class GpsMainActivity extends Activity implements View.OnClickListener {

    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;
    private Spinner spServer;
    private String resultInfo;
    private static final String [] countriesStr = {"www.rocolock.com","web.dola520.com"};
    private static final String[]  mesServiceStr = {Config.WEB_SERVICE_URL,Config.WEB_SERVICE_URL_TEST};
    private ArrayAdapter<String> adapter;
    private ProgressBar pbLogin;
    private CheckBox cbSave;
    private Spinner spMesServer;
    private ArrayAdapter<String> mesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_activity_main);
        initView();
        initData();
    }
    private void initView(){
        etUsername = (EditText)findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        SharedPreferences sp =  getSharedPreferences("save_password", Context.MODE_PRIVATE);
        spServer = (Spinner)findViewById(R.id.sp_spin);
        spMesServer = (Spinner) findViewById(R.id.sp_spin_mes);
        btLogin = (Button) findViewById(R.id.btn_login);
        pbLogin = (ProgressBar) findViewById(R.id.pb_login);
        pbLogin.setVisibility(View.INVISIBLE);
        cbSave = (CheckBox)findViewById(R.id.cb_save);
        btLogin.setOnClickListener(this);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,countriesStr);
        mesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mesServiceStr);
        spMesServer.setAdapter(mesAdapter);

        spServer.setAdapter(adapter);
        String username  = sp.getString("username",null);
        String password = sp.getString("password",null);
        Boolean checkbox = sp.getBoolean("isCheck",false);
        if(username != null && password != null){
            etUsername.setText(username);
            etUsername.setSelection(username.length());
            etPassword.setText(password);
            etPassword.setSelection(password.length());
        }
        if(checkbox){
            cbSave.setChecked(true);
        }
    }
    private void initData(){
        getDataFromServer();
    }
    private void showToast(String toast){
        Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();
    }

    private void login()
    {
        String str1 = this.etPassword.getText().toString().trim();
        String str2 = this.etUsername.getText().toString().trim();
        if ((TextUtils.isEmpty(str1)) || (TextUtils.isEmpty(str2)))
        {
            showToast("手机号码或密码不能为空");
            return;
        }
        pbLogin.setVisibility(View.VISIBLE);
        btLogin.setEnabled(false);
        String  url = "http://"+ spServer.getSelectedItem().toString()+"/cxlogin.aspx?data1=" + str2 + "&data2=" + str1;
        HttpUtils utils1 = new HttpUtils();
        utils1.send(HttpRequest.HttpMethod.POST, url,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String  reponse = responseInfo.result;
                        if ("fail".equals(reponse)) {
                            pbLogin.setVisibility(View.INVISIBLE);
                            btLogin.setEnabled(true);
                            showToast("登陆失败，请检查用户名或密码是否正确！");
                            return;
                        }
                        Intent intent = new Intent(GpsMainActivity.this,LockActivity.class);
                        intent.putExtra("key",reponse);
                        intent.putExtra("url","http://"+spServer.getSelectedItem().toString() + "/cx.aspx?data1=");
                        intent.putExtra("mesUrl","http://" + spMesServer.getSelectedItem().toString() +"/MES_Web_Services.asmx");
                        pbLogin.setVisibility(View.INVISIBLE);
                        btLogin.setEnabled(true);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        pbLogin.setVisibility(View.INVISIBLE);
                        btLogin.setEnabled(true);
                        showToast("网络连接失败，请检查网络是否连接成功！");
                    }
                });
    }


    /**
     * 从服务器获取数据
     */
    private void getDataFromServer() {
        HttpUtils utils = new HttpUtils();

        // 使用xutils发送请求
        utils.send(HttpRequest.HttpMethod.GET, GlobalContants.CATEGORIES_URL,
                new RequestCallBack<String>() {

                    // 访问成功, 在主线程运行
                    @Override
                    public void onSuccess(ResponseInfo responseInfo) {
                        String result = (String) responseInfo.result;
                        resultInfo = result;
                        System.out.println("返回结果:" + result);
                       // parseData(result);
                    }

                    // 访问失败, 在主线程运行
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(GpsMainActivity.this, msg, Toast.LENGTH_SHORT)
                                .show();
                        error.printStackTrace();
                    }
                });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                SharedPreferences sp =  getSharedPreferences("save_password", Context.MODE_PRIVATE);
                SharedPreferences.Editor et = sp.edit();
                if(cbSave.isChecked()){
                    et.putString("username",etUsername.getText().toString());
                    et.putString("password",etPassword.getText().toString());
                }else{
                    et.putString("username",null);
                    et.putString("password",null);
                }
                et.putBoolean("isCheck",cbSave.isChecked());
                et.commit();
                login();
                break;
        }
    }
}
