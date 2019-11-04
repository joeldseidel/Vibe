package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.ProjectDataManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GetDrawingArtifactHandler extends HandlerPrototype implements HttpHandler {
    public GetDrawingArtifactHandler(){
        requiredKeys = new String[] { "cid", "token" };
    }
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String cid = requestParams.getString("cid");
        ProjectDataManager projectDataManager = new ProjectDataManager();
        List<JSONObject> drawingArtifacts = projectDataManager.getDrawingArtifacts(cid);
        JSONArray drawingJsonArray = new JSONArray();
        for(JSONObject artifact : drawingArtifacts){
            drawingJsonArray.put(artifact);
        }
        JSONObject artifacts = new JSONObject();
        artifacts.put("artifacts", drawingJsonArray);
        returnActionSuccess(artifacts);
    }
}
