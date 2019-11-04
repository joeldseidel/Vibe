package main.types;

import org.json.JSONObject;

public class Project {
    private String name, description, pid;
    public Project(String name, String description, String pid){
        this.name = name;
        this.description = description;
        this.pid = pid;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getPid() {
        return pid;
    }
    public JSONObject convertToJson(){
        JSONObject thisObj = new JSONObject();
        thisObj.put("title", name);
        thisObj.put("description", description);
        thisObj.put("pid", pid);
        return thisObj;
    }
}
