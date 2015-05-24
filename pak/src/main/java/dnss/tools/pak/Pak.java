package dnss.tools.pak;

import dnss.tools.pak.extract.PakParser;
import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class Pak {
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static final ArrayList<Pattern> whiteList = new ArrayList<>();
    private static final ArrayList<Pattern> blackList = new ArrayList<>();
    private static final ArrayList<PakParser> parsers = new ArrayList<>();
    private static File destination;
    private static boolean autoDetect;
    private static boolean skipDeleted;
    private static boolean overwrite;
    private static final Log log = new Log();

    public static Queue<Runnable> getQueue() {
        return queue;
    }

    public static ArrayList<Pattern> getWhiteList() {
        return whiteList;
    }

    public static ArrayList<Pattern> getBlackList() {
        return blackList;
    }

    public static File getDestination() {
        return destination;
    }

    public static void setDestination(File destination) {
        Pak.destination = destination;
    }

    public static ArrayList<PakParser> getParsers() {
        return parsers;
    }

    public static boolean isOverwrite() {
        return overwrite;
    }

    public static void setOverwrite(boolean overwrite) {
        Pak.overwrite = overwrite;
    }

    public static boolean isAutoDetect() {
        return autoDetect;
    }

    public static void setAutoDetect(boolean autoDetect) {
        Pak.autoDetect = autoDetect;
    }

    public static boolean isSkipDeleted() {
        return skipDeleted;
    }

    public static void setSkipDeleted(boolean skipDeleted) {
        Pak.skipDeleted = skipDeleted;
    }

    public static IniPreferences getIni(InputStream input) throws IOException {
        Config.getGlobal().setEscape(false);
        return new IniPreferences(input);
    }

    public static boolean isParsingDone() {
        for (PakParser parser : parsers) {
            if (! parser.isDone()) {
                return false;
            }
        }

        return true;
    }

    public static Log getLog() {
        return log;
    }

    // For checking what should be logged
    public static class Log {
        // Log files that are not extracted
        private boolean ignored;

        // Log files that are deleted
        private boolean deleted;

        // Log files that are being extracted
        private boolean extracted = true;

        public boolean showIgnored() {
            return ignored;
        }

        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        public boolean showDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean showExtracted() {
            return extracted;
        }

        public void setExtracted(boolean extracted) {
            this.extracted = extracted;
        }
    }

    // do not call constructor!
    private Pak() {
    }
}
