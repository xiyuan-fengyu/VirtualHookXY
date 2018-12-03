package com.xiyuan.inject;

import android.content.Context;
import android.util.Log;

/**
 * Created by xiyuan_fengyu on 2018/11/29.
 */

public class Hook_Intent_constructor {

    public static String method = "public android.content.Intent(android.content.Context,java.lang.Class)";

    public static Object hook(Object thiz, Context ctx, Class clazz) {
        Log.i("xiyuan", "new Intent: " + ctx + ", " + clazz);
        return backup(thiz, ctx, clazz);
    }

    public static Object backup(Object thiz, Context ctx, Class clazz) {
        // just a method stub for original method
        return null;
    }

}
