package dnss.tools.dnt;

import java.util.ArrayList;
import java.util.Map;

public class DNTEntries {
    private StringBuilder buf = new StringBuilder();
    private ArrayList<Map.Entry<String, Types>> fields;

    public DNTEntries(DNT dnt, ArrayList<Map.Entry<String, Types>> fields) {
        this.fields = fields;

        buf.append("INSERT INTO " + dnt.getId() + " (");
        for (Map.Entry<String, Types> pair : fields) {
            buf.append(pair.getKey());
            buf.append(',');
        }

        buf.deleteCharAt(buf.length() - 1);
        buf.append(") VALUES \n");
    }

    public void accumulate(ArrayList<Object> element) {
        buf.append("  (" + element.get(0));
        for (int i = 1; i < element.size(); i++) {
            buf.append(',');
            switch (fields.get(i).getValue()) {
                case STRING:
                    String insert = element.get(i).toString();
                    insert = insert.replaceAll("'", "''");
                    insert = insert.isEmpty() ? "null" : "'" + insert + "'";


                    buf.append(insert);
                    break;
                default:
                    buf.append(fields.get(i).getValue().TYPE.cast(element.get(i)));
                    break;
            }
        }
        buf.append("),\n");
    }

    public StringBuilder dissipate() {
        StringBuilder builder = buf;
        builder.delete(builder.length() - 2, builder.length());
        builder.append(';');
        buf = null; // if you dissipate, can't use this object anymore
        return builder;
    }
}
