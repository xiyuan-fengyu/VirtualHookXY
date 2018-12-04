package com.xiyuan.inject;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by xiyuan_fengyu on 2018/12/3.
 */

public class LogToFile {

    public static void append(String str) {
        try (FileOutputStream out = new FileOutputStream("/data/data/io.virtualhook/virtual/data/inject/log.txt", true)) {
            if (!str.endsWith("\n")) {
                str += "\n";
            }
            out.write(str.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
