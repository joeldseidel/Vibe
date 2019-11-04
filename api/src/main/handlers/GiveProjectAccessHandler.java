package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.ProjectDataManager;
import org.json.JSONObject;

public class GiveProjectAccessHandler extends HandlerPrototype implements HttpHandler {

    public GiveProjectAccessHandler(){
        requiredKeys = new String[]{"uid","pid","token"};
    }

    @Override
    public void fulfillRequest(JSONObject requestParams){
        String uid = requestParams.getString("uid");
        String pid = requestParams.getString("pid");
        ProjectDataManager projectDataManager = new ProjectDataManager();

        projectDataManager.giveProjectAccess(uid,pid);
        JSONObject pidObj = new JSONObject().put("pid", pid);
        returnActionSuccess(pidObj);
    }

















}
