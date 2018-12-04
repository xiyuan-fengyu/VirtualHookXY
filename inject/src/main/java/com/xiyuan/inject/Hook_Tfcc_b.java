package com.xiyuan.inject;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by xiyuan_fengyu on 2018/11/30.
 */

public class Hook_Tfcc_b {

    public static String method = "public java.lang.String com.bytedance.caijing.tfccsdk.Tfcc.b(int,java.lang.String,java.lang.String,int[]) throws java.lang.IllegalStateException";

    public static String hook(Object thiz, int arg0, String arg1, String arg2, int[] arg3) {
        String res = backup(thiz, arg0, arg1, arg2, arg3);
        String log = System.currentTimeMillis()
                + "\n" + method
                + "\narg0=" + arg0
                + "\narg1=" + arg1
                + "\narg2=" + arg2
                + "\narg3=" + Arrays.toString(arg3)
                + "\nresult=" + res
                + "\ndecoded=" + new String(Base64.decode(res, 2), StandardCharsets.UTF_8)
                + "\n\n";
        Log.i("xiyuan", log);
        LogToFile.append(log);
        return res;
    }

    public static String backup(Object thiz, int arg0, String arg1, String arg2, int[] arg3) {
        // just a method stub for original method
        return null;
    }

}
