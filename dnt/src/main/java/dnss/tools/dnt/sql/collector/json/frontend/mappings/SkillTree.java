package dnss.tools.dnt.sql.collector.json.frontend.mappings;

import java.util.Map;

public class SkillTree {
    private int advancement;
    private Map<Integer, Skill> skills;
    private Map<Integer, String> uiString;

    public int getAdvancement() {
        return advancement;
    }

    public void setAdvancement(int advancement) {
        this.advancement = advancement;
    }

    public Map<Integer, Skill> getSkills() {
        return skills;
    }

    public void setSkills(Map<Integer, Skill> skills) {
        this.skills = skills;
    }

    public Map<Integer, String> getUiString() {
        return uiString;
    }

    public void setUiString(Map<Integer, String> uiString) {
        this.uiString = uiString;
    }
}
