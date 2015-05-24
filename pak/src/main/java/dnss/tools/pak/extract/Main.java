package dnss.tools.pak.extract;

import dnss.tools.common.worker.Worker;
import dnss.tools.pak.Pak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private final static String DEFAULT_INI = "pak.ini";

    private static void showManual() {
        System.out.println("Usage: pak [INI_FILE]");
        System.out.println("'pak' Uses the ini file options to extract pak file(s).");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  pak\t\t# Uses the default pak.ini to extract pak files.");
        System.out.println("  pak \"C:\\pak.ini\" \t\t# Uses the C:\\pak.ini file settings to extract pak files.");
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        InputStream input;
        switch (args.length) {
            case 0:
                input = Main.class.getClassLoader().getResourceAsStream(DEFAULT_INI);
                break;
            case 1:
                File file = new File(args[0]);
                LOG.info("Loading properties from " + file.getAbsolutePath());
                input = new FileInputStream(file);
                break;
            default:
                showManual();
                return;
        }

        Preferences ini = Pak.getIni(input);

        LOG.info("===================================================================");
        LOG.info("PakExtractor Properties");
        LOG.info("===================================================================");

        // setup the white and black list
        if (ini.nodeExists("whitelist")) {
            List<Pattern> list = Pak.getWhiteList();
            Preferences whiteList = ini.node("whitelist");
            for (String key : whiteList.keys()) {
                Pattern pattern = Pattern.compile(whiteList.get(key, ""));
                list.add(pattern);
                LOG.info("[whitelist] " + key + " = " + pattern.pattern());
            }
        }

        if (ini.nodeExists("blacklist")) {
            List<Pattern> list = Pak.getBlackList();
            Preferences blackList = ini.node("blacklist");
            for (String key : blackList.keys()) {
                Pattern pattern = Pattern.compile(blackList.get(key, ""));
                list.add(pattern);
                LOG.info("[blacklist] " + key + " = " + pattern.pattern());
            }
        }

        // log preferences
        if (ini.nodeExists("log")) {
            Preferences log = ini.node("log");
            Pak.Log pakLog = Pak.getLog();
            pakLog.setIgnored(log.getBoolean("ignored", pakLog.showIgnored()));
            pakLog.setDeleted(log.getBoolean("deleted", pakLog.showDeleted()));
            pakLog.setExtracted(log.getBoolean("extracted", pakLog.showExtracted()));

            LOG.info("[log] ignored = " + pakLog.showIgnored());
            LOG.info("[log] deleted = " + pakLog.showDeleted());
            LOG.info("[log] extracted = " + pakLog.showExtracted());
        }

        // common section must exist
        Preferences common = ini.node("common");

        Pak.setAutoDetect(common.getBoolean("auto_detect", Pak.isAutoDetect()));
        LOG.info("[common] auto_detect = " + Pak.isAutoDetect());

        Pak.setOverwrite(common.getBoolean("overwrite", Pak.isOverwrite()));
        LOG.info("[common] overwrite = " + Pak.isOverwrite());

        Pak.setSkipDeleted(common.getBoolean("skip_deleted", Pak.isSkipDeleted()));
        LOG.info("[common] skip_deleted = " + Pak.isSkipDeleted());

        Pak.setDestination(new File(common.get("destination", null)));
        LOG.info("[common] destination = " + Pak.getDestination().getAbsolutePath());


        // Get list of all pak files to extract
        List<File> files;
        if (Pak.isAutoDetect()) { // detect pak files in source
            File source = new File(common.get("source", null));
            files = Arrays.asList(source.listFiles((dir, name) -> name.endsWith(".pak")));
        } else {
            files = new ArrayList<>();
            Preferences pakPref = ini.node("paks");
            for (String key : pakPref.keys()) {
                files.add(new File(pakPref.get(key, null)));
            }
        }

        for (File file : files) {
            LOG.info("[pak] " + file.getName() + " = " + file.getAbsolutePath());
        }


        // the queue and parser
        Queue<Runnable> queue = Pak.getQueue();
        List<PakParser> parsers = Pak.getParsers();

        // condition for workers to stay alive:
        // 1) more incoming parsers
        // 2) queue is not empty
        // 3) parsing is not done
        Worker.setQueue(queue);
        Worker.setCondition(condition -> files.size() != parsers.size() || ! condition || ! Pak.isParsingDone());

        Thread[] threads = new Thread[Worker.MAX_WORKERS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Worker();
            threads[i].start();
        }

        for (File file : files) {
            PakParser parser = new PakParser(file);
            parsers.add(parser);
            queue.add(parser); // add parser to queue
        }

        // wait for all workers to finish
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        long endTime = System.currentTimeMillis();

        for (PakParser parser : parsers) {
            String name = parser.getPakName();
            String format = "[%s] %s = %d";
            LOG.info("===================================================================");
            LOG.info(String.format(format, name, "total", parser.getTotalFiles()));
            LOG.info(String.format(format, name, "extracted", parser.getExtractedFiles()));
            LOG.info(String.format(format, name, "skipped", parser.getSkippeddFiles()));

            if (parser.getSkippeddFiles() != parser.getIgnoredFiles()) {
                LOG.info(String.format(format, name, "ignored", parser.getIgnoredFiles()));
            }
        }

        LOG.info("===================================================================");
        LOG.info("[system] workers = " + threads.length);
        LOG.info("[system] runtime = " + (endTime - startTime) + " ms");
    }
}
