package com.xiyuan.hook;

import android.app.Activity;
import android.app.Activity_onCreate;
import android.app.Activity_onPause;
import android.app.Activity_onResume;
import android.os.Bundle;
import android.util.Log;

import com.xiyuan.hookmethod.HookMethod;

/**
 * Created by xiyuan_fengyu on 2018/12/4.
 */

public class Hook_Activity {

    @HookMethod("protected void android.app.Activity.onCreate(android.os.Bundle)")
    public static void onCreate(Activity thiz, Bundle bundle) {
        Log.i("xiyuan", "onCreate: " + thiz);
        Activity_onCreate.invoke(thiz, bundle);
    }

    @HookMethod("protected void android.app.Activity.onResume()")
    public static void onResume(Activity thiz) {
        Log.i("xiyuan", "onResume: " + thiz);
        Activity_onResume.invoke(thiz);
    }

    @HookMethod("protected void android.app.Activity.onPause()")
    public static void onPause(Activity thiz) {
        Log.i("xiyuan", "onPause: " + thiz);
        Activity_onPause.invoke(thiz);
    }

}
