package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.ProjectDataManager;
import main.types.Card;
import main.types.CardType;
import org.json.JSONObject;

public class CreateCardHandler extends HandlerPrototype implements HttpHandler {
    public CreateCardHandler(){
        requiredKeys = new String[] { "uid", "pid", "cid", "newCard", "token" };
    }
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        //Who made this card?
        String uid = requestParams.getString("uid");
        //Parse create card request object to a real card
        Card newCard = getCardFromRequest(requestParams);
        ProjectDataManager projectDataManager = new ProjectDataManager();
        projectDataManager.createCard(newCard, uid);
        //NOTE: there is no return on this handler because the WebSockets server isn't listening for one
        //The handler prototype class is going to send one just because it does that for every handler
        //but its just going to die on arrival
    }

    /**
     * Parse a card object out of the request parameter object
     * @param requestParams request parameter object
     * @return an equivalent card object
     */
    private Card getCardFromRequest(JSONObject requestParams){
        String pid = requestParams.getString("pid");
        String cid = requestParams.getString("cid");
        String title = requestParams.getJSONObject("newCard").getString("title");
        String content = requestParams.getJSONObject("newCard").getString("text");
        int type = requestParams.getJSONObject("newCard").getInt("type");
        return new Card(pid, cid, title, content, CardType.values()[type]);
    }
}
