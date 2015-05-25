package dnss.tools.dnt.sql.json.mappings;

public class Level {
    private int requiredLevel;
    private Mode pvp;
    private Mode pve;

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public Mode getPvp() {
        return pvp;
    }

    public void setPvp(Mode pvp) {
        this.pvp = pvp;
    }

    public Mode getPve() {
        return pve;
    }

    public void setPve(Mode pve) {
        this.pve = pve;
    }

    public static class Mode {
        private int explanationId;
        private String explanationParams;
        private float mp;
        private float cd;

        public int getExplanationId() {
            return explanationId;
        }

        public void setExplanationId(int explanationId) {
            this.explanationId = explanationId;
        }

        public String getExplanationParams() {
            return explanationParams;
        }

        public void setExplanationParams(String explanationParams) {
            this.explanationParams = explanationParams;
        }

        public float getMp() {
            return mp;
        }

        public void setMp(float mp) {
            this.mp = mp;
        }

        public float getCd() {
            return cd;
        }

        public void setCd(float cd) {
            this.cd = cd;
        }
    }
}
