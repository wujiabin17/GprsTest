package com.sprocomm.gprstest.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by yuanbin.ning on 2017/6/5.
 */
public class ViewUtils {

    /** 把自身从父View中移除 */
    public static void removeSelfFromParent(View view) {
        // 先找到父类，再通过父类移除孩子
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(view);
            }
        }
    }

    /** FindViewById的泛型封装 */
    public static <T extends View> T findViewById(View layout, int id) {
        return (T) layout.findViewById(id);
    }
}