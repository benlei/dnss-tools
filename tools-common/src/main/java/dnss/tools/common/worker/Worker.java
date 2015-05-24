package dnss.tools.common.worker;

import java.util.Queue;
import java.util.function.Function;

public class Worker extends Thread {
    private static Function<Boolean, Boolean> condition;
    private static Queue<Runnable> queue;
    public static final int MAX_WORKERS = Math.max(Runtime.getRuntime().availableProcessors(), 1);

    public static void setCondition(Function<Boolean, Boolean> condition) {
        Worker.condition = condition;
    }

    public static void setQueue(Queue<Runnable> queue) {
        Worker.queue = queue;
    }

    @Override
    public void run() {
        while (condition.apply(queue.isEmpty())) {
            Runnable runnable = queue.poll();
            if (runnable == null) {
                Thread.yield(); // let another thread do something
                continue;
            }

            Thread.currentThread().setName(runnable.toString());
            runnable.run();
        }
    }
}
