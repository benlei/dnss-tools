package dnss.tools.dnt.processor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public class DNTProcessor extends AbstractProcessor {
    public DNTProcessor(Connection conn, File file) {
        super(conn, file);
    }

    public void parse() throws Exception {
        // open the file + get channel
        RandomAccessFile inStream = new RandomAccessFile(getFile(), "r");
        FileChannel channel = inStream.getChannel();
        ByteBuffer buf = channel.map(READ_ONLY, 4, getFile().length() - 4); // it's already flipped
        buf.order(LITTLE_ENDIAN);

        // # of cols EXCLUDING the Id column.
        int numCols = buf.getShort();
        int numRows = buf.getInt();

        Map<String, Types> fields = new LinkedHashMap<>();
        fields.put("_ID", Types.INTEGER);
        for (int i = 0; i < numCols; i++) {
            byte[] fieldNameBytes = new byte[buf.getShort()];
            buf.get(fieldNameBytes);
            fields.put(new String(fieldNameBytes), Types.getType(buf.get()));
        }
        recreateTable(fields);

        for (int i = 0; i < numRows; i++) {
            ArrayList<Object> values = new ArrayList<>();
            for (Types type: fields.values()) {
                switch (type) {
                    case STRING:
                        byte[] bytes = new byte[buf.getShort()];
                        buf.get(bytes);
                        values.add(new String(bytes).replaceAll("\u0000", ""));
                        break;
                    case BOOLEAN:
                        values.add(buf.getInt() != 0);
                        break;
                    case INTEGER:
                        values.add(buf.getInt());
                        break;
                    case FLOAT:
                        values.add(buf.getFloat());
                        break;
                }
            }

            try {
                insert(values);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        channel.close();
        inStream.close();
    }

    @Override
    public String getName() {
        String name = super.getName();
        name = name.substring(0, name.length() - 4); // remove extension
        return name;
    }
}
