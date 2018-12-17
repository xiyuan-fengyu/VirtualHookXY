package lab.galaxy.yahfa;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import lab.galaxy.yahfa.util.LocalIp;
import lab.galaxy.yahfa.util.PortChecker;

/**
 * Created by xiyuan_fengyu on 2018/12/3.
 */

public class HelpServer extends NanoHTTPD {

    private static final String TAG = "YAHFA";

    private String ip;

    private Application application;

    private ClassLoader originalClassLoader;

    public HelpServer(Application application) {
        super(PortChecker.findFree(8080, 9999));
        this.application = application;
        this.originalClassLoader = application.getClassLoader();
        try {
            ip = LocalIp.get();
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            Log.i(TAG, "Help server start successfully at http://" + ip + ":" + this.getListeningPort() + "\n" +
                    "Rest api list:\n" +
                    "/methods?class=<className>[&method=<methodName>]\t\tprint the method list of class\n");
        } catch (IOException e) {
            Log.e(TAG, "Help server failed to start", e);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String methodName = session.getUri().substring(1);
        try {
            java.lang.reflect.Method method = HelpServer.class.getDeclaredMethod(methodName, IHTTPSession.class);
            method.setAccessible(true);
            return (Response) method.invoke(this, session);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "method not found: " + methodName, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "method invoke with error: " + methodName, e);
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "plain/text", "bad request");
    }

    private Response methods(IHTTPSession session) {
        List<String> list = session.getParameters().get("class");
        if (list.size() > 0) {
            String className = list.get(0);
            Class<?> aClass = null;
            try {
                aClass = Class.forName(className, true, this.originalClassLoader);
            } catch (ClassNotFoundException e) {
                //
            }
            try {
                aClass = Class.forName(className, true, this.getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                //
            }

            if (aClass != null) {
                list = session.getParameters().get("method");
                String methodRegex = list == null || list.isEmpty() ? null : list.get(0);
                StringBuilder builder = new StringBuilder();
                for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
                    if (methodRegex == null || methodRegex.equals("<init>") || constructor.toString().matches(methodRegex)) {
                        builder.append(constructor.toString()).append('\n');
                    }
                }
                for (java.lang.reflect.Method method : aClass.getDeclaredMethods()) {
                    if (methodRegex == null ||  methodRegex.equals(method.getName()) || method.toString().matches(methodRegex)) {
                        builder.append(method.toString()).append('\n');
                    }
                }
                return newFixedLengthResponse(builder.toString());
            }
            else {
                return newFixedLengthResponse("class not found: " + className + "\n");
            }
        }
        return newFixedLengthResponse("parameter class is required\n");
    }

}
