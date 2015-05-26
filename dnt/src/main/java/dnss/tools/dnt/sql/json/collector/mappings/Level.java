package dnss.tools.dnt.sql.json.collector.mappings;

import dnss.tools.dnt.sql.json.collector.Apply;

public class Level {
    private int sp;
    private int limit;
    private Mode pvp;
    private Mode pve;

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
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

    public Mode createOrGetMode(Apply type) {
        switch (type) {
            case PvE:
                if (pve == null) {
                    pve = new Mode();
                }
                return pve;
            case PvP:
                if (pvp == null) {
                    pvp = new Mode();
                }
                return pvp;
        }

        throw new RuntimeException("Invalid mode");
    }

    public static class Mode {
        private int explanationID;
        private String explanationParams;
        private double mp;
        private double cd;

        public int getExplanationID() {
            return explanationID;
        }

        public void setExplanationID(int explanationID) {
            this.explanationID = explanationID;
        }

        public String getExplanationParams() {
            return explanationParams;
        }

        public void setExplanationParams(String explanationParams) {
            this.explanationParams = explanationParams;
        }

        public double getMp() {
            return mp;
        }

        public void setMp(double mp) {
            this.mp = mp;
        }

        public double getCd() {
            return cd;
        }

        public void setCd(double cd) {
            this.cd = cd;
        }
    }
}
