package dnss.tools.pak.extract;

import dnss.tools.pak.Pak;

import java.util.Queue;

public class Worker implements Runnable {
    @Override
    public void run() {
        Queue<Runnable> queue = Pak.getQueue();
        while (! Pak.isParsingDone() || ! queue.isEmpty()) {
            Runnable runnable = queue.poll();
            if (runnable == null) {
                Thread.yield(); // let another thread do something
                continue;
            }

            if (runnable instanceof PakParser) {
                PakParser parser = (PakParser)runnable;
                Thread.currentThread().setName("Parser-" + parser.getPakName());
            } else { // must be a PakContent
                PakContent content = (PakContent)runnable;
                Thread.currentThread().setName("Content-" + content.getParser().getPakName());
            }
            runnable.run();
        }
    }
}
