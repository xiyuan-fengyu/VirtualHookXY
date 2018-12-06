# VirtualHook
## 体验方式
1. 编译整个项目
2. 安装运行 VirtualHook app
3. 运行 push_inject.bat
4. 在 Logcat 中添加一个 filter， tag分为： xiyuan|YAHFA，启用Regex  
    YAHFA 过滤的日志是注入过程相关的信息  
    xiyuan 过滤的日志是注入的代码打印的日志  
5. 在 VirtualHook app 中添加手机中已经安装的其他app，然后运行  
    在日志中可以看到很多有用的信息    

## 更改说明
### 注入声明方式
在原项目的基础上更改了注入代码的加载方式和注入的声明方式，使得申明更加简单  
在注入过程中，用于保存原方法地址的桩方法是通过java annotation processor生成的  

注入声明的过程：  
编写一个 Hook 类，在其中声明一个通过@HookMethod注解的public static方法  
```java
package com.xiyuan.hook;

import android.app.Activity;
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
    }

}
```
编译 inject 模块，在 inject/build/generated/source/apt/debug 目录下将会生成   
protected void android.app.Activity.onCreate(android.os.Bundle)  
这个方法对应的桩方法类  
android.app.Activity_onCreate  
其中定义了一个方法  
```
public static <T> T invoke(android.app.Activity thiz, android.os.Bundle bundle) {
    // Stub for the orignal method
    return null;
}
```
在 Hook_Activity 的 onCreate 方法中调用 Activity_onCreate.invoke  
```
@HookMethod("protected void android.app.Activity.onCreate(android.os.Bundle)")
public static void onCreate(Activity thiz, Bundle bundle) {
    Log.i("xiyuan", "onCreate: " + thiz);
    Activity_onCreate.invoke(thiz, bundle);
}
```
在注入成功后，调用 Activity_onCreate.invoke 方法即是对原方法的调用  
有些情况下也可以不用调用原方法  

类的成员方法，构造方法，native方法都可以被hook  
注意：目标方法为静态方法时，hook方法和桩方法参数列表没有 thiz 这个参数   
详见 inject 模块几个注入的例子    

### 辅助服务
在VirtualApp中启动一个新的app同时，会启动一个辅助服务器  
在 YAHFA 过滤的日志中可以看到服务器的相关信息以及可用的rest api列表  
如果手机和开发者电脑不在同一个网段，可以通过adb shell连接手机后，在shell中通过 curl 命令来访问rest api

目前提供的rest api
```
/methods?class=<className>[&method=<methodName>]    打印类中定义的方法（包括构造方法），方便查询方法签名，通过method参数进行过滤，如果要查询构造函数，令method=<init>
```

## 已知问题
1. java.lang.System类中的方法hook后，应用可能无法启动  

## 更新日志
### 2018-12-06
1. 桩方法返回值改为 (T) new Object ，防止编辑组提示空指针异常警告  
2. 编写静态目标方法的hook例子  
3. 通过build.gradle, inject 模块编译生成apk后自动将apk导入到手机相应目录  

### 2018-12-04
1. 继续重写钩子的声明方式，使用更加优雅  
2. 重写 push_inject.bat  

### 2018-12-03
1. 更新整个项目的结构，方便在AS中分辨不同的项目
2. 重写钩子的声明方式
3. 提供额外服务，方便查询类的方法列表

