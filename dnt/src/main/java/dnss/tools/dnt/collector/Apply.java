package dnss.tools.dnt.collector;

public enum Apply {
    PvE (0),
    PvP (1);

    public final int type;
    Apply(int type) {
        this.type = type;
    }
}
