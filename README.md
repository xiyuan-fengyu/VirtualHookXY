# VirtualHook
## 体验方式
1. 编译整个项目
2. 安装运行app
3. 运行 push_inject.bat
4. 在 Logcat 中添加两个 filter， tag分别为： YAHFA, xiyuan  
    YAHFA 过滤的日志是注入过程相关的信息  
    xiyuan 过滤的日志是注入的代码打印的日志  
    每当打开新的Activity，就会在xiyuan这个tag中看到日志  

## 更改说明
### 注入声明方式
在原项目的基础上更改了注入代码的加载方式和注入的声明方式，使得申明更加简单  
详见 inject 模块  
```
package lab.galaxy.yahfa;

import com.xiyuan.inject.Hook_Activity_onCreate;

/**
 * Created by liuruikai756 on 31/03/2017.
 */

public class Hooks {

    public static Class[] hooks = {
            Hook_Activity_onCreate.class
    };

}
```
```
package com.xiyuan.inject;

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
        Log.i("xiyuan", "backup");
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
### 2018-12-03
1. 更新整个项目的结构，方便在AS中分辨不同的项目
2. 重写钩子的声明方式
3. 提供额外服务，方便查询类的方法列表

