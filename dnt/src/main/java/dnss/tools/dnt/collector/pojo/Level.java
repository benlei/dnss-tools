package dnss.tools.dnt.collector.pojo;

import dnss.tools.dnt.collector.Apply;

public class Level {
    private int sp;
    private int limit;
    private String explanationID;
    private String explanationParams;
    private String mp;
    private String cd;

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

    public String getExplanationID() {
        return explanationID;
    }

    public void setExplanationID(String explanationID) {
        this.explanationID = explanationID;
    }

    public String getExplanationParams() {
        return explanationParams;
    }

    public void setExplanationParams(String explanationParams) {
        this.explanationParams = explanationParams;
    }

    public String getMp() {
        return mp;
    }

    public void setMp(String mp) {
        this.mp = mp;
    }

    public String getCd() {
        return cd;
    }

    public void setCd(String cd) {
        this.cd = cd;
    }
}
