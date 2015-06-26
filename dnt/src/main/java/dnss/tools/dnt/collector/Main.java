package dnss.tools.dnt.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import dnss.tools.common.worker.Worker;
import dnss.tools.dnt.DNT;
import dnss.tools.dnt.collector.pojo.SkillTree;
import org.apache.commons.cli.*;
import org.ini4j.Config;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
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

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Displays this help message")
                .build());


        CommandLine cli = parser.parse(options, args);

        // gets remaining arguments that could not be parsed
        List<String> remaining = cli.getArgList();

        if (remaining.size() != 1 || cli.hasOption('h')) {
            new HelpFormatter().printHelp("skilltree-collector", "Skill Tree Collector queries database to build up " +
                            "each DragonNest job's skill tree, and output as a JSON file. This program requires an " +
                            "ini file to be specified in its arguments.",
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

        if (cli.hasOption('v')) {
            DNT.setVerbose(true);
        }

        // load the uistring
        UIString.setup(new File(ini.node("common").get("output", null), "resource/uistring/uistring.xml"));

        // Get all skill tables
        DatabaseMetaData metaData = conn.getMetaData();
        Queue<Runnable> queue = DNT.getQueue();
        Set<String> set = new HashSet<>();
        Worker.setQueue(queue);
        try(ResultSet rs = metaData.getTables(null, null, "skillleveltable\\_character%", null)) {
            while (rs.next()) {
                String table = rs.getString(3);
                if (table.endsWith("_pkey")) { //postgresql :(
                    table = table.substring(0, table.length() - 5);
                }


                if (! set.contains(table) && ! table.endsWith("characteretc")) {
                    if (! table.endsWith("pve")) { // must be a PvP table if not pve (or DA)
                        queue.add(new Collector(table, Apply.PvP, conn));
                    }
                    if (! table.endsWith("pvp")) { // must be a PvE table if not pvp (or DA)
                        queue.add(new Collector(table, Apply.PvE, conn));
                    }
                    set.add(table);
                }
            }
            rs.close();
        }

        Worker.startWorkers();
        Worker.awaitTermination();

        System.out.println();
        File ext = new File(ini.node("common").get("output", null), "resource/ext");
        Map<String, SkillTree> skillTrees = Collector.getSkillTrees();
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, SkillTree> entry : skillTrees.entrySet()) {
            File output = new File(ext, entry.getKey().toLowerCase() + ".json");
            System.out.println("Creating " + output.getAbsolutePath());
            mapper.writeValue(output, entry.getValue());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("\nworkers = " + Worker.MAX_WORKERS);
        System.out.println("runtime = " + (endTime - startTime) + " ms");
    }
}
