package dnss.tools.dnt.sql.collector;

import dnss.tools.dnt.sql.collector.mappings.Level;
import dnss.tools.dnt.sql.collector.mappings.Skill;
import dnss.tools.dnt.sql.collector.mappings.SkillTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dnss.tools.dnt.sql.collector.mappings.Level.Mode;

public class Collector implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(Collector.class);
    private Apply apply;
    private Connection conn;
    private static final Map<String, SkillTree> skillTrees = new ConcurrentHashMap<>();
    private final static Matcher matcher = Pattern.compile("\\{([0-9]+)\\}").matcher("");

    private String table;
    private final static String template;
    private static Map<Integer, String> uiString;

    static {
        String str = null;
        try {
            URL resource = Collector.class.getClassLoader().getResource("sql/collector.sql");
            if (resource != null) {
                str = new String(Files.readAllBytes(Paths.get(resource.toURI())));
            } else {
                LOG.error("resource/sql/collector.sql could not be loaded.");
            }
        } catch (Exception e) {
            LOG.error("Template Loader Error: " + e.getMessage(), e);
        } finally {
            template = str;
        }
    }

    public Collector(String table, Apply apply, Connection conn) {
        this.table = table;
        this.apply = apply;
        this.conn = conn;
    }
    
    public static Map<String, SkillTree> getSkillTrees() {
        return skillTrees;
    }

    public static void setUiString(Map<Integer,String> uiString) {
        Collector.uiString = uiString;
    }

    public void collect() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = String.format(template, table, apply.type);

        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("_SkillTableID");
                int lvl = rs.getInt("_SkillLevel");
                int jobID = rs.getInt("_NeedJob");
                String jobSlug = rs.getString("_EnglishName").toLowerCase();
                int advancement = rs.getInt("_JobNumber");
                int nameID = rs.getInt("_NameID");
                int type = rs.getInt("_SkillType");
                int weapon1 = rs.getInt("_NeedWeaponType1");
                int weapon2 = rs.getInt("_NeedWeaponType2");
                int parentID1 = rs.getInt("_ParentSkillID1");
                int parentLevel1 = rs.getInt("_NeedParentSkillLevel1");
                int parentID2 = rs.getInt("_ParentSkillID2");
                int parentLevel2 = rs.getInt("_NeedParentSkillLevel1");
                int basicSP = rs.getInt("_NeedBasicSP1");
                int firstSP = rs.getInt("_NeedFirstSP1");
                int slot = rs.getInt("_TreeSlotIndex");
                int limit = rs.getInt("_LevelLimit");
                int sp = rs.getInt("_NeedSkillPoint");
                int explanationID = rs.getInt("_SkillExplanationID");
                String explanationParams = rs.getString("_SkillExplanationIDParam");
                double mp = rs.getInt("_DecreaseSP") / 10.0;
                double cd = rs.getInt("_DelayTime") / 1000.0;

                SkillTree skillTree;
                synchronized (skillTrees) { // skill tree lock for when pvp/pve creates skill tree
                    if (skillTrees.containsKey(jobSlug)) {
                        skillTree = skillTrees.get(jobSlug);
                    } else {
                        skillTree = new SkillTree();
                        skillTree.setJob(jobID);
                        skillTree.setAdvancement(advancement);

                        Map<Integer, String> uiString = new ConcurrentHashMap<>();
                        skillTree.setUiString(uiString);

                        Map<Integer, Skill> skills = new HashMap<>();
                        skillTree.setSkills(skills);

                        skillTree.setSlug(jobSlug);
                        skillTrees.put(jobSlug, skillTree);
                    }
                }

                Map<Integer, Skill> skills = skillTree.getSkills();
                Map<Integer, Level> levels;
                synchronized (skills) { // sync condition for when a pvp or pve thread wants to create the skill
                    if (skills.containsKey(id)) {
                        levels = skills.get(id).getLevels();
                    } else {
                        Skill skill = new Skill();
                        levels = new HashMap<>();

                        skill.setId(id);
                        skill.setNameID(nameID);
                        skill.setLevels(levels);
                        skill.setType(type);
                        skill.setWeapon1(weapon1);
                        skill.setWeapon2(weapon2);
                        skill.setParentID1(parentID1);
                        skill.setParentLevel1(parentLevel1);
                        skill.setParentID2(parentID2);
                        skill.setParentLevel2(parentLevel2);
                        skill.setBasicSP(basicSP);
                        skill.setFirstSP(firstSP);
                        skill.setSlot(slot);

                        skills.put(id, skill);
                    }
                }

                Level level;
                synchronized (levels) { // sync for when a pvp and pve thread wants to create/get the level
                    if (levels.containsKey(lvl)) {
                        level = levels.get(lvl);
                    } else {
                        level = new Level();

                        level.setSp(sp);
                        level.setLimit(limit);

                        levels.put(lvl, level);
                    }
                }

                // although a pvp/pve thread may call this method, it's for two different instance variables.
                Mode mode = level.createOrGetMode(apply);
                mode.setMp(mp);
                mode.setCd(cd);
                mode.setExplanationID(explanationID);
                mode.setExplanationParams(explanationParams);

                Map<Integer, String> uiString = skillTree.getUiString();
                if (explanationParams != null) {
                    String[] params = explanationParams.split(",");
                    for (String param : params) {
                        synchronized (matcher) {
                            matcher.reset(param);
                            if (matcher.matches()) { // get parameters uistrings
                                int mid = Integer.valueOf(matcher.group(1));
                                uiString.put(mid, Collector.uiString.get(mid));
                            }
                        }
                    }
                }

                // get skill name and the explanation for skill too
                uiString.put(nameID, Collector.uiString.get(nameID));
                uiString.put(explanationID, Collector.uiString.get(explanationID));
            }
        } catch (SQLException e) { // catch it to also record the query.
            LOG.warn(query);
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            collect();
        } catch (SQLException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Collector-"+table;
    }
}
