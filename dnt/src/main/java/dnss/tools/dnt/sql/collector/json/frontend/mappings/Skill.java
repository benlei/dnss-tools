package dnss.tools.dnt.sql.collector.json.frontend.mappings;

import java.util.Map;

public class Skill {
    private int id;
    private int nameID;
    private Map<Integer, Level> levels;
    private int icon;
    private int type;
    private int weapon1;
    private int weapon2;
    private int parentID1;
    private int parentLevel1;
    private int parentID2;
    private int parentLevel2;
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

    public Map<Integer, Level> getLevels() {
        return levels;
    }

    public void setLevels(Map<Integer, Level> levels) {
        this.levels = levels;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWeapon1() {
        return weapon1;
    }

    public void setWeapon1(int weapon1) {
        this.weapon1 = weapon1;
    }

    public int getWeapon2() {
        return weapon2;
    }

    public void setWeapon2(int weapon2) {
        this.weapon2 = weapon2;
    }

    public int getParentID1() {
        return parentID1;
    }

    public void setParentID1(int parentID1) {
        this.parentID1 = parentID1;
    }

    public int getParentLevel1() {
        return parentLevel1;
    }

    public void setParentLevel1(int parentLevel1) {
        this.parentLevel1 = parentLevel1;
    }

    public int getParentID2() {
        return parentID2;
    }

    public void setParentID2(int parentID2) {
        this.parentID2 = parentID2;
    }

    public int getParentLevel2() {
        return parentLevel2;
    }

    public void setParentLevel2(int parentLevel2) {
        this.parentLevel2 = parentLevel2;
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
