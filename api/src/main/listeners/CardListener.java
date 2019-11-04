package main.listeners;

import main.data.DatabaseInteraction;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CardListener {
    private static BlockingQueue<String> commitQueue = new ArrayBlockingQueue<String>(1024);
    public static void startListener(){
        Thread cardQueueListener = new Thread(() -> {
            DatabaseInteraction database = new DatabaseInteraction();
            while(true) {
                try {
                    String cardCmdObjStr = commitQueue.take();
                    if(cardCmdObjStr != null){
                        JSONObject cardCmdObj = new JSONObject(cardCmdObjStr);
                        String cmdType = cardCmdObj.getString("type");
                        String createTransactionSql = "INSERT INTO Card_Transactions (cid, uid, timeStamp) VALUES (?, ?, NOW())";
                        PreparedStatement createTransactionStmt = database.prepareStatement(createTransactionSql);
                        createTransactionStmt.setString(1, cardCmdObj.getString("cid"));
                        createTransactionStmt.setString(2, cardCmdObj.getString("uid"));
                        int tid = database.nonQueryWithIdCallback(createTransactionStmt);
                        if (cmdType.equals("update-triage")) {
                            String updateTriageSql = "INSERT INTO Triages (tid, new_triage) VALUES (?, ?)";
                            PreparedStatement updateTriageStmt = database.prepareStatement(updateTriageSql);
                            updateTriageStmt.setInt(1, tid);
                            updateTriageStmt.setInt(2, cardCmdObj.getInt("newTriage"));
                            database.nonQuery(updateTriageStmt);
                        }
                    }
                } catch (SQLException sqlEx){
                    sqlEx.printStackTrace();
                } catch (InterruptedException iEx) {
                    iEx.printStackTrace();
                }
            }
        });
        cardQueueListener.start();
    }
    public static void enqueue(String cmdStr) throws InterruptedException{
        commitQueue.put(cmdStr);
    }
}
