package dnss.tools.dnt.sql.json.collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class UnifiedCollector extends BaseCollector {
    private final static Logger LOG = LoggerFactory.getLogger(UnifiedCollector.class);

    public UnifiedCollector(String table, Connection conn) {
        super(table, conn);
    }

    @Override
    public void collectPve() throws SQLException {

    }

    @Override
    public void collectPvp() throws SQLException {

    }
}
