package dnss.tools.dnt;

import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DNT {
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static final Map<Integer, String> uiString = new HashMap<>();
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

    public static Map<Integer, String> getUiString() {
        return uiString;
    }

    public static IniPreferences getIni(InputStream input) throws IOException {
        Config.getGlobal().setEscape(false);
        return new IniPreferences(input);
    }
}
