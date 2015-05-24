package dnss.tools.dnt.sql;

import dnss.tools.dnt.DNT;

import java.util.Queue;

public class Worker implements Runnable {
    @Override
    public void run() {
        Queue<Runnable> queue = DNT.getQueue();
        while (! queue.isEmpty()) {
            Runnable runnable = queue.poll();
            Thread.currentThread().setName(((AbstractParser) runnable).getThreadName());
            runnable.run();
        }
    }
}
