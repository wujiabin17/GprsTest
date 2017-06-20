package com.sprocomm.gprstest.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.sprocomm.gprstest.R;
import com.sprocomm.gprstest.bean.SettingsCase;

import java.util.List;

public class SettingsAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater inflater;
	private List<SettingsCase> mData;

	public SettingsAdapter(Context context, List<SettingsCase> data) {
		mContext = context;
		mData = data;
		initData();
	}

	private void initData() {
		inflater = LayoutInflater.from(mContext);
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public SettingsCase getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_settings, null);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.settings_name);
			viewHolder.value = (EditText) convertView
					.findViewById(R.id.settings_value);
			viewHolder.checkBox = (CheckBox) convertView
					.findViewById(R.id.settings_check);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SettingsCase settingsCase = mData.get(position);
		viewHolder.name.setText(String.format(
				mContext.getString(R.string.settings_item_name), position + 1,
				settingsCase.getName()));
		viewHolder.checkBox
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						settingsCase.setTest(isChecked);
					}
				});
		viewHolder.checkBox.setChecked(settingsCase.isTest());

        if (viewHolder.value.getTag() instanceof TextWatcher) {
        	viewHolder.value.removeTextChangedListener((TextWatcher) (viewHolder.value.getTag()));
        }

        if (TextUtils.isEmpty(settingsCase.getValue())) {
        	viewHolder.value.setText("");
        } else {
        	viewHolder.value.setText(settingsCase.getValue());
        }

		viewHolder.value.setEnabled(settingsCase.isEnable());
        if (settingsCase.isFocus()) {
            if (!viewHolder.value.isFocused()) {
            	viewHolder.value.requestFocus();
            }
            CharSequence text = settingsCase.getValue();
            viewHolder.value.setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
        } else {
            if (viewHolder.value.isFocused()) {
            	viewHolder.value.clearFocus();
            }
        }

        viewHolder.value.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
					if (settingsCase.isEnable()) {
						final boolean focus = settingsCase.isFocus();
						check(position);
						if (!focus && !viewHolder.value.isFocused()) {
							viewHolder.value.requestFocus();
							viewHolder.value.onWindowFocusChanged(true);
						}
					}
                }
                return false;
            }
        });
		
		final TextWatcher watcher = new SimpeTextWather() {
            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
					settingsCase.setValue(null);
                } else {
					settingsCase.setValue(String.valueOf(s));
                }
            }
        };
        viewHolder.value.addTextChangedListener(watcher);
        viewHolder.value.setTag(watcher);
		return convertView;
	}

	private void check(int position) {
        for (SettingsCase sc : mData) {
            sc.setFocus(false);
        }
		mData.get(position).setFocus(true);
    }
	
	class ViewHolder {
		TextView name;
		EditText value;
		CheckBox checkBox;
	}
}
