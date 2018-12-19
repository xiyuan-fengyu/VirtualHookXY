//package com.xiyuan.hook;
//
//import android.util.Log;
//
//import com.xiyuan.hookmethod.HookMethod;
//
///**
// * Created by xiyuan_fengyu on 2018/12/6.
// */
//
//public class Hook_String {
//
//    // 注意：目标方法为静态方法时，hook方法参数列表不需要 thiz 这个参数
//    @HookMethod("public static java.lang.String java.lang.String.format(java.lang.String,java.lang.Object[])")
//    public static String format(String format, Object[] args) {
//        String res = String_format.invoke(format, args);
//        StringBuilder builder = new StringBuilder();
//        builder.append("String.format(").append(format);
//        for (Object arg : args) {
//            builder.append(", ").append(arg);
//        }
//        builder.append(") = ").append(res);
//        Log.i("xiyuan", builder.toString());
//        return res;
//    }
//
//}
