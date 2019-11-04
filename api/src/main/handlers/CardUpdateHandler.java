package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.listeners.CardListener;
import org.json.JSONObject;

public class CardUpdateHandler extends HandlerPrototype implements HttpHandler {
    public CardUpdateHandler(){
        requiredKeys = new String[] { "type", "cid", "uid", "token" };
        handlerName = "Card Update Handler";
    }
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        try {
            CardListener.enqueue(requestParams.toString());
        } catch (InterruptedException iEx){
            iEx.printStackTrace();
        }
    }
}
