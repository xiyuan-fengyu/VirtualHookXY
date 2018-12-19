package lab.galaxy.yahfa;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.galaxy.yahfa.util.LocalIp;
import lab.galaxy.yahfa.util.PortChecker;

/**
 * Created by xiyuan_fengyu on 2018/12/3.
 */

public class HelpServer {

    private static final String TAG = "YAHFA";

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .create();

    private String ip;

    private final int port;

    private Context virtualContext;

    private Application application;

    private ClassLoader originalClassLoader;

    public HelpServer(Context virtualContext, Application application) {
        this.ip = LocalIp.get();
        this.port = PortChecker.findFree(8080, 9999);

        this.virtualContext = virtualContext;
        this.application = application;
        this.originalClassLoader = application.getClassLoader();

        AsyncHttpServer server = new AsyncHttpServer();

        server.get("/", this::staticFile);

        server.get(".*\\.(html|css|js|json|gif|jpeg|webp|jpg|png|bmp|ico)(\\?.*)?", this::staticFile);

        server.get("/methods", this::methods);

        server.listen(port);
        Log.i(TAG, "Help server start successfully at http://" + ip + ":" + this.port + "\n" +
                "Rest api list:\n" +
                "/methods?class=<className>[&method=<methodName>]\t\tprint the method list of class\n");
    }

    private void staticFile(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        String path = request.getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        String assetPath = "web" + path;
        AssetManager assetManager = virtualContext.getAssets();
        try (InputStream in = assetManager.open(assetPath)) {
            response.sendStream(in, in.available());
        } catch (IOException e) {
            response.code(404);
            response.send("not found");
        }
    }

    private void methods(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        Map<String, Object> res = new HashMap<>();
        try {
            String className = request.getQuery().getString("class");
            if (className == null || className.isEmpty()) {
                res.put("success", false);
                res.put("message", "parameter class is required");
            }
            else {
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
                    List<String> methods = new ArrayList<>();
                    for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
                        methods.add(constructor.toString());
                    }
                    for (Method method : aClass.getDeclaredMethods()) {
                        methods.add(method.toString());
                    }
                    res.put("success", true);
                    res.put("message", "methods get successfully");
                    res.put("data", methods);
                }
                else {
                    res.put("success", false);
                    res.put("message", "class not found: " + className);
                }
            }
        }
        catch (Exception e) {
            res.put("success", false);
            res.put("message", "parameter class is required");
        }
        response.send("application/json", gson.toJson(res));
    }

}
