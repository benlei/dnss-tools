package dnss.tools.dnt.sql.json.collector.mappings;

import java.util.Map;

public class SkillTree {
    private int job;
    private String slug;
    private int advancement;
    private Map<Integer, Skill> skills;
    private Map<Integer, String> uiString;

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

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
