package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.ProjectDataManager;
import main.types.ProjectArtifact;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GetProjectArtifactsHandler extends HandlerPrototype implements HttpHandler {
    public GetProjectArtifactsHandler(){
        requiredKeys = new String[] { "pid", "uid", "token" };
    }
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String projId = requestParams.getString("pid");
        String uid = requestParams.getString("uid");
        ProjectDataManager projectDataManager = new ProjectDataManager();
        //Get project artifacts from user id and project id
        List<ProjectArtifact> artifactList = projectDataManager.getArtifacts(projId, uid);
        JSONArray artifactArr = new JSONArray();
        for(ProjectArtifact artifact : artifactList){
            artifactArr.put(artifact.convertToJsonObject());
        }
        returnActionSuccess(new JSONObject().put("artifacts", artifactArr));
    }
}
