package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import org.json.JSONObject;

import java.util.List;

public class GetUserDataHandler extends HandlerPrototype implements HttpHandler {
    public GetUserDataHandler(){
        requiredKeys = new String[]{ "get", "uid", "token" };
        handlerName = "Get User Data";
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        List<Object> getAttr = requestParams.getJSONArray("get").toList();
        String uid = requestParams.getString("uid");
        JSONObject userDataObj = getUserData(getAttr, uid);
        returnActionSuccess(userDataObj);
    }

    private JSONObject getUserData(List<Object> get, String uid){
        UserDataManager userDataManager = new UserDataManager();
        List<String> retrievedAttrs = userDataManager.getUserAttributes(get, uid);
        JSONObject userDataObj = new JSONObject();
        for(int i = 0; i < retrievedAttrs.size(); i++){
            userDataObj.put(get.get(i).toString(), retrievedAttrs.get(i));
        }
        return userDataObj;
    }
}
