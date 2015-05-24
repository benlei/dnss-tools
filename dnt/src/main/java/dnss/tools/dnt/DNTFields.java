package dnss.tools.dnt;

import java.util.Map;

/**
 * Created by Ben on 3/16/2015.
 */
public class DNTFields {
    private StringBuilder buf = new StringBuilder();
    public static String id = "_ID";

    public DNTFields(DNT dnt) {
        buf.append("DROP TABLE IF EXISTS " + dnt.getId() + ";\n");
        buf.append("CREATE TABLE " + dnt.getId() + "(\n");
        buf.append("  " + id + " serial PRIMARY KEY");

    }

    public void accumulate(Map.Entry<String, Types> item) {
        buf.append(",\n");
        buf.append("  " + item.getKey() + " " + item.getValue().FIELD);
    }

    public StringBuilder dissipate() {
        StringBuilder builder = buf;
        builder.append(");\n");
        buf = null; // if you are dissipating, can't use this object anymore
        return builder;
    }
}
