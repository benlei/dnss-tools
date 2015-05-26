package dnss.tools.dnt.sql;

import dnss.tools.dnt.DNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class XMLParser extends AbstractParser {
    private final static Logger LOG = LoggerFactory.getLogger(DNTParser.class);

    public XMLParser(Connection conn, File file) {
        super(conn, file);
    }

    public void parse() throws Exception {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getFile());
            document.getDocumentElement().normalize();
        } catch (Exception e) {
            LOG.error("Could not open XML document", e);
            return;
        }

        Map<String, Types> fields = new LinkedHashMap<>();
        fields.put("_ID", Types.INTEGER);
        fields.put("_Message", Types.STRING);
        recreateTable(fields);

        int total = 0;
        int fails = 0;
        NodeList nodeList = document.getElementsByTagName("message");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            CharacterData characterData = (CharacterData) element.getFirstChild();
            try {
                ++total;
                insert(Arrays.asList(new Object[]{Integer.valueOf(element.getAttribute("mid")),
                        characterData.getData().getBytes("UTF-8")}));
            } catch (SQLException e) {
                ++fails;
                if (DNT.isLogQueries()) {
                    LOG.warn(e.getMessage(), e);
                }
            }
        }

        if (fails != 0) {
            LOG.info(fails + " out of " + total + " entries failed to be inserted to " + getName());
        }
    }

    @Override
    public String getName() {
        String name = super.getName();
        name = name.substring(0, name.length() - 4); // remove extension
        return name;
    }

    @Override
    public String toString() {
        return "XML-" + getName();
    }
}
