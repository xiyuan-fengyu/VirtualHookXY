package lab.galaxy.yahfa;

import android.util.Log;

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

    private static List<Class<?>> hookInfoClasses = new LinkedList<>();

    private static final Matcher hookMethodM = Pattern.compile(".* ([^ ()]+)\\(.*").matcher("");

    static {
        System.loadLibrary("yahfa");
        init(android.os.Build.VERSION.SDK_INT);
    }

    public static void doHookDefault(ClassLoader patchClassLoader, ClassLoader originClassLoader) {
        try {
            Class<?> hooksClass = Class.forName("lab.galaxy.yahfa.Hooks", true, patchClassLoader);
            Class<?>[] hooks = (Class<?>[])hooksClass.getDeclaredField("hooks").get(null);
            for(Class<?> hook : hooks) {
                doHookItemDefault(patchClassLoader, hook, originClassLoader);
            }
            hookInfoClasses.add(hooksClass);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static void doHookItemDefault(ClassLoader patchClassLoader, Class<?> hookInfo, ClassLoader originClassLoader) {
        try {
            Log.i(TAG, "Start hooking with item " + hookInfo.getName());

            String hookMethodSignature = (String) hookInfo.getDeclaredField("method").get(null);
            String hookClassMethodName = null;
            synchronized (hookMethodM) {
                hookMethodM.reset(hookMethodSignature);
                if (hookMethodM.find()) {
                    hookClassMethodName = hookMethodM.group(1);
                }
            }

            if(hookClassMethodName == null) {
                Log.w(TAG, "No target class. Skipping...");
                return;
            }

            boolean isConstructor = false;
            String hookClassName;
            Class<?> hookClass;
            try {
                hookClassName = hookClassMethodName.substring(0, hookClassMethodName.lastIndexOf('.'));
                hookClass = Class.forName(hookClassName, true, originClassLoader);
            }
            catch (Exception e) {
                hookClass = Class.forName(hookClassMethodName, true, originClassLoader);
                hookClassName = hookClassMethodName;
                isConstructor = true;
            }

            if(Modifier.isAbstract(hookClass.getModifiers())) {
                Log.w(TAG, "Hook may fail for abstract class: " + hookClassName);
            }

            Method hook = null;
            Method backup = null;
            for (Method method : hookInfo.getDeclaredMethods()) {
                if (method.getName().equals("hook") && Modifier.isStatic(method.getModifiers())) {
                    hook = method;
                } else if (method.getName().equals("backup") && Modifier.isStatic(method.getModifiers())) {
                    backup = method;
                }
            }
            if (hook == null) {
                Log.e(TAG, "Cannot find hook for: " + hookMethodSignature);
                return;
            }

            Object original = null;
            if (isConstructor) {
                for (Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
                    if (constructor.toString().equals(hookMethodSignature)) {
                        original = constructor;
                        break;
                    }
                }
            }
            else {
                for (Method method : hookClass.getDeclaredMethods()) {
                    if (method.toString().equals(hookMethodSignature)) {
                        original = method;
                        break;
                    }
                }
            }
            if (original == null) {
                StringBuilder builder = new StringBuilder();
                if (isConstructor) {
                    for (Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
                        builder.append(constructor.toString()).append('\n');
                    }
                }
                else {
                    for (Method method : hookClass.getDeclaredMethods()) {
                        builder.append(method.toString()).append('\n');
                    }
                }
                Log.e(TAG, "Cannot find original method: " + hookMethodSignature
                        + "\nYou may may check the method signature with the following:\n"
                        + builder.toString());
                return;
            }

            bindBackupHook(original, hook, backup);
        }
        catch (Exception e) {
            Log.e(TAG, "fail to create hook from: " + hookInfo, e);
        }
    }

    private static native void bindBackupHook(Object original, Method hook, Method backup);

    private static native void init(int SDK_version);
}
