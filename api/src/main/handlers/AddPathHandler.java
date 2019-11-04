package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.listeners.PathListener;
import org.json.JSONObject;


public class AddPathHandler extends HandlerPrototype implements HttpHandler {
    public AddPathHandler(){
        requiredKeys = new String[]{ "path", "cid", "token" };
        handlerName = "Add Path Handler";
    }

    @Override
    public void fulfillRequest(JSONObject requestParams){
        try {
            PathListener.enqueue(requestParams.toString());
        }
        catch (InterruptedException iEx){
            iEx.printStackTrace();
        }
    }
}