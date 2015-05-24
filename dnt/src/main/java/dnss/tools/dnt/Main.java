package dnss.tools.dnt;

import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.prefs.Preferences;

public class Main {
    private final static Logger log = LoggerFactory.getLogger(Main.class);

    private final static String DEFAULT = "dnt.ini";

    private static Preferences ini;


    public static void main(String[] args) throws Exception {
        InputStream input;
        if (args.length > 1) {
            throw new Exception("Too many arguments");
        } else if (args.length == 1) {
            input = new FileInputStream(args[0]);
        } else {
            input = Main.class.getClassLoader().getResourceAsStream(DEFAULT);
        }

        ini = new IniPreferences(input);

        // get list of dnts
        HashSet<String> dnt = new HashSet<>(Arrays.asList(ini.keys()));
        dnt.remove("system");
        dnt.remove("uistring");

        // Get system information
        Preferences system = ini.node("system");
        ini.remove("system");

//        // SQL conn
//        Preferences sql = ini.node("sql");
//        ini.remove("sql");
//        Connection conn = DriverManager.getConnection(
//                sql.get("url", null),
//                sql.get("user", null),
//                sql.get("pass", null));
//
//
//        // turn off auto commit because we're going to insert a bunch of stuff
//        conn.setAutoCommit(false);
//
//        // do the iterations
//
//
//
//        // put everything together
//        conn.commit();
//        conn.close();

        /*
        Properties properties = new Properties();
        byte[] propertiesBytes = new byte[input.available()];
        input.read(propertiesBytes);
        properties.load(new StringReader((new String(propertiesBytes)).replace("\\","\\\\")));
        input.close();

        // set the system properties
        int maxThreads = Integer.valueOf(properties.getProperty("system.max.threads", System.getProperty("max.threads", "1")));
        System.setProperty("max.threads", String.valueOf(maxThreads));

        // ordinary dnt properties
        HashMap<String, DNT> dntMap = new HashMap<String, DNT>();
        for (String name : properties.stringPropertyNames()) {
            if (! name.startsWith("dnt.")) {
                continue;
            }

            String dntId = name.substring(4);
            dntId = dntId.substring(0, dntId.indexOf('.'));
            if (! dntMap.containsKey(dntId)) {
                String prefix = "dnt." + dntId + ".";

                DNT dnt = new DNT();
                dnt.setId(dntId);
                dnt.setLocation(new File(properties.getProperty(prefix + "location")));
                dnt.setDestination(new File(properties.getProperty(prefix + "destination")));
                dntMap.put(dntId, dnt);
            }
        }

        // add message table
        if (properties.containsKey("xml.uistring.location")) {
            DNT dnt = new DNT();
            dnt.setId("messages");
            dnt.setLocation(new File(properties.getProperty("xml.uistring.location")));
            dnt.setDestination(new File(properties.getProperty("xml.uistring.destination")));
            dntMap.put("messages", dnt);
        }

        ExecutorService service = Executors.newFixedThreadPool(maxThreads);
        for (DNT dnt : dntMap.values()) {
            Runnable parser;
            if (dnt.getId().equals("messages")) {
                parser = new XMLParser(dnt);
            } else {
                parser = new DNTParser(dnt);
            }
            service.submit(parser);
        }

        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("Parser has been interrupted!", e);
        }
        */
    }
}
