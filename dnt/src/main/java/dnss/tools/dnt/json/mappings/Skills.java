package dnss.tools.dnt.json.mappings;

import java.util.List;
import java.util.Map;

public class Skills {
    private String job;
    private int advancement;
    private List<Skill> skills;
    private Map<Integer, String> uiString;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getAdvancement() {
        return advancement;
    }

    public void setAdvancement(int advancement) {
        this.advancement = advancement;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public Map<Integer, String> getUiString() {
        return uiString;
    }

    public void setUiString(Map<Integer, String> uiString) {
        this.uiString = uiString;
    }
}
