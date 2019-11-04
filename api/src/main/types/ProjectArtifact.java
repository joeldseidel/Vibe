package main.types;

import org.json.JSONObject;

public class ProjectArtifact {
    private String cid;
    private CardType type;
    private String title;
    private String text;
    private int triage;
    public ProjectArtifact(String cid, CardType type, String title, String text, int triage){
        this.cid = cid;
        this.type = type;
        this.title = title;
        this.text = text;
        this.triage = triage;
    }
    public JSONObject convertToJsonObject(){
        JSONObject thisObj = new JSONObject();
        thisObj.put("cid", this.cid);
        thisObj.put("type", this.type.toString());
        thisObj.put("title", this.title);
        thisObj.put("text", this.text);
        thisObj.put("triage", this.triage);
        return thisObj;
    }
}
