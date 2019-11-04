package main.listeners;

import main.data.DatabaseInteraction;
import main.managers.ProjectDataManager;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AssignListener {
    private static BlockingQueue<String> commitQueue = new ArrayBlockingQueue<String>(1024);
    public static void startListener(){
        Thread cardQueueListener = new Thread(() -> {
            DatabaseInteraction database = new DatabaseInteraction();
            while(true) {
                try {
                    JSONObject requestParams = new JSONObject(commitQueue.take());
                    String uid = requestParams.getString("uid");
                    String cid = requestParams.getString("cid");
                    ProjectDataManager projectDataManager = new ProjectDataManager();
                    projectDataManager.assignCard(uid, cid);
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
