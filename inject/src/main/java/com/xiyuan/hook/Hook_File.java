////日志打印太多了，影响分析日志，所以这里注释掉，如果要体验效果，可以解开注释
//package com.xiyuan.hook;
//
//import android.util.Log;
//
//import com.xiyuan.hookmethod.HookMethod;
//
//import java.io.File_constructor;
//import java.net.URI;
//
///**
// * Created by xiyuan_fengyu on 2018/12/4.
// */
//
//public class Hook_File {
//
//    @HookMethod("public java.io.File(java.lang.String)")
//    public static Object constructor(Object thiz, String path) {
//        Log.i("xiyuan", "new File: " + path);
//        return File_constructor.invoke(thiz, path);
//    }
//
//}
