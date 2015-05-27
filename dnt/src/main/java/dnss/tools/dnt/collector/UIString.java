package dnss.tools.dnt.collector;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UIString {
    private static Map<Integer, String> strings;

    public static void setup(File file) {
        strings = new HashMap<>();
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            document.getDocumentElement().normalize();
        } catch (Exception e) {
            System.err.println("Could not open XML document");
            e.printStackTrace();
            return;
        }


        NodeList nodeList = document.getElementsByTagName("message");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            CharacterData characterData = (CharacterData) element.getFirstChild();
            strings.put(Integer.valueOf(element.getAttribute("mid")), characterData.getData());
        }
    }

    public static String get(int id) {
        return strings.get(id);
    }
}
