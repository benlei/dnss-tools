package dnss.tools.dnt.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class XMLParser extends AbstractParser {
    private final static Logger log = LoggerFactory.getLogger(DNTParser.class);

    public XMLParser(Connection conn, File file) {
        super(conn, file);
    }

    public void parse() throws Exception {
        File file = getFile();
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            document.getDocumentElement().normalize();
        } catch (Exception e) {
            log.error("Could not open XML document", e);
            return;
        }

        Map<String, Types> fields = new LinkedHashMap<>();
        fields.put("_mid", Types.INTEGER);
        fields.put("_cdata", Types.STRING);
        recreateTable(fields);


        NodeList nodeList = document.getElementsByTagName("message");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ArrayList<Object> values = new ArrayList<>();
            Element element = (Element) nodeList.item(i);
            CharacterData characterData = (CharacterData)element.getFirstChild();
            values.add(Integer.valueOf(element.getAttribute("mid"))); // first col: message id
            values.add(new String(characterData.getData().getBytes())); // second col: cdata
//            values.add(new String(characterData.getData().getBytes(), Charset.forName("UTF-8"))); // second col: the data (to utf8)
            insert(values);
        }
    }

    @Override
    public String getName() {
        String name = super.getName();
        name = name.substring(0, name.length() - 4); // remove extension
        return name;
    }

    @Override
    public String getThreadName() {
        return "XML-"+getName();
    }
}
