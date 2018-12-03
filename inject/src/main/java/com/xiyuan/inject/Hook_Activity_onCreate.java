package com.xiyuan.inject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by xiyuan_fengyu on 2018/11/29.
 */

public class Hook_Activity_onCreate {

    public static String method = "protected void android.app.Activity.onCreate(android.os.Bundle)";

    public static void hook(Object thiz, Bundle bundle) {
        Log.i("xiyuan", "new activity: " + thiz);
        backup(thiz, bundle);
    }

    public static void backup(Object thiz, Bundle bundle) {
        // just a method stub for original method
    }

}
