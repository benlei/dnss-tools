package dnss.tools.pak;

import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class Pak {
    private static final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static final ArrayList<Pattern> whiteList = new ArrayList<>();
    private static final ArrayList<Pattern> blackList = new ArrayList<>();
    private static final ArrayList<PakParser> parsers = new ArrayList<>();
    private static List<File> sources;
    private static File output;
    private static boolean keepDeleted;
    private static boolean overwrite;
    private static boolean singly;
    private static boolean verbose;

    public static Queue<Runnable> getQueue() {
        return queue;
    }

    public static ArrayList<Pattern> getWhiteList() {
        return whiteList;
    }

    public static ArrayList<Pattern> getBlackList() {
        return blackList;
    }

    public static List<File> getSources() {
        return sources;
    }

    public static void setSources(List<File> sources) {
        Pak.sources = sources;
    }

    public static File getOutput() {
        return output;
    }

    public static void setOutput(File output) {
        Pak.output = output;
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

    public static boolean isKeepDeleted() {
        return keepDeleted;
    }

    public static void setKeepDeleted(boolean keepDeleted) {
        Pak.keepDeleted = keepDeleted;
    }

    public static boolean isSingly() {
        return singly;
    }

    public static void setSingly(boolean singly) {
        Pak.singly = singly;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        Pak.verbose = verbose;
    }
}
