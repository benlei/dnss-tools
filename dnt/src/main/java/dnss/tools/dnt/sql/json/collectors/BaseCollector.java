package dnss.tools.dnt.sql.json.collectors;

import dnss.tools.dnt.sql.json.mappings.SkillTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class BaseCollector implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(BaseCollector.class);

    private String table;
    private Connection conn;
    private static final Map<String, SkillTree> skillTrees = new ConcurrentHashMap<>();

    public BaseCollector(String table, Connection conn) {
        this.table = table;
        this.conn = conn;
    }

    public String getTable() {
        return table;
    }

    public Connection getConn() {
        return conn;
    }

    public static Map<String, SkillTree> getSkillTrees() {
        return skillTrees;
    }

    public final void collect() throws SQLException {
        collectPve();
        collectPvp();
    }

    public abstract void collectPve() throws SQLException;

    public abstract void collectPvp() throws SQLException;

    @Override
    public void run() {
        try {
            collect();
        } catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Collector-" + table;
    }
}
