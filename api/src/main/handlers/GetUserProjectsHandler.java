package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import main.types.Project;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GetUserProjectsHandler extends HandlerPrototype implements HttpHandler {
    public GetUserProjectsHandler(){
        requiredKeys = new String[] { "uid", "token" };
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String uid = requestParams.getString("uid");
        UserDataManager userDataManager = new UserDataManager();
        List<Project> userProjects = userDataManager.getUserProjects(uid);
        JSONArray userProjectArr = new JSONArray();
        for(Project thisProject : userProjects){
            userProjectArr.put(thisProject.convertToJson());
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("projects", userProjectArr);
        returnActionSuccess(returnObj);
    }
}
