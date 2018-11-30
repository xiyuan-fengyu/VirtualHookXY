VirtualHook
-----------

由于原项目无法搜索到 plugin packages，所以这里改为从固定目录加载注入apk代码  
更改代码详见  
com/lody/virtual/client/VClientImpl.java:357  

inject 这个模块是注入代码模块，在 android.app.Activity.onCreate 方法上注入了代码    

编译整个项目  
将 inject/build/outputs/apk/debug/inject-debug.apk 上传到 /data/data/io.virtualhook/virtual/data/inject 目录下  
在 Logcat 中添加两个 filter， tag分别为： YAHFA, xiyuan  
YAHFA 过滤的日志是注入相关的信息  
xiyuan 过滤的日志是注入的代码打印的日志   
