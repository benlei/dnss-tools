package dnss.tools.dnt.sql;

import dnss.tools.dnt.DNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractParser implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractParser.class);

    private final Connection conn;
    private final File file;
    private Map<String, Types> fields;

    public AbstractParser(Connection conn, File file) {
        this.conn = conn;
        this.file = file;
    }

    public Connection getConn() {
        return conn;
    }

    public File getFile() {
        return file;
    }

    public void recreateTable(Map<String, Types> fields) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            String table = getName();

            // first drop table
            String query = "DROP TABLE IF EXISTS " + table;
            if (DNT.isLogQueries()) {
                LOG.info(query);
            }

            stmt.executeUpdate(query);

            // recreate table
            query = "CREATE TABLE " + table + "(";
            Iterator<Map.Entry<String, Types>> iterator = fields.entrySet().iterator();
            boolean isFirst = true;
            while (iterator.hasNext()) {
                Map.Entry<String, Types> entry = iterator.next();

                query += entry.getKey() + " " + entry.getValue();
                if (isFirst) {
                    query += " PRIMARY KEY";
                }

                isFirst = false;
                if (iterator.hasNext()) {
                    query += ",";
                }
            }
            query += ")";

            if (DNT.isLogQueries()) {
                LOG.info(query);
            }

            stmt.executeUpdate(query);
            this.fields = fields; // since this was all okay, we'll keep it for inserting later
        } finally {
            stmt.close();
        }
    }


    public void insert(List<Object> list) throws Exception {
        String query = "INSERT INTO " + getName() + " VALUES(";

        for (int i = 0; i < list.size(); i++) {
            query += "?";
            if (i < list.size() - 1) {
                query += ",";
            }
        }
        query += ")";

        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            Iterator<Types> iterator = fields.values().iterator();
            for (int i = 0, j = 1; iterator.hasNext(); i++, j++) {
                Types type = iterator.next();
                switch (type) {
                    case STRING:
                        String str = (String) list.get(i);
                        stmt.setString(j, str.length() == 0 ? null : str);
                        break;
                    case INTEGER:
                        stmt.setInt(j, (Integer) list.get(i));
                        break;
                    case BOOLEAN:
                        stmt.setBoolean(j, (Boolean) list.get(i));
                        break;
                    case FLOAT:
                        stmt.setFloat(j, (Float) list.get(i));
                        break;
                }
            }

            if (DNT.isLogQueries()) {
                LOG.info(stmt.toString());
            }

            stmt.executeUpdate();
        } finally {
            stmt.close();
        }
    }

    public abstract String getThreadName();

    public String getName() {
        return file.getName(); // remove .dnt/xml at end
    }

    abstract void parse() throws Exception;

    @Override
    public void run() {
        try {
            LOG.info("Starting to parse " + file.getAbsolutePath());
            parse();
            LOG.info(file.getAbsolutePath() + " has been put into the SQL table " + getName());
        } catch (Exception e) {
            LOG.error("There was an error when parsing " + file.getAbsolutePath(), e);
        }
    }

    public enum Types {
        STRING  ("TEXT"),
        BOOLEAN ("BOOLEAN"),
        INTEGER ("INTEGER"),
        FLOAT   ("REAL");

        public String fieldType;
        Types(String fieldType) {
            this.fieldType = fieldType;
        }

        public static Types getType(int b) {
            switch (b) {
                case 1: return STRING;
                case 2: return BOOLEAN;
                case 3: return INTEGER;
                case 4:case 5: return FLOAT;
                default: throw new RuntimeException("Cannot resolve type " + b);
            }
        }

        @Override
        public String toString() {
            return fieldType;
        }
    }
}
