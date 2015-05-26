package dnss.tools.dnt.sql.json.mappings;

import java.util.Map;

public class SkillTree {
    private int jobID;
    private String jobSlug;
    private int advancement;
    private Map<Integer, Skill> skills;
    private Map<Integer, String> uiString;

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public String getJobSlug() {
        return jobSlug;
    }

    public void setJobSlug(String jobSlug) {
        this.jobSlug = jobSlug;
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
