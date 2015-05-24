package dnss.tools.dnt.sql.entry;

/**
 * Created by Ben on 5/23/2015.
 */
public abstract class SQLEntry<T> {
    private final String field;
    public SQLEntry(String field) {
        this.field = field;
    }
}
