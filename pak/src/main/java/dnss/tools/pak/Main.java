package dnss.tools.pak;

import dnss.tools.common.worker.Worker;
import org.apache.commons.cli.*;
import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class Main {
    private static void loadIniProperties(InputStream input) throws Exception {
        Config.getGlobal().setEscape(false);
        Preferences preferences = new IniPreferences(input);

        if (preferences.nodeExists("whitelist")) {
            List<Pattern> list = Pak.getWhiteList();
            Preferences whiteList = preferences.node("whitelist");
            for (String key : whiteList.keys()) {
                Pattern pattern = Pattern.compile(whiteList.get(key, ""));
                list.add(pattern);
            }
        }

        if (preferences.nodeExists("blacklist")) {
            List<Pattern> list = Pak.getBlackList();
            Preferences blackList = preferences.node("blacklist");
            for (String key : blackList.keys()) {
                Pattern pattern = Pattern.compile(blackList.get(key, ""));
                list.add(pattern);
            }
        }

        // common section must exist
        Preferences common = preferences.node("common");
        Pak.setOverwrite(common.getBoolean("overwrite", Pak.isOverwrite()));
        Pak.setKeepDeleted(common.getBoolean("keep_deleted", Pak.isKeepDeleted()));
        Pak.setSingly(common.getBoolean("singly", Pak.isSingly()));
        Pak.setVerbose(common.getBoolean("verbose", Pak.isVerbose()));

        if (common.get("source", null) != null) { // detect pak files in source
            File source = new File(common.get("source", null));
            if (source.isDirectory()) {
                Pak.setSources(Arrays.asList(source.listFiles((dir, name) -> name.endsWith(".pak"))));
            } else {
                Pak.setSources(Arrays.asList(source));
            }
        }

        String output = common.get("output", null);
        if (output != null) {
            Pak.setOutput(new File(output));
        } else {
            Pak.setOutput(new File("."));
        }
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt("ini")
                .hasArg()
                .desc("Ini file properties for specific extra configuration.")
                .build());

        options.addOption(Option.builder("O")
                .longOpt("output")
                .hasArg()
                .desc("Output directory for pak files.")
                .build());

        options.addOption(Option.builder("s")
                .longOpt("singly")
                .desc("Extract one pak at a time, in name order.")
                .build());

        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Verbosely outputs what files are being extracted.")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("overwrite")
                .desc("Overwrites files if there is a file collision.")
                .build());

        options.addOption(Option.builder("k")
                .longOpt("keep-deleted")
                .desc("Extracts files that are marked as deleted from pak.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Displays this help message")
                .build());


        CommandLine cli = parser.parse(options, args);

        // gets remaining arguments that could not be parsed
        List<String> remaining = cli.getArgList();

        // if there is no ini and no specific pak file to parse
        if((remaining.size() == 0 && ! cli.hasOption('i')) || cli.hasOption('h')) {
            new HelpFormatter().printHelp("pak", "Dragon Nest Resource.pak extractor program.", options, null, true);
            return;
        }

        if (cli.hasOption('i')) {
            InputStream input;
            File file = new File(cli.getOptionValue('i'));
            System.out.println("Loading properties from " + file.getPath());
            input = new FileInputStream(file);
            loadIniProperties(input);
        }

        if (cli.hasOption('s')) {
            Pak.setSingly(true);
        }

        if (cli.hasOption('o')) {
            Pak.setOverwrite(true);
        }

        if (cli.hasOption('v')) {
            Pak.setVerbose(true);
        }

        if (cli.hasOption('k')) {
            Pak.setKeepDeleted(true);
        }

        if (cli.hasOption('O')) {
            Pak.setOutput(new File(cli.getOptionValue('O')));
        }

        if (! Pak.getOutput().exists()) {
            System.out.println(Pak.getOutput().getPath() + " does not exist. Attempting to create...");
            if (! Pak.getOutput().mkdirs()) {
                System.err.println("Could not create " + Pak.getOutput().getPath());
                return;
            }
        }

        if (! remaining.isEmpty()) {
            List<File> files = new ArrayList<>();
            for (String path : remaining) {
                File file = new File(path);
                if (! file.exists()) {
                    System.err.println(path + " does not exist! Aborting!");
                    return;
                }

                if (file.isDirectory()) { // add all .pak from this directory
                    files.addAll(Arrays.asList(file.listFiles((dir, name) -> name.endsWith(".pak"))));
                } else { // add this file
                    files.add(file);
                }
            }
            Pak.setSources(files);
        }

        List<File> sources = Pak.getSources();
        Collections.sort(sources, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        // setup the white and black list
        List<Pattern> patternList = Pak.getWhiteList();
        for (Pattern pattern : patternList) {
            System.out.println("whitelisting " + pattern.pattern());
        }

        patternList = Pak.getBlackList();
        for (Pattern pattern : patternList) {
            System.out.println("blacklisting " + pattern.pattern());
        }


        System.out.println("overwrite = " + Pak.isOverwrite());
        System.out.println("keep_deleted = " + Pak.isKeepDeleted());
        System.out.println("output = " + Pak.getOutput().getAbsolutePath());
        System.out.println();

        // the queue and parser
        Queue<Runnable> queue = Pak.getQueue();
        List<PakParser> pakParsers = Pak.getParsers();

        // condition for workers to stay alive:
        // 1) more incoming parsers
        // 2) queue is not empty
        // 3) parsing is not done
        Worker.setQueue(queue);

        if (Pak.isSingly()) {
            for (File source : sources) {
                PakParser pakParser = new PakParser(source);
                pakParsers.add(pakParser);
                queue.add(pakParser); // add parser to queue

                // keep going if queue has items or pak parser is still finding files to extract
                Worker.setCondition(condition -> !condition || !pakParser.isDone());
                Worker.startWorkers();
                Worker.awaitTermination();
            }
        } else {
            for (File source : sources) {
                PakParser pakParser = new PakParser(source);
                pakParsers.add(pakParser);
                queue.add(pakParser); // add parser to queue
            }

            // keep going if queue has items or if there is a parser that is still finding files to extract
            Worker.setCondition(condition -> {
                for (PakParser p : pakParsers) {
                    if (! p.isDone()) {
                        return true;
                    }
                }

                return ! condition;
            });

            Worker.startWorkers();

            // wait for all workers to finish
            Worker.awaitTermination();
        }

        System.out.println();

        int total = 0, extracted = 0, skipped = 0;
        String format = "%s = %d";
        for (PakParser pakParser : pakParsers) {
            System.out.println("Finished extracting " + pakParser.getFile().getPath());
            total += pakParser.getTotalFiles();
            extracted += pakParser.getExtractedFiles();
            skipped += pakParser.getSkippeddFiles();
        }

        System.out.println(String.format(format,"\ntotal", total));
        System.out.println(String.format(format,"extracted", extracted));
        System.out.println(String.format(format,"skipped", skipped));

        long endTime = System.currentTimeMillis();
        System.out.println("\nworkers = " + Worker.MAX_WORKERS);
        System.out.println("runtime = " + (endTime - startTime) + " ms");
    }
}
