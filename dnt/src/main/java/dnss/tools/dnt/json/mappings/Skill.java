package dnss.tools.dnt.json.mappings;

import java.util.List;
import java.util.Map;

public class Skill {
    private int id;
    private int name;
    private List<Level> levels;
    private int type;
    private List<Integer> weapons;
    private Map<Integer, Integer> sp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Integer> weapons) {
        this.weapons = weapons;
    }

    public Map<Integer, Integer> getSp() {
        return sp;
    }

    public void setSp(Map<Integer, Integer> sp) {
        this.sp = sp;
    }
}
