package dnss.tools.dnt;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DNT {
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static boolean verbose = true;

    public static Queue<Runnable> getQueue() {
        return queue;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        DNT.verbose = verbose;
    }
}
