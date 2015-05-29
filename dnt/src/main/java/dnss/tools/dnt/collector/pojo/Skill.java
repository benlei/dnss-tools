package dnss.tools.dnt.collector.pojo;

import java.util.List;

public class Skill {
    private int id;
    private int nameID;
    private List<Level> levels;
    private int type;
    private String weapons;
    private String parents;
    private int basicSP;
    private int firstSP;
    private int slot;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNameID() {
        return nameID;
    }

    public void setNameID(int nameID) {
        this.nameID = nameID;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public String getWeapons() {
        return weapons;
    }

    public void setWeapons(String weapons) {
        this.weapons = weapons;
    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBasicSP() {
        return basicSP;
    }

    public void setBasicSP(int basicSP) {
        this.basicSP = basicSP;
    }

    public int getFirstSP() {
        return firstSP;
    }

    public void setFirstSP(int firstSP) {
        this.firstSP = firstSP;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
