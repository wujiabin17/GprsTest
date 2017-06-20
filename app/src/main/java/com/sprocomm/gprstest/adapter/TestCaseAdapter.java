package com.sprocomm.gprstest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sprocomm.gprstest.R;
import com.sprocomm.gprstest.bean.OneCase;
import com.sprocomm.gprstest.bean.TestCase;
import com.sprocomm.gprstest.bean.TwoCase;
import com.sprocomm.gprstest.utils.LogUtils;
import com.sprocomm.gprstest.utils.ViewUtils;

import java.util.List;

/**
 * Created by yuanbin.ning on 2017/6/7.
 */

public class TestCaseAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private List<TestCase> mData;
    public TestCaseAdapter(Context context, List<TestCase> data) {
        this.mContext = context;
        this.mData = data;
        inflater = LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TestCase getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        DoubleHolder doubleHolder = null;
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TestCase.TYPE_1:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_test, null);
                    viewHolder = new ViewHolder(convertView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                break;
            case TestCase.TYPE_2:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_test2, null);
                    View v1 = ViewUtils.findViewById(convertView, R.id.layout1);
                    View v2 = ViewUtils.findViewById(convertView, R.id.layout2);
                    doubleHolder = new DoubleHolder(v1, v2);
                    convertView.setTag(doubleHolder);
                } else {
                    doubleHolder = (DoubleHolder) convertView.getTag();
                }
                break;
        }
        final TestCase tc = mData.get(position);
        switch (viewType) {
            case TestCase.TYPE_1:
                viewHolder.set(tc);
                break;
            case TestCase.TYPE_2:
                doubleHolder.set(tc);
                break;
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    class ViewHolder {
        TextView nameView;
        TextView passView;
        public ViewHolder(View view) {
            nameView = ViewUtils.findViewById(view, R.id.type_view);
            passView = ViewUtils.findViewById(view, R.id.result_view);
        }

        public void set(TestCase tc) {
            String name = tc.getName();
            String value = tc.getValue();
            boolean showPass = (tc.getState() == 1);
            boolean pass = tc.isPass();
            if (value == null) {
                nameView.setText(name + "：");
                passView.setVisibility(View.GONE);
            } else {
                nameView.setText(name + "：" + value);
                if (showPass) {
                    if (pass) {
                        passView.setText(R.string.pass);
                        passView.setTextColor(Color.GREEN);
                    } else {
                        passView.setText(R.string.fail);
                        passView.setTextColor(Color.RED);
                    }
                    passView.setVisibility(View.VISIBLE);
                } else {
                    passView.setVisibility(View.GONE);
                }
            }
        }
    }

    class DoubleHolder {
        ViewHolder holder1;
        ViewHolder holder2;
        public DoubleHolder(View v1, View v2) {
            holder1 = new ViewHolder(v1);
            holder2 = new ViewHolder(v2);
        }

        public void set(TestCase tc) {
            holder1.set(tc);
            if (tc instanceof TwoCase) {
                TwoCase twoCase = (TwoCase) tc;
                holder2.set(twoCase.getTestCase());
            }
        }
    }
}
