package dnss.tools.dnt.json;

import dnss.tools.dnt.DNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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

        // Get all skill tables
        List<String> skillTables = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(null, null, "skillleveltable\\_character%pve", null);
        while (rs.next()) {
            String table = rs.getString(3);
            skillTables.add(table.substring(SKILL_TABLE_PREFIX.length(), table.length() - 3));
        }

        List<List<String>> condensedTables = condense(skillTables);

        for (List<String> list : condensedTables) {
            LOG.info(list.toString());
        }

    }

    // probably unneeded as the only corner case is bringer, but meh just to be potentially safe
    private static List<List<String>> condense(List<String> tables) {
        List<List<String>> lists = new ArrayList<>();
        for (String table : tables) {
            List<String> initial = new ArrayList<>();
            initial.add(table);
            lists.add(initial);
        }

        for (String table : tables) {
            for (int i = 0; i < lists.size(); i++) {
                if (table == lists.get(i).get(0)) { // same string
                    continue;
                }

                // eg if assassinbringer startswith assassin
                if (lists.get(i).get(0).startsWith(table)) {
                    String name = lists.get(i).get(0);
                    lists.remove(i);
                    for (List<String> list : lists) {
                        if (list.get(0) == table) {
                            list.add(name);
                            break;
                        }
                    }
                    --i;
                }
            }
        }

        return lists;
    }
}
