package com.sprocomm.gprstest.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sprocomm.gprstest.R;
import com.sprocomm.gprstest.adapter.SettingsAdapter;
import com.sprocomm.gprstest.application.App;
import com.sprocomm.gprstest.bean.SettingsCase;
import com.sprocomm.gprstest.config.Configure;
import com.sprocomm.gprstest.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {

	private ListView mListView;
	private SettingsAdapter adapter;
	private List<SettingsCase> mData = new ArrayList<>();

	private EditText ipEdit;
	private RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		initViews();
		initData();
	}

	private void initViews() {
		mListView = (ListView) findViewById(R.id.list_view);
		
		ipEdit = (EditText) findViewById(R.id.server_ip_view);
		radioGroup = (RadioGroup) findViewById(R.id.radio_group);

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group,int checkedId) {
				switch (checkedId) {
				case R.id.semi_bt_test:
					Configure.METHOD_NAME = Configure.METHOD_SUB_SEMI_BT_TEST;
					break;
				case R.id.bt_test:
					Configure.METHOD_NAME = Configure.METHOD_SUB_BT_TEST;
					break;
				}
			}
		});
	}

	private void initData() {
		updateConfigs();
		adapter = new SettingsAdapter(this, mData);
		mListView.setAdapter(adapter);
	}

	public void saveSettings(View view) {
		Configure.SERVER_IP = ipEdit.getText().toString();

		App.getInstance().sharedSave(App.SHARED_KEY_CONFIG_IP, Configure.SERVER_IP);
		App.getInstance().sharedSave(App.SHARED_KEY_CONFIG_METHOD, Configure.METHOD_NAME);

		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < mData.size(); ++i) {
			jsonArray.put(mData.get(i).toJSONString());
		}
		App.getInstance().sharedSave(App.SHARED_KEY_SETTINGS_VALUE, jsonArray.toString());
		showMessage(R.string.save_success);
	}
	
	public void resetSettings(View view) {
		Configure.SERVER_IP = "192.168.8.13";
		ipEdit.setText(Configure.SERVER_IP);
		radioGroup.check(R.id.semi_bt_test); // 监听会更新Config.METHOD_NAME

		mData = Utils.getDefaultValues(this);
		adapter.notifyDataSetChanged();
		App.getInstance().sharedSave(App.SHARED_KEY_SETTINGS_VALUE, "");
		App.getInstance().sharedSave(App.SHARED_KEY_CONFIG_IP, "");
		App.getInstance().sharedSave(App.SHARED_KEY_CONFIG_METHOD, "");
		showMessage(R.string.reset_success);
	}

	private void updateConfigs() {
		String savedIp = App.getInstance().sharedGet(App.SHARED_KEY_CONFIG_IP);
		String savedMethod = App.getInstance().sharedGet(App.SHARED_KEY_CONFIG_METHOD);
		String savedSettings = App.getInstance().sharedGet(App.SHARED_KEY_SETTINGS_VALUE);
		if (!TextUtils.isEmpty(savedIp)) {
			Configure.SERVER_IP = savedIp;
			ipEdit.setText(Configure.SERVER_IP);
		}
		if (!TextUtils.isEmpty(savedMethod)) {
			Configure.METHOD_NAME = savedMethod;
			if (Configure.METHOD_NAME.equals(Configure.METHOD_SUB_SEMI_BT_TEST)) {
				radioGroup.check(R.id.semi_bt_test);				
			} else if (Configure.METHOD_NAME.equals(Configure.METHOD_SUB_BT_TEST)) {
				radioGroup.check(R.id.bt_test);
			}
		}
		if (!TextUtils.isEmpty(savedSettings)) {
			try {
				mData.clear();
				JSONArray jsonArray = new JSONArray(savedSettings);
				for (int i = 0; i < jsonArray.length(); ++i) {
					SettingsCase sc = new SettingsCase(jsonArray.getJSONObject(i));
					mData.add(sc);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			mData = Utils.getDefaultValues(this);
		}
	}
	
	private Toast mToast;

	private void showMessage(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(resId);
			mToast.show();
		}
	}
}
