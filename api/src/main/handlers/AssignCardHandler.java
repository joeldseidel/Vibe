package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.listeners.AssignListener;
import main.managers.ProjectDataManager;
import org.json.JSONObject;

public class AssignCardHandler extends HandlerPrototype implements HttpHandler {
    public AssignCardHandler(){
        requiredKeys = new String[] { "uid", "cid", "token" };
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        try{
            AssignListener.enqueue(requestParams.toString());
        } catch (InterruptedException iEx) {
            returnActionFailure();
        }
    }
}
