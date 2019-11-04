package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.ProjectDataManager;
import main.types.Card;
import org.json.JSONObject;

public class GetCardDetailHandler extends HandlerPrototype implements HttpHandler {
    public GetCardDetailHandler(){
        requiredKeys = new String[] { "cid", "token" };
    }
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String cid = requestParams.getString("cid");
        ProjectDataManager projectDataManager = new ProjectDataManager();
        Card thisCard = projectDataManager.getCard(cid);
        JSONObject cardDetailsObj = thisCard.convertToJson();
        returnActionSuccess(cardDetailsObj);
    }
}
