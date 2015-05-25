package dnss.tools.dnt.sql.json.mappings;

import java.util.List;
import java.util.Map;

public class SkillTree {
    private int jobId;
    private String jobName;
    private int advancement;
    private Map<Integer, Skill> skills;
    private Map<Integer, String> uiString;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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
