package dnss.tools.common.worker;

import java.util.Queue;
import java.util.function.Function;

public class Worker extends Thread {
    private static Function<Boolean, Boolean> condition = (empty -> ! empty);
    private static Queue<Runnable> queue;
    public static final int MAX_WORKERS = Math.max(Runtime.getRuntime().availableProcessors(), 1);
    private static Thread[] workers = new Thread[MAX_WORKERS];

    public static void setCondition(Function<Boolean, Boolean> condition) {
        Worker.condition = condition;
    }

    public static void setQueue(Queue<Runnable> queue) {
        Worker.queue = queue;
    }

    public static void startWorkers() {
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public static void awaitTermination() {
        for (int i = 0; i < workers.length; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException ignorable) {
            }
        }
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
