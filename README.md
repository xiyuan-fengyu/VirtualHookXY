# VirtualHook
## 体验方式
1. 编译整个项目
2. 安装运行 VirtualHook app
3. 运行 push_inject.bat
4. 在 Logcat 中添加两个 filter， tag分别为： YAHFA, xiyuan  
    YAHFA 过滤的日志是注入过程相关的信息  
    xiyuan 过滤的日志是注入的代码打印的日志  
5. 在 VirtualHook app 中添加手机中已经安装的其他app，然后运行  
    每当打开新的Activity，就会在xiyuan这个tag中看到日志  

## 更改说明
### 注入声明方式
在原项目的基础上更改了注入代码的加载方式和注入的声明方式，使得申明更加简单  
详见 inject 模块  
```java
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
```

### 辅助服务
在VirtualApp中启动一个新的app同时，会启动一个辅助服务器  
在 YAHFA 过滤的日志中可以看到服务器的相关信息以及可用的rest api列表  
如果手机和开发者电脑不在同一个网段，可以通过adb shell连接手机后，在shell中通过 curl 命令来访问rest api

目前提供的rest api
```
/methods?class=<className>[&method=<methodName>]    打印类中定义的方法（包括构造方法），方便查询方法签名，通过method参数进行过滤，如果要查询构造函数，令method=<init>
```

## 更新日志
### 2018-12-04
1. 继续重写钩子的声明方式，使用更加优雅  
2. 重写 push_inject.bat  

### 2018-12-03
1. 更新整个项目的结构，方便在AS中分辨不同的项目
2. 重写钩子的声明方式
3. 提供额外服务，方便查询类的方法列表

