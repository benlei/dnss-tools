package dnss.tools.dnt.processor;

import dnss.tools.common.worker.Worker;
import dnss.tools.dnt.DNT;
import org.apache.commons.cli.*;
import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.prefs.Preferences;

public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Verbosely outputs what files are being extracted.")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg()
                .desc("Specific file to process. Specify more of this option to process multiple DNT files.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Displays this help message")
                .build());


        CommandLine cli = parser.parse(options, args);

        // gets remaining arguments that could not be parsed
        List<String> remaining = cli.getArgList();

        if (remaining.size() != 1 || cli.hasOption('h')) {
            new HelpFormatter().printHelp("dnt-processor", "DNSS processor processes DNT files and inserts all " +
                            "of its content into a database. If the file option is not specified, all the " +
                            "{resource.location}/resource/ext/*.dnt files will be processored. This program " +
                            "requires an ini file to be specified in its arguments.",
                    options, null, true);
            return;
        }


        InputStream input = new FileInputStream(remaining.get(0));
        Config.getGlobal().setEscape(false);
        Preferences ini = new IniPreferences(input);

        // SQL conn
        Preferences sql = ini.node("sql");
        Connection conn = DriverManager.getConnection(
                sql.get("url", null),
                sql.get("user", null),
                sql.get("pass", null));
        DNT.setVerbose(sql.getBoolean("verbose", DNT.isVerbose()));

        // auto commit off
        conn.setAutoCommit(false);

        // start queueing up the jobs
        Queue<Runnable> queue = DNT.getQueue();

        // start the workers
        Worker.setQueue(queue);
        if (cli.hasOption('f')) {
            String[] files = cli.getOptionValues('f');
            for (String f : files) {
                File file = new File(f);
                if (! file.exists()) {
                    System.err.println(f + " does not exist! Aborting!");
                    return;
                }

                queue.add(new DNTProcessor(conn, file));
            }
        } else {
            File root = new File(ini.node("common").get("output", null));
            File ext = new File(root, "resource/ext");
            List<File> files = Arrays.asList(ext.listFiles((dir, name) -> name.endsWith(".dnt")));

            for (File file : files) {
                queue.add(new DNTProcessor(conn, file));
            }
        }

        Worker.startWorkers();
        Worker.awaitTermination();

        conn.commit();
        conn.close();

        long endTime = System.currentTimeMillis();
        System.out.println("\nworkers = " + Worker.MAX_WORKERS);
        System.out.println("runtime = " + (endTime - startTime) + " ms");
    }
}
