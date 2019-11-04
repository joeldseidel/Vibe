package main.types;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Card {
    private String pid, cid, title, content;
    private CardType type;
    private List<CardTransaction> transactions;
    /**
     * Default constructor - takes a card that may or may not be in the database yet
     * @param pid project id this card belongs to
     * @param cid card id hash
     * @param title title of the card
     * @param content content of the card
     * @param type type of the card
     */
    public Card(String pid, String cid, String title, String content, CardType type){
        this.pid = pid;
        this.cid = cid;
        this.title = title;
        this.content = content;
        this.type = type;
    }
    public Card(String cid, String title, String content, CardType type){
        this.cid = cid;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public JSONObject convertToJson() {
        JSONObject thisCardObj = new JSONObject();
        thisCardObj.put("cid", this.getCid());
        thisCardObj.put("title", this.getTitle());
        thisCardObj.put("content", this.getContent());
        thisCardObj.put("type", this.getType().ordinal());
        JSONArray transactions = new JSONArray();
        for(CardTransaction thisTransaction : this.transactions){
            transactions.put(thisTransaction.convertToJson());
        }
        thisCardObj.put("transactions", transactions);
        return thisCardObj;
    }

    public String getPid() {
        return pid;
    }
    public String getCid() {
        return cid;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public CardType getType() {
        return type;
    }
    public void setTransactions(List<CardTransaction> transactions){
        this.transactions = transactions;
    }

}
