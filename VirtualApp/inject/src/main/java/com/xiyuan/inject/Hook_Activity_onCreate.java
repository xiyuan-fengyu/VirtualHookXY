package com.xiyuan.inject;

import android.os.Bundle;
import android.util.Log;

/**
 * Created by xiyuan_fengyu on 2018/11/29.
 */

public class Hook_Activity_onCreate {

    public static String className = "android.app.Activity";

    public static String methodName = "onCreate";

    public static String methodSig = "(Landroid/os/Bundle;)V";

    public static void hook(Object thiz, Bundle bundle) {
        Log.i("xiyuan", "new activity: " + thiz);
        backup(thiz, bundle);
    }

    public static void backup(Object thiz, Bundle bundle) {
        Log.i("xiyuan", "backup");
    }

}
/*
获取 methodSig 的方式
javap -s -bootclasspath "D:\\SoftwareForCode\\adt-bundle-windows-x86_64-20140702\\sdk\\platforms\\android-27\\android.jar" -classpath bin/classes android.app.Activity
 */