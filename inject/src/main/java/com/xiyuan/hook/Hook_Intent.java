package com.xiyuan.hook;

import android.content.Context;
import android.content.Intent_constructor;
import android.util.Log;

import com.xiyuan.hookmethod.HookMethod;

/**
 * Created by xiyuan_fengyu on 2018/12/4.
 */

public class Hook_Intent {

    @HookMethod("public android.content.Intent(android.content.Context,java.lang.Class)")
    public static Object constructor(Object thiz, Context ctx, Class clazz) {
        Log.i("xiyuan", "new Intent: " + thiz + ", " + ctx + ", " + clazz);
        return Intent_constructor.invoke(thiz, ctx, clazz);
    }

    @HookMethod("public android.content.Intent()")
    public static Object constructor(Object thiz) {
        Log.i("xiyuan", "new Intent: " + thiz);
        return Intent_constructor.invoke(thiz);
    }

}
