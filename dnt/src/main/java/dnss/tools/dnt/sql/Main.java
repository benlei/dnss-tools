package dnss.tools.dnt.sql;

import dnss.tools.common.worker.Worker;
import dnss.tools.dnt.DNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

public class Main {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    private final static String DEFAULT_INI = "dnt.ini";

    private static void showManual() {
        System.out.println("Usage: dnt-sql [INI_FILE]");
        System.out.println("'dnt-sql' converts relevant DNT files to SQL tables");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  dnt\t\t# Uses the default dnt.ini to converts SQL");
        System.out.println("  dnt \"C:\\dnt.ini\" \t\t# Uses the C:\\dnt.ini file settings to convert");
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream input;
        if (args.length > 1) {
            showManual();
            return;
        } else if (args.length == 1) {
            input = new FileInputStream(args[0]);
        } else {
            input = Main.class.getClassLoader().getResourceAsStream(DEFAULT_INI);
        }

        Preferences ini = DNT.getIni(input);

        // SQL conn
        Preferences sql = ini.node("sql");
        Connection conn = DriverManager.getConnection(
                sql.get("url", null),
                sql.get("user", null),
                sql.get("pass", null));
        DNT.setLogQueries(sql.getBoolean("log_queries", DNT.isLogQueries()));

        // auto commit off
        conn.setAutoCommit(false);


        Preferences pak = ini.node("pak");
        File root = new File(pak.get("root", null));
        File ext = new File(root, "resource/ext");
        List<File> files = Arrays.asList(ext.listFiles((dir, name) -> name.endsWith(".dnt")));

        // start queueing up the jobs
        Queue<Runnable> queue = DNT.getQueue();

        // start the workers
        AtomicBoolean stillAdding = new AtomicBoolean(true); // only since a Boolean doesn't have a setter
        Worker.setQueue(queue);
        Worker.setCondition((queueIsEmpty -> ! queueIsEmpty || stillAdding.get()));
        Thread[] threads = new Thread[Worker.MAX_WORKERS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }

        // First add the uistrings
        queue.add(new XMLParser(conn, new File(root, "resource/uistring/uistring.xml")));
        for (File file : files) {
            queue.add(new DNTParser(conn, file));
        }

        stillAdding.set(false);


        // wait for all workers to finish
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        conn.commit();
        conn.close();

        long endTime = System.currentTimeMillis();
        LOG.info("===================================================================");
        LOG.info("[system] workers = " + threads.length);
        LOG.info("[system] runtime = " + (endTime - startTime) + " ms");
    }
}
