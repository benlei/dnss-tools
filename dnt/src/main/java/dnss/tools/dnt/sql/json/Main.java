package dnss.tools.dnt.sql.json;

import dnss.tools.common.worker.Worker;
import dnss.tools.dnt.DNT;
import dnss.tools.dnt.sql.json.collectors.BaseCollector;
import dnss.tools.dnt.sql.json.collectors.SplitCollector;
import dnss.tools.dnt.sql.json.collectors.UnifiedCollector;
import dnss.tools.dnt.sql.json.mappings.SkillTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.prefs.Preferences;

public class Main {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);
    private final static String DEFAULT_INI = "dnt.ini";
    private final static String SKILL_TABLE_PREFIX = "skillleveltable_character";

    private static void showManual() {
        System.out.println("Usage: sql-json [INI_FILE]");
        System.out.println("'sql-json' Uses SQL table information to gather the skill JSON files");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  sql-json\t\t# Uses the default dnt.ini to generate JSON files");
        System.out.println("  sql-json \"C:\\dnt.ini\" \t\t# Uses the C:\\dnt.ini file settings for JSON generation");
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

        // fill the uistring
        Map<Integer, String> uiString = DNT.getUiString();
        Statement stmt = conn.createStatement();
        try(ResultSet rs = stmt.executeQuery("SELECT * FROM uistring")) {
            while (rs.next()) {
                uiString.put(rs.getInt(1), rs.getString(2));
            }
            rs.close();
        }

        // Get all skill tables
        Set<String> skillLevelTables = new HashSet<>();
        DatabaseMetaData metaData = conn.getMetaData();
        Queue<Runnable> queue = DNT.getQueue();
        Worker.setQueue(queue);
        try(ResultSet rs = metaData.getTables(null, null, "skillleveltable\\_character%", null)) {
            while (rs.next()) {
                String table = rs.getString(3);
                if (table.endsWith("characteretc")) {
                    continue; // ignore for now
                } else if (table.endsWith("pvp") || table.endsWith("pve")) {
                    table = table.substring(SKILL_TABLE_PREFIX.length(), table.length() - 3);
                    skillLevelTables.add(table);
                    queue.add(new SplitCollector(table, conn));
                } else { // atm DA has not been added to the roster
//                    table = table.substring(SKILL_TABLE_PREFIX.length());
//                    skillLevelTables.add(table);
//                    queue.add(new UnifiedCollector(table, conn));
                }

            }
            rs.close();
        }

        Worker.startWorkers();
        Worker.awaitTermination();

        Map<String, SkillTree> skillTrees = BaseCollector.getSkillTrees();
        for (Map.Entry<String, SkillTree> entry : skillTrees.entrySet()) {
            LOG.info(entry.getKey() + ".json can be made!");
        }

        long endTime = System.currentTimeMillis();
        LOG.info("===================================================================");
        LOG.info("[system] workers = " + Worker.MAX_WORKERS);
        LOG.info("[system] runtime = " + (endTime - startTime) + " ms");
    }
}
