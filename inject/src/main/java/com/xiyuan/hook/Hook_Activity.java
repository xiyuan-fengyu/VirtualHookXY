package com.xiyuan.hook;

import android.app.Activity;
import android.app.Activity_onCreate;
import android.app.Activity_onDestroy;
import android.app.Activity_startActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.xiyuan.hookmethod.HookMethod;
import com.xiyuan.hookmethod.Hooks;

/**
 * Created by xiyuan_fengyu on 2018/12/4.
 */

public class Hook_Activity {

    @HookMethod("protected void android.app.Activity.onCreate(android.os.Bundle)")
    public static void onCreate(Activity thiz, Bundle bundle) {
        Log.i("xiyuan", "onCreate: " + thiz + ", " + bundle);
        Log.i("xiyuan", "virtualHookDataPath=" + Hooks.getVirtualHookDataPath());
        Log.i("xiyuan", "application=" + Hooks.getApplication());
        Activity_onCreate.invoke(thiz, bundle);
    }

    @HookMethod("protected void android.app.Activity.onDestroy()")
    public static void onDestory(Activity thiz) {
        Log.i("xiyuan", "onDestroy: " + thiz);
        Activity_onDestroy.invoke(thiz);
    }

    @HookMethod("public void android.app.Activity.startActivity(android.content.Intent)")
    public static void startActivity(Activity thiz, Intent intent) {
        StringBuilder builder = new StringBuilder();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                builder.append("\nintent.extras.").append(key).append(": ").append(extras.get(key));
            }
        }
        Log.i("xiyuan", "startActivity: "
                +  "\ncontext: " + thiz
                +  "\nintent.component: " + intent.getComponent()
                +  "\nintent.action: " + intent.getAction()
                +  "\nintent.scheme: " + intent.getScheme()
                +  "\nintent.categories: " + join(intent.getCategories(), ", ")
                +  "\nintent.package: " + intent.getPackage()
                +  "\nintent.dataString: " + intent.getDataString()
                + builder.toString()
                + "\n\n"
        );
        Activity_startActivity.invoke(thiz, intent);
    }

    private static String join(Iterable iterable, String divider) {
        if (iterable== null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (Object item : iterable) {
            builder.append(item).append(divider);
        }
        return builder.toString();
    }

}
