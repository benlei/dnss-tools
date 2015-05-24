package dnss.tools.dnt;

import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ben on 5/24/2015.
 */
public class DNT {
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static boolean logQueries = true;

    public static Queue<Runnable> getQueue() {
        return queue;
    }

    public static boolean isLogQueries() {
        return logQueries;
    }

    public static void setLogQueries(boolean logQueries) {
        DNT.logQueries = logQueries;
    }

    public static IniPreferences getIni(InputStream input) throws IOException {
        Config.getGlobal().setEscape(false);
        return new IniPreferences(input);
    }
}
