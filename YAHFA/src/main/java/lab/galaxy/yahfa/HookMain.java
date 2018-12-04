package lab.galaxy.yahfa;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuruikai756 on 28/03/2017.
 */

public class HookMain {
    private static final String TAG = "YAHFA";

    private static final Matcher hookMethodM = Pattern.compile(".* ([^ ()]+)\\(.*").matcher("");

    static {
        System.loadLibrary("yahfa");
        init(android.os.Build.VERSION.SDK_INT);
    }

    public static void doHookDefault(ClassLoader patchClassLoader, ClassLoader originClassLoader) {
        try {
            Class<?> hooksClass = Class.forName("com.xiyuan.hookmethod.Hooks", true, patchClassLoader);
            Class<?>[] hooks = (Class<?>[])hooksClass.getDeclaredField("hooks").get(null);
            for(Class<?> hook : hooks) {
                doHookItemDefault(patchClassLoader, hook, originClassLoader);
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static void doHookItemDefault(ClassLoader patchClassLoader, Class<?> hook, ClassLoader originClassLoader) {
        try {
            Log.i(TAG, "Start hooking with item " + hook.getName());

            Class<?> HookMethodClass = Class.forName("com.xiyuan.hookmethod.HookMethod", false, patchClassLoader);
            Method valueMethod = HookMethodClass.getMethod("value");
            for (Method method : hook.getDeclaredMethods()) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    if (annotation.annotationType() == HookMethodClass) {
                        String orignalMethodSignature = (String) valueMethod.invoke(annotation);
                        analysisHookMethod(orignalMethodSignature, method, patchClassLoader, originClassLoader);
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "fail to create hook from: " + hook, e);
        }
    }

    private static void analysisHookMethod(String orignalMethodSignature, Method hook, ClassLoader patchClassLoader, ClassLoader originClassLoader) throws ClassNotFoundException {
        Log.i(TAG, "start hook: " + orignalMethodSignature);

        String orignalClassMethodName = null;
        synchronized (hookMethodM) {
            hookMethodM.reset(orignalMethodSignature);
            if (hookMethodM.find()) {
                orignalClassMethodName = hookMethodM.group(1);
            }
        }

        if(orignalClassMethodName == null) {
            Log.e(TAG, "cannot found the orignal class and method from the HookMethod value(\"" + orignalMethodSignature + "\"), skipping...");
            return;
        }

        // 查找 orignal 类
        boolean isConstructor = false;
        String orignalClassName;
        Class<?> orignalClass;
        try {
            orignalClassName = orignalClassMethodName.substring(0, orignalClassMethodName.lastIndexOf('.'));
            orignalClass = Class.forName(orignalClassName, true, originClassLoader);
        }
        catch (Exception e) {
            orignalClass = Class.forName(orignalClassMethodName, true, originClassLoader);
            orignalClassName = orignalClassMethodName;
            isConstructor = true;
        }

        // 查找 orignal 方法
        Object original = null;
        if (isConstructor) {
            for (Constructor<?> constructor : orignalClass.getDeclaredConstructors()) {
                if (constructor.toString().equals(orignalMethodSignature)) {
                    original = constructor;
                    break;
                }
            }
        }
        else {
            for (Method method : orignalClass.getDeclaredMethods()) {
                if (method.toString().equals(orignalMethodSignature)) {
                    original = method;
                    break;
                }
            }
        }
        if (original == null) {
            StringBuilder builder = new StringBuilder();
            if (isConstructor) {
                for (Constructor<?> constructor : orignalClass.getDeclaredConstructors()) {
                    builder.append(constructor.toString()).append('\n');
                }
            }
            else {
                for (Method method : orignalClass.getDeclaredMethods()) {
                    builder.append(method.toString()).append('\n');
                }
            }
            Log.e(TAG, "Cannot find the original method: " + orignalMethodSignature
                    + "\nYou may may check the method signature with the following:\n"
                    + builder.toString());
            return;
        }

        // 查找 orignalStub 类
        String orignalStubClassName;
        if (isConstructor) {
            orignalStubClassName = orignalClassMethodName + "_constructor";
        }
        else {
            orignalStubClassName = orignalClassMethodName;
            int lastDotIndex= orignalStubClassName.lastIndexOf('.');
            if (lastDotIndex > -1) {
                orignalStubClassName = orignalStubClassName.substring(0, lastDotIndex)
                        + '_' + orignalStubClassName.substring(lastDotIndex + 1);
            }
        }

        // 查找 orignalStub 方法
        Method orignalStub = null;
        Class orignalStubClass = Class.forName(orignalStubClassName, true, patchClassLoader);
        String hookMethodSignature = hook.toString();
        String orignalStubMethodSignature = "public static java.lang.Object " + orignalStubClassName
                + ".invoke" + hookMethodSignature.substring(hookMethodSignature.indexOf('('),
                hookMethodSignature.indexOf(')') + 1);
        for (Method method : orignalStubClass.getDeclaredMethods()) {
            if (method.toString().equals(orignalStubMethodSignature)) {
                orignalStub = method;
                break;
            }
        }
        if (orignalStub == null) {
            Log.e(TAG, "Cannot find the original stub method: " + orignalStubMethodSignature);
            return;
        }

        Log.i(TAG, "hook bind:\noriginal: " + original + "\nhook: " + hook + "\norignalStub: " + orignalStub);
        bind(original, hook, orignalStub);
        Log.i(TAG, "hook complete: " + orignalMethodSignature);
    }

    private static native void bind(Object original, Method hook, Method orignalStub);

    private static native void init(int SDK_version);

}
