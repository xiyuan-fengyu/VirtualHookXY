package lab.galaxy.yahfa.util;

import java.net.ServerSocket;

/**
 * Created by xiyuan_fengyu on 2018/9/29.
 */

public class PortChecker {

    public static boolean isFree(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static int findFree(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (isFree(i)) {
                return i;
            }
        }
        throw new NoFreePortException(start, end);
    }

    public static class NoFreePortException extends RuntimeException {

        public final int start;

        public final int end;

        public NoFreePortException(int start, int end) {
            super("no free port between " + start + " with " + end);
            this.start = start;
            this.end = end;
        }

    }

}
