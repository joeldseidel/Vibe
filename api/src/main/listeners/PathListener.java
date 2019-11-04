package main.listeners;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import main.data.DatabaseInteraction;
import org.json.JSONObject;

public class PathListener {
    public static BlockingQueue<String> commitQueue = new ArrayBlockingQueue(1024);

    public static void startListener(){
        //Start the listener to the queue for entering lines into the database
        Thread pathQueueListener = new Thread(() -> {
            DatabaseInteraction database = new DatabaseInteraction();
            //Listener loop
            while(true){
                try{
                    //Get the next committed line from the queue
                    JSONObject nextPathObj = new JSONObject(commitQueue.take());
                    String cid = nextPathObj.getString("cid");
                    Blob artifact = database.getBlob();
                    artifact.setBytes(1, nextPathObj.toString().getBytes());
                    String insertLineSql = "INSERT INTO Drawing_Artifacts (cid, artifact, timeStamp) VALUES (?, ?, NOW())";
                    PreparedStatement insertLineStmt = database.prepareStatement(insertLineSql);
                    try{
                        insertLineStmt.setString(1, cid);
                        insertLineStmt.setBlob(2, artifact);
                        database.nonQuery(insertLineStmt);
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                } catch (Exception iEx) {
                    iEx.printStackTrace();
                }
            }
        });
        pathQueueListener.start();
    }
    public static void enqueue(String cmdStr) throws InterruptedException{
        commitQueue.put(cmdStr);
    }
}