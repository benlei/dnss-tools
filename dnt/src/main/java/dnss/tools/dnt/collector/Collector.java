package dnss.tools.dnt.collector;

import dnss.tools.dnt.DNT;
import dnss.tools.dnt.collector.pojo.Level;
import dnss.tools.dnt.collector.pojo.Skill;
import dnss.tools.dnt.collector.pojo.SkillTree;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collector implements Runnable {
    private Apply apply;
    private Connection conn;
    private static final Map<String, SkillTree> skillTrees = new ConcurrentHashMap<>();
    private final static Matcher matcher = Pattern.compile("\\{([0-9]+)\\}").matcher("");

    private String table;
    private final static String template;

    static {
        String str = null;
        try {
            URL resource = Collector.class.getClassLoader().getResource("collector.sql");
            if (resource != null) {
                str = new String(Files.readAllBytes(Paths.get(resource.toURI())));
            } else {
                System.err.println("collector.sql could not be loaded.");
            }
        } catch (Exception e) {
            System.err.println("Template Loader Error: " + e.getMessage());
            e.printStackTrace();
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

    public void collect() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = String.format(template, table, apply.type);

        if (DNT.isVerbose()) {
            System.out.println(query);
        }

        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("_SkillTableID");
                int lvl = rs.getInt("_SkillLevel") - 1;
                String slug = rs.getString("_EnglishName").toLowerCase();
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
                    if (skillTrees.containsKey(slug)) {
                        skillTree = skillTrees.get(slug);
                    } else {
                        skillTree = new SkillTree();
                        skillTree.setAdvancement(advancement);

                        Map<Integer, String> uiString = new ConcurrentHashMap<>();
                        skillTree.setUiString(uiString);

                        Map<Integer, Skill> skills = new HashMap<>();
                        skillTree.setSkills(skills);

                        skillTrees.put(slug, skillTree);
                    }
                }

                Map<Integer, Skill> skills = skillTree.getSkills();
                List<Level> levels;
                synchronized (skills) { // sync condition for when a pvp or pve thread wants to create the skill
                    if (skills.containsKey(id)) {
                        levels = skills.get(id).getLevels();
                    } else {
                        Skill skill = new Skill();
                        levels = new ArrayList<>();
                        skill.setId(id);
                        skill.setNameID(nameID);
                        skill.setLevels(levels);
                        skill.setType(type);
                        if (weapon1 == -1 && weapon2 != -1) {
                            skill.setWeapons(Integer.toString(weapon2));
                        } else if (weapon1 != -1 && weapon2 == -1) {
                            skill.setWeapons(Integer.toString(weapon1));
                        } else if (weapon1 != -1) {
                            skill.setWeapons(weapon1 + "," + weapon2);
                        }

                        if (parentID2 != 0) {
                            skill.setParents(parentID1 + ":" + parentLevel1 + "," + parentID2 + ":" + parentLevel2);
                        } else if (parentID1 != 0) {
                            skill.setParents(parentID1 + ":" + parentLevel1);
                        }

                        skill.setBasicSP(basicSP);
                        skill.setFirstSP(firstSP);
                        skill.setSlot(slot);

                        skills.put(id, skill);
                    }
                }

                Level level;
                synchronized (levels) { // sync for when a pvp and pve thread wants to create/get the level
                    if (lvl < levels.size() && levels.get(lvl) != null) {
                        level = levels.get(lvl );
                    } else {
                        level = new Level();

                        level.setSp(sp);
                        level.setLimit(limit);

                        ensureSize(levels, lvl);
                        levels.set(lvl, level);
                    }
                }

                synchronized (level) {
                    String lMP = level.getMp();
                    String lCD = level.getCd();
                    String lEID = level.getExplanationID();
                    String lEP = level.getExplanationParams();

                    if (lMP == null) {
                        level.setMp(String.valueOf(mp));
                    } else if(! lMP.equals(String.valueOf(mp))) {
                        switch (apply) {
                            case PvE:
                                level.setMp(mp + "," + lMP);
                                break;
                            case PvP:
                                level.setMp(lMP + "," + mp);
                                break;
                        }
                    }

                    if (lCD == null) {
                        level.setCd(String.valueOf(cd));
                    } else if (! lCD.equals(String.valueOf(cd))) {
                        switch (apply) {
                            case PvE:
                                level.setCd(cd + "," + lCD);
                                break;
                            case PvP:
                                level.setCd(lCD + "," + cd);
                                break;
                        }
                    }

                    if (lEID == null) {
                        level.setExplanationID(String.valueOf(explanationID));
                    } else if (! lEID.equals(String.valueOf(explanationID))) {
                        switch (apply) {
                            case PvE:
                                level.setExplanationID(explanationID + "," + lEID);
                                break;
                            case PvP:
                                level.setExplanationID(lEID + "," + explanationID);
                                break;
                        }
                    }

                    if (lEP == null) {
                        level.setExplanationParams(explanationParams);
                    } else if (! lEP.equals(explanationParams)) {
                        switch (apply) {
                            case PvE:
                                level.setExplanationParams(explanationParams + "|" + lEP);
                                break;
                            case PvP:
                                level.setExplanationParams(lEP + "|" + explanationParams);
                                break;
                        }
                    }
                }


                Map<Integer, String> uiString = skillTree.getUiString();
                if (explanationParams != null) {
                    String[] params = explanationParams.split(",");
                    for (String param : params) {
                        synchronized (matcher) {
                            matcher.reset(param);
                            if (matcher.matches()) { // get parameters uistrings
                                int mid = Integer.valueOf(matcher.group(1));
                                uiString.put(mid, UIString.get(mid));
                            }
                        }
                    }
                }

                // get skill name and the explanation for skill too
                uiString.put(nameID, UIString.get(nameID));
                uiString.put(explanationID, UIString.get(explanationID));
            }
        } catch (SQLException e) { // catch it to also record the query.
            System.err.println(query);
            throw e;
        }
    }


    private void ensureSize(List list, int size) {
        if (size < list.size()) {
            return;
        }

        for (int i = list.size(); i <= size; i++) {
            list.add(null);
        }
    }

    @Override
    public void run() {
        try {
            collect();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
