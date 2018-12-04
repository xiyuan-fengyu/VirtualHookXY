package com.xiyuan.inject;

import android.util.Base64;
import android.util.Log;

/**
 * @author jx on 2018/12/4.
 */
public class Hook_SecretKeySpec_Constructor {
    public static String method = "public javax.crypto.spec.SecretKeySpec(byte[],java.lang.String)";

    public static Object hook(Object thiz, byte[] key, String algorithm) {
        Log.i("xiyuan", "SecretKeySpec key= " + Base64.encodeToString(key,Base64.DEFAULT) + ", algorithm" + algorithm);
        return backup(thiz, key, algorithm);
    }

    public static Object backup(Object thiz, byte[] ctx, String clazz) {
        // just a method stub for original method
        return null;
    }
}
