package com.sprocomm.gprstest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sprocomm.gprstest.libs.Config;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Date;

public class LockActivity extends Activity implements View.OnClickListener, TextWatcher {

    private Button btnResetMotor;
    private EditText etImei;
    private Button btnSearch;
    private Button btnXing;
    private Button btnOpenLock;
    private Button btnVoice;
    private String mkey;
    private static String mUri = "http://web.dola520.com/cs.aspx?data1=";
    private static String mMesUri ="http://192.168.8.13:82/MES_Web_Services.asmx";
    private static final int RESULT_FROM_CAPTURE_ACTIVITY = 1;
    private static final String RETURN_BYCLE_ID = "return_bycle_id";
    private TextView tvSearchLock;
    private Button btnOpenLockPass;
    private Button btnOpenLockFail;
    private Button btnVoicePass;
    private Button btnVoiceFail;
    private Button btnResetMotorPass;
    private Button btnResetMotorFail;
    private static final int NO_CLICK = 0;
    private static int isOpenLock = NO_CLICK;
    private static int isVoice = NO_CLICK;
    private static int isResetMotor = NO_CLICK;
    private static int isMoveMode = NO_CLICK;
    private static final int TEST_PASS = 1;
    private static final int TEST_FAIL = 2;

    private static final int SENDING = 1;
    private static final int SENDING_OK = 2;
    private static final int SENDING_NO = 3;
    private static int lockCount = 0;
    private static int voiceCount =0;
    private static int motorCount = 0;
    private static int moveCount = 0;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENDING:
                    showToast("正在发送至MES服务器，请稍等...");
                    break;
                case SENDING_OK:
                    tvResult.setText(msg.obj.toString());
                    break;
                case SENDING_NO:
                    showToast("发送失败，请检查网络和测试网址是否正确");
                    break;
            }
        }
    };
    private TextView tvResult;
    private TextView tvClickLock;
    private TextView tvClickVoice;
    private TextView tvClickMotor;
    private TextView tvClickMove;
    private Button btnMoveMode;
    private Button btnMovePass;
    private Button btnMoveFail;
    private SharedPreferences sp;
    private Button btnFlush;
    private TextView tvImeiValue;
    private String mImei = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        sp= getSharedPreferences("save_imei",Context.MODE_PRIVATE);
        initView();
        initEvent();
        mkey = getIntent().getStringExtra("key");
        mUri = getIntent().getStringExtra("url");
        mMesUri = getIntent().getStringExtra("mesUrl");
    }
    private void initView(){
        etImei = (EditText) findViewById(R.id.et_imei);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnXing = (Button) findViewById(R.id.btn_xing);
        btnOpenLock = (Button)findViewById(R.id.btn_open_lock);
        btnVoice = (Button)findViewById(R.id.btn_voice);
        btnResetMotor   = (Button) findViewById(R.id.btn_reset_motor);
        tvSearchLock = (TextView) findViewById(R.id.tv_search_lock);
        btnOpenLockPass = (Button)findViewById(R.id.btn_open_lock_pass);
        btnOpenLockFail = (Button) findViewById(R.id.btn_open_lock_fail);
        btnVoicePass  =(Button) findViewById(R.id.btn_voice_pass);
        btnVoiceFail = (Button) findViewById(R.id.btn_voice_fail);
        btnResetMotorPass =(Button) findViewById(R.id.btn_reset_motor_pass);
        btnResetMotorFail  = (Button) findViewById(R.id.btn_reset_motor_fail);
        tvResult = (TextView) findViewById(R.id.tv_mes_return);
        btnMoveMode = (Button) findViewById(R.id.btn_move_mode);
        btnMovePass= (Button) findViewById(R.id.btn_move_mode_pass);
        btnMoveFail = (Button) findViewById(R.id.btn_move_mode_fail);
        tvClickLock =  (TextView) findViewById(R.id.tv_lock_count);
        tvClickVoice = (TextView) findViewById(R.id.tv_voice_count);
        tvClickMotor = (TextView) findViewById(R.id.tv_motor_count);
        tvClickMove = (TextView) findViewById(R.id.tv_move_mode_count);
        btnFlush = (Button) findViewById(R.id.btn_flush);
        tvImeiValue = (TextView) findViewById(R.id.tv_imei_value);
        buttonEnableFalse();
        String  imeiString =  sp.getString("imei_toString",null);
        if(imeiString != null){
             mImei = parseImei(imeiString);
        }
    }
    private void buttonEnableFalse(){
        btnOpenLockPass.setEnabled(false);
        btnOpenLockFail.setEnabled(false);
        btnVoicePass.setEnabled(false);
        btnVoiceFail.setEnabled(false);
        btnResetMotorPass.setEnabled(false);
        btnResetMotorFail.setEnabled(false);
        btnMovePass.setEnabled(false);
        btnMoveFail.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        flushWorkMode();
    }

    private void initEvent(){
        etImei.addTextChangedListener(this);
        btnSearch.setOnClickListener(this);
        btnXing.setOnClickListener(this);
        btnOpenLock.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        btnResetMotor.setOnClickListener(this);
        btnOpenLockPass.setOnClickListener(this);
        btnOpenLockFail.setOnClickListener(this);
        btnVoicePass.setOnClickListener(this);
        btnVoiceFail.setOnClickListener(this);
        btnResetMotorPass.setOnClickListener(this);
        btnResetMotorFail.setOnClickListener(this);
        btnMoveMode.setOnClickListener(this);
        btnMovePass.setOnClickListener(this);
        btnMoveFail.setOnClickListener(this);
        btnFlush.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_search:
                send(1);
                break;
            case R.id.btn_xing:
                startActivityForResult(new Intent(this, CaptureActivity.class), RESULT_FROM_CAPTURE_ACTIVITY);
                break;
            case R.id.btn_open_lock:
                send(2);
                break;
            case R.id.btn_voice:
                send(3);
                break;
            case R.id.btn_move_mode:
                if(lockCount < 5 && voiceCount <= 1){
                    showToast("请在测试完开锁，蜂鸣器后执行！");
                    break;
                }
                send(4);
                break;
            case R.id.btn_reset_motor:
                send(5);
                break;
            case R.id.btn_open_lock_pass:
                isOpenLock = TEST_PASS;
                btnOpenLockPass.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                btnOpenLockFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                break;
            case R.id.btn_open_lock_fail:
                isOpenLock = TEST_FAIL;
                btnOpenLockPass.setBackgroundColor(getResources().getColor(android.R.color.white));
                btnOpenLockFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case R.id.btn_voice_pass:
                isVoice = TEST_PASS;
                btnVoicePass.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                btnVoiceFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                break;
            case R.id.btn_voice_fail:
                isVoice = TEST_FAIL;
                btnVoicePass.setBackgroundColor(getResources().getColor(android.R.color.white));
                btnVoiceFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case R.id.btn_reset_motor_pass:
                isResetMotor = TEST_PASS;
                btnResetMotorPass.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                btnResetMotorFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                break;
            case R.id.btn_reset_motor_fail:
                isResetMotor = TEST_FAIL;
                btnResetMotorPass.setBackgroundColor(getResources().getColor(android.R.color.white));
                btnResetMotorFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case R.id.btn_move_mode_pass:
                isMoveMode = TEST_PASS;
                btnMovePass.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                btnMoveFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                break;
            case R.id.btn_move_mode_fail:
                isMoveMode = TEST_FAIL;
                btnMovePass.setBackgroundColor(getResources().getColor(android.R.color.white));
                btnMoveFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case R.id.btn_flush:
                flushWorkMode();
                break;
        }
        int isPass = -1;
        if(isOpenLock != NO_CLICK && isVoice != NO_CLICK && isMoveMode != NO_CLICK){
            if(tvSearchLock.getText() != null && isOpenLock == TEST_PASS  && isVoice == TEST_PASS && isMoveMode == TEST_PASS) {
                isPass = 1;
            }else if(tvSearchLock.getText() == null || isOpenLock == TEST_FAIL || isVoice ==TEST_FAIL || isMoveMode == TEST_FAIL){
                isPass =0;
            }else{
                isPass =-1;
            }
        }
        final int isFinalPass = isPass;
        if(isFinalPass != -1) {
            try {
                new Thread() {
                    @Override
                    public void run() {
                        mHandler.sendEmptyMessage(SENDING);
                        getRemoteInfo(etImei.getText().toString(),isFinalPass);

                    }
                }.start();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    private void showToast(String toast){
        Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();
    }

    private void flushWorkMode(){
        String getEtImei = etImei.getText().toString();
        if(mImei != null && getEtImei !=null  && mImei.equalsIgnoreCase(getEtImei)){
            int  workMode = sp.getInt("work_mode",-1);
            if(workMode  == 1){
                tvImeiValue.setText("正常模式");
            }else if(workMode == 0){
                tvImeiValue.setText("运输模式");
            }else{
                tvImeiValue.setText("未获得在线的工作模式");
            }
        }else{
            Toast.makeText(this,"请检查imei是否在线",Toast.LENGTH_SHORT).show();
        }
    }

    private String parseCode(String paramString,int paramInt){
        return mUri + paramString + "&data2=" + paramInt + "&data3=" + mkey;
    }

    public void send(final int paramInt){
        String str = this.etImei.getText().toString().trim();
        if ((TextUtils.isEmpty(str)) || (str.length() < 12)){
            showToast("设备编号不能为空或少于12位");
            return;
        }
        String localUri = parseCode(str,paramInt);
        HttpUtils utils1 = new HttpUtils();
        utils1.send(HttpRequest.HttpMethod.POST, localUri,
                new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String str = responseInfo.result;
                        Log.d("wjb sprocomm",str);
                        if(paramInt == 1 && (!"fail".equals(str))){
                             tvSearchLock.setText(str);
                        }else if ("指令保存成功".equals(str)) {
                            switch (paramInt){
                                case 2:
                                    isOpenLock = NO_CLICK;
                                    btnOpenLockPass.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    btnOpenLockFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    lockCount++;
                                    tvClickLock.setText("" + lockCount);
                                    if(lockCount > 4){
                                        btnOpenLockPass.setEnabled(true);
                                        btnOpenLockFail.setEnabled(true);
                                    }
                                    break;
                                case 3:
                                    isVoice = NO_CLICK;
                                    btnVoicePass.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    btnVoiceFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    voiceCount++;
                                    tvClickVoice.setText("" +voiceCount);
                                    if(voiceCount >= 1){
                                        btnVoicePass.setEnabled(true);
                                        btnVoiceFail.setEnabled(true);
                                    }
                                    break;
                                case 4:
                                    isMoveMode = NO_CLICK;
                                    btnMovePass.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    btnMoveFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    btnMovePass.setEnabled(true);
                                    btnMoveFail.setEnabled(true);
                               /*     btnOpenLock.setEnabled(false);
                                    btnVoice.setEnabled(false);
                                    btnResetMotor.setEnabled(false);*/
                                    moveCount++;
                                    tvClickMove.setText("" + moveCount);
                                    break;
                                case 5:
                                    isResetMotor = NO_CLICK;
                                    btnResetMotorPass.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    btnResetMotorFail.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    motorCount++;
                                    tvClickMotor.setText("" + motorCount);
                                    btnResetMotorPass.setEnabled(true);
                                    btnResetMotorFail.setEnabled(true);
                                    break;
                            }
                            if(paramInt == 2){
                                showToast("请在开锁5次后判断是否有效，有效选PASS，无效选FAIL");
                            }else {
                                showToast("请在操作后判断是否有效，有效选PASS,无效选FAIL");
                            }
                        }else {
                            isFailed(paramInt);
                        }
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {
                        isFailed(paramInt);
                    }
        });
    }

    private void isFailed(int paramInt){
        switch (paramInt) {
            case 1:
                tvSearchLock.setText(null);
                break;
            case 2:
                isOpenLock = TEST_FAIL;
                btnOpenLockFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case 3:
                isVoice = TEST_FAIL;
                btnVoiceFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case 4:
                isResetMotor = TEST_FAIL;
                btnResetMotorFail.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        showToast("发送指令失败，请检查锁是否在线及账号是否允许开锁...");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_FROM_CAPTURE_ACTIVITY:
                if (data == null) {
                    return;
                }
                String returnBycleId = data.getStringExtra(RETURN_BYCLE_ID);
                parseImei(returnBycleId);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        isVoice = NO_CLICK;
        isResetMotor = NO_CLICK;
        isOpenLock = NO_CLICK;
        isMoveMode = NO_CLICK;
        moveCount = 0;
        lockCount = 0;
        voiceCount = 0;
        motorCount = 0;
        tvClickLock.setText("" + lockCount);
        tvClickVoice.setText("" + voiceCount);
        tvClickMotor.setText("" + motorCount);
        tvClickMove.setText("" + moveCount);
        tvSearchLock.setText(null);
        btnOpenLock.setEnabled(true);
        btnVoice.setEnabled(true);
        btnResetMotor.setEnabled(true);
        btnOpenLockPass.setEnabled(false);
        btnOpenLockFail.setEnabled(false);
        btnVoicePass.setEnabled(false);
        btnVoiceFail.setEnabled(false);
        btnResetMotorPass.setEnabled(false);
        btnResetMotorFail.setEnabled(false);
        btnMovePass.setEnabled(false);
        btnMoveFail.setEnabled(false);
        btnOpenLockPass.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnOpenLockFail.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnVoicePass.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnVoiceFail.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnResetMotorPass.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnResetMotorFail.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnMovePass.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnMoveFail.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void getRemoteInfo(String imei,int flag) {
        String methodName = "SubGPSTest";
        SoapObject rpc = new SoapObject(Config.NAME_SPACE, methodName);
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
//        rpc.addProperty("sn", mImei.substring(mImei.length() - 12, mImei.length()));
        rpc.addProperty("sn", imei);//"2008G16000100"
        rpc.addProperty("testUser", "GPSTestUser");
        rpc.addProperty("flag", flag);
        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(mMesUri);
        try {
            // 调用WebService
            transport.call(Config.NAME_SPACE + "/" + methodName, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Object object = envelope.getResponse();
        } catch (SoapFault e1) {
            e1.printStackTrace();
        }
        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        // 获取返回的结果
        String result =null;
        if(object !=null && object.getProperty(0) != null) {
            result = object.getProperty(0).toString();
        }
        // 将WebService返回的结果显示在TextView中
        Log.d("wjb sprocomm", "result: " + result);
        if(result != null){
            Message msg  = new Message();
            msg.what = SENDING_OK;
            msg.obj = result;
            mHandler.sendMessage(msg);
        }else{
            mHandler.sendEmptyMessage(SENDING_NO);
        }
    }
    private String parseImei(String returnBycleId){
        String lastImei = null;
        if (returnBycleId != null && returnBycleId.length() >= 15) {
            String getTwoImei = returnBycleId.substring(0,2);
            if(getTwoImei.equalsIgnoreCase("35")){
                lastImei = "0630" + returnBycleId.substring(7,15);
            }else if(getTwoImei.equalsIgnoreCase("86")){
                lastImei = "05662" + returnBycleId.substring(8,15);
            }else{
                lastImei = returnBycleId.substring(3,15);
            }
            etImei.setText(lastImei);
            etImei.setSelection(lastImei.length());
            btnSearch.callOnClick();
        }else if(returnBycleId != null && returnBycleId.length() >= 11 && returnBycleId.length() <= 15){
            etImei.setText(returnBycleId);
            lastImei =  returnBycleId;
            etImei.setSelection(returnBycleId.length());
            btnSearch.callOnClick();
        }else{
            lastImei = null;
            showToast("请扫描正确的imei号");
        }
        return lastImei;
    }



}
