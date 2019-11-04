package main.types;

import org.json.JSONObject;

public class CardTransaction {
    private String cid, uid, name, timeStamp, toUsername, toUid;
    private int newTriage;
    private TransactionType transactionType;
    public CardTransaction(String cid, String uid, String name, String timeStamp, int newTriage){
        this.cid = cid;
        this.uid = uid;
        this.name = name;
        this.timeStamp = timeStamp;
        this.newTriage = newTriage;
        this.transactionType = TransactionType.TRIAGE;
    }

    public CardTransaction(String cid, String uid, String name, String timeStamp, String toUsername, String toUid){
        this.cid = cid;
        this.uid = uid;
        this.name = name;
        this.timeStamp = timeStamp;
        this.toUsername = toUsername;
        this.toUid = toUid;
        this.transactionType = TransactionType.ASSIGNMENT;
    }

    /**
     * Convert this transaction object to an equivalent json object
     * @return json equivalent of this object
     */
    public JSONObject convertToJson(){
        JSONObject thisTransObj = new JSONObject();
        thisTransObj.put("uid", uid);
        thisTransObj.put("name", name);
        thisTransObj.put("timeStamp", timeStamp);
        thisTransObj.put("type", this.transactionType.name());
        if(this.transactionType == TransactionType.TRIAGE){
            thisTransObj.put("triage", newTriage);
        }else if (this.transactionType == TransactionType.ASSIGNMENT){
            thisTransObj.put("toUserName", toUsername);
            thisTransObj.put("toUid", toUid);
        }
        return thisTransObj;
    }
    public TransactionType getTransactionType() {
        return transactionType;
    }
    public String getCid() {
        return cid;
    }
    public String getUid() {
        return uid;
    }
    public String getName() {
        return name;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public int getNewTriage() {
        return newTriage;
    }
}
