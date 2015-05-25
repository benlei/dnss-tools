package dnss.tools.dnt.sql.json;

import dnss.tools.dnt.DNT;
import dnss.tools.dnt.sql.json.mappings.Skill;
import dnss.tools.dnt.sql.json.mappings.SkillTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This class should just collect all releveant skill trees, then create it into a json
public class SkillsCollector implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(SkillsCollector.class);

    private String table;
    private Connection conn;
    private static final Map<String, SkillTree> skillTrees = new ConcurrentHashMap<>();

    public SkillsCollector(String table, Connection conn) {
        this.table = table;
        this.conn = conn;
    }

    public void collect() throws SQLException {
        Map<Integer, String> uiStrings = DNT.getUiString();
        Statement stmt = conn.createStatement();

        // Generate the basic skill table
        String query = "SELECT _SkillTableID, _NeedJob, LOWER(_EnglishName), _JobNumber  " +
                "FROM skilltreetable t " +
                "   INNER JOIN skilltable_character s " +
                "       ON(_SkillTableID = s._ID) " +
                "   INNER JOIN jobtable j " +
                "       ON(s._NeedJob = j._ID) " +
                "   INNER JOIN skillleveltable_character" + table + "pve c " +
                "       ON(_SkillTableID = c._SkillIndex)";


        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int skillId = rs.getInt(1);
                int jobId = rs.getInt(2);
                String job = rs.getString(3);
                int advancement = rs.getInt(4);

                SkillTree skillTree;
                if (skillTrees.containsKey(job)) {
                    skillTree = skillTrees.get(job);
                } else {
                    skillTree = new SkillTree();
                    skillTree.setJobId(jobId);
                    skillTree.setAdvancement(advancement);

                    Map<Integer, String> uiString = new HashMap<>();
                    skillTree.setUiString(uiString);

                    Map<Integer, Skill> skills = new HashMap<>();
                    skillTree.setSkills(skills);

                    skillTrees.put(job, skillTree);
                }

                Map<Integer, Skill> skills = skillTree.getSkills();
                if (! skills.containsKey(skillId)) {
                    Skill skill = new Skill(skillId);
                    skills.put(skillId, skill);
                    // need to fill rest of Skill
                }

                // fill up level of each skill
            }
        }

        // fill up pvp mode of each skill

    }

    public static Map<String, SkillTree> getAllSkillTrees() {
        return skillTrees;
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
        return "Collector-" + table;
    }
}
