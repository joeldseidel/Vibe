package main.managers;

import main.data.DatabaseInteraction;
import main.types.Card;
import main.types.CardTransaction;
import main.types.CardType;
import main.types.ProjectArtifact;
import org.json.JSONObject;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectDataManager {
    private DatabaseInteraction database;
    public ProjectDataManager(){
        this.database = new DatabaseInteraction();
    }

    /**
     * Get all of the cards in the project from persistence
     * @param pid project id
     * @param uid user id request (for validation)
     * @return List of project artifact objects
     */
    public List<ProjectArtifact> getArtifacts(String pid, String uid) {
        String getProjectCardsSql = "SELECT ProjectAccess.pid, Cards.cardType, Cards.title, Cards.content, Cards.cid AS cid FROM ProjectAccess INNER JOIN Cards ON Cards.pid = ProjectAccess.pid WHERE ProjectAccess.uid = ? AND ProjectAccess.pid = ?";
        PreparedStatement getProjectCardsStmt = database.prepareStatement(getProjectCardsSql);
        String getCardTriageSql = "SELECT Triages.new_triage AS triage FROM Card_Transactions INNER JOIN Triages ON Triages.tid = Card_Transactions.tid WHERE Card_Transactions.cid = ? ORDER BY Card_Transactions.timeStamp DESC LIMIT 1";
        List<ProjectArtifact> artifacts = new ArrayList<>();
        try{
            //Get the card attributes for a unique card associated with this project
            getProjectCardsStmt.setString(1, uid);
            getProjectCardsStmt.setString(2, pid);
            ResultSet getProjectCardsResults = database.query(getProjectCardsStmt);
            while(getProjectCardsResults.next()){
                //Get the triage value as the most recent transaction
                String cid = getProjectCardsResults.getString("cid");
                PreparedStatement getCardTriageStmt = database.prepareStatement(getCardTriageSql);
                getCardTriageStmt.setString(1, cid);
                ResultSet getCardTriageResults = database.query(getCardTriageStmt);
                if(getCardTriageResults.next()){
                    //Get the card attributes and triage - combine the two queries
                    //Card attributes
                    String title = getProjectCardsResults.getString("Cards.title");
                    String content = getProjectCardsResults.getString("Cards.content");
                    CardType type = CardType.values()[getProjectCardsResults.getInt("Cards.cardType")];
                    //Triage value
                    int triage = getCardTriageResults.getInt("triage");
                    artifacts.add(new ProjectArtifact(cid, type, title, content, triage));
                }
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return artifacts;
    }

    /**
     * Get the title and description of the project
     * @param pid project id hash
     * @return String array of title and description
     */
    public String[] getHeadline(String pid){
        String getHeadlineSql = "SELECT ProjName, ProjDesc FROM Projects WHERE pid = ?";
        PreparedStatement getHeadlineStmt = database.prepareStatement(getHeadlineSql);
        String[] headlineComponents = new String[2];
        try{
            getHeadlineStmt.setString(1, pid);
            ResultSet getHeadlineResults = database.query(getHeadlineStmt);
            if(getHeadlineResults.next()){
                String title = getHeadlineResults.getString("ProjName");
                String desc = getHeadlineResults.getString("ProjDesc");
                headlineComponents[0] = title;
                headlineComponents[1] = desc;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return headlineComponents;
    }

    /**
     * Create a new card
     * @param newCard card object to create
     * @param uid who made this card? will need to initialize the card transactions
     */
    public void createCard(Card newCard, String uid){
        insertNewCard(newCard);
        initNewCard(newCard.getCid(), uid);
    }

    /**
     * Write a new card record to the database
     * @param newCard new card object to write
     */
    private void insertNewCard(Card newCard){
        String insertCardSql = "INSERT INTO Cards (cid, pid, cardType, title, content) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertCardStmt = database.prepareStatement(insertCardSql);
        try {
            //Get card elements and insert
            insertCardStmt.setString(1, newCard.getCid());
            insertCardStmt.setString(2, newCard.getPid());
            insertCardStmt.setInt(3, newCard.getType().ordinal());
            insertCardStmt.setString(4, newCard.getTitle());
            insertCardStmt.setString(5, newCard.getContent());
            database.nonQuery(insertCardStmt);
        } catch (SQLException sqlEx){
            sqlEx.printStackTrace();
        }
    }

    /**
     * Initialize a new card with an initial transaction and backlog triage
     * @param cid card id hash
     * @param uid user id hash
     */
    private void initNewCard(String cid, String uid){
        //Create the statements for initialization
        String writeInitSql = "INSERT INTO Card_Transactions(cid, uid, timeStamp) VALUES (?, ?, NOW())";
        String writeInitTriageSql = "INSERT INTO Triages(tid, new_triage) VALUES (?, ?)";
        PreparedStatement writeInitStmt = database.prepareStatement(writeInitSql);
        PreparedStatement writeInitTriageStmt = database.prepareStatement(writeInitTriageSql);
        try {
            //Run the transaction init query and get the transaction id that was assigned
            writeInitStmt.setString(1, cid);
            writeInitStmt.setString(2, uid);
            int tid = database.nonQueryWithIdCallback(writeInitStmt);
            //Run the initial triage statement to complement the transaction
            writeInitTriageStmt.setInt(1, tid);
            //Put this in the backlog triage index
            writeInitTriageStmt.setInt(2, 0);
            database.nonQuery(writeInitTriageStmt);
        } catch (SQLException sqlEx) {

        }
    }

    /**
     * Get the details and history of a card by its id hash
     * @param cid card id hash
     * @return Card object - containing card data attributes and transactions
     */
    public Card getCard(String cid){
        //Get card data
        Card thisCard = getCardData(cid);
        //Get card transactional data
        thisCard.setTransactions(getCardTransactions(cid));
        return thisCard;
    }

    /**
     * Get the artifacts of a specific card collaboration
     * @param cid card id hash
     * @return list of json objects that make up the artifact
     */
    public List<JSONObject> getDrawingArtifacts(String cid){
        String getDrawingArtifactSql = "SELECT artifact FROM Drawing_Artifacts WHERE cid = ? ORDER BY timeStamp ASC LIMIT 200";
        PreparedStatement getDrawingArtifactStmt = database.prepareStatement(getDrawingArtifactSql);
        List<JSONObject> drawingArtifacts = new ArrayList<>();
        try{
            getDrawingArtifactStmt.setString(1, cid);
            ResultSet getDrawingArtifactResults = database.query(getDrawingArtifactStmt);
            while(getDrawingArtifactResults.next()){
                Blob artifact = getDrawingArtifactResults.getBlob("artifact");
                drawingArtifacts.add(new JSONObject(new String(artifact.getBytes(1, (int)artifact.length()))));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return drawingArtifacts;
    }

    /**
     * Get the data regarding a single card - what's its name, what's it about?
     * @param cid card id hash
     * @return Card data object
     */
    private Card getCardData(String cid){
        String getCardSql = "SELECT Cards.cardType AS cardType, Cards.title AS title, Cards.content AS content FROM Cards WHERE Cards.cid = ?";
        PreparedStatement getCardStmt = database.prepareStatement(getCardSql);
        Card thisCard;
        try {
            getCardStmt.setString(1, cid);
            ResultSet getCardResults = database.query(getCardStmt);
            if(getCardResults.next()){
                CardType cardType = CardType.values()[getCardResults.getInt("cardType")];
                String title = getCardResults.getString("title");
                String content = getCardResults.getString("content");
                thisCard = new Card(cid, title, content, cardType);
            } else {
                //Useless branch but the JVM will have a stroke if its not here
                thisCard = null;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            thisCard = null;
        }
        return thisCard;
    }

    /**
     * Get the card history and create list of transactions on card by cid
     * @param cid card id hash
     * @return list of card transactions
     */
    private List<CardTransaction> getCardTransactions(String cid){
        String getTransSql = "SELECT Card_Transactions.timeStamp AS timeStamp, Card_Transactions.uid AS uid, Users.FirstName AS firstname, Users.LastName AS lastname, Card_Transactions.tid AS tid FROM Card_Transactions INNER JOIN Users ON Users.Uid = Card_Transactions.uid WHERE Card_Transactions.cid = ? ORDER BY Card_Transactions.timeStamp DESC";
        PreparedStatement getTransStmt = database.prepareStatement(getTransSql);
        List<CardTransaction> cardTransactions = new ArrayList<>();
        try {
            getTransStmt.setString(1, cid);
            ResultSet getTransResults = database.query(getTransStmt);
            while(getTransResults.next()){
                String uid = getTransResults.getString("uid");
                String timeStamp = ((Date)getTransResults.getTimestamp("timeStamp")).toString();
                String name = getTransResults.getString("firstname") + " " + getTransResults.getString("lastname");
                int tid = getTransResults.getInt("tid");
                PreparedStatement getIsTriageStmt = database.prepareStatement("SELECT Triages.new_triage AS triage FROM Triages WHERE tid = ?");
                getIsTriageStmt.setInt(1, tid);
                ResultSet isTriageResults = database.query(getIsTriageStmt);
                if(isTriageResults.next()){
                    int triage = isTriageResults.getInt("triage");
                    //This is a triage transaction
                    cardTransactions.add(new CardTransaction(cid, uid, name, timeStamp, triage));
                } else {
                    //This is an assignment transaction
                    PreparedStatement getAsAssignmentStmt = database.prepareStatement("SELECT Card_Assignments.to_uid FROM Card_Assignments WHERE tid = ?");
                    getAsAssignmentStmt.setInt(1, tid);
                    ResultSet getAsAssignmentResult = database.query(getAsAssignmentStmt);
                    if(getAsAssignmentResult.next()) {
                        String newUserName = getTransResults.getString("firstname") + " " + getTransResults.getString("lastname");
                        String newUserId = getAsAssignmentResult.getString("to_uid");
                        cardTransactions.add(new CardTransaction(cid, uid, name, timeStamp, newUserName, newUserId));
                    }
                }
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return cardTransactions;
    }

    /**
     * Assign a card to a user
     * @param uid user to assign the card to - always self
     * @param cid card hash id
     */
    public void assignCard(String uid, String cid){
        String assignCardSql = "INSERT INTO Card_Transactions (cid, uid, timeStamp) VALUES (?, ?, NOW())";
        PreparedStatement assignCardStmt = database.prepareStatement(assignCardSql);
        try {
            assignCardStmt.setString(1, cid);
            assignCardStmt.setString(2, uid);
            int tid = database.nonQueryWithIdCallback(assignCardStmt);
            String addAssignSql = "INSERT INTO Card_Assignments(tid, to_uid) VALUES (?, ?)";
            PreparedStatement addAssignStmt = database.prepareStatement(addAssignSql);
            addAssignStmt.setInt(1, tid);
            addAssignStmt.setString(2, uid);
            database.nonQuery(addAssignStmt);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    /**
     *
     * @param uid
     * @param pid
     */
    public void giveProjectAccess (String uid, String pid){
        //give access to user
        String giveAccessSql = "INSERT INTO ProjectAccess(pid, uid) VALUES (?, ?)";
        PreparedStatement giveAccessStmt = database.prepareStatement(giveAccessSql);
        try {
            giveAccessStmt.setString(1, pid);
            giveAccessStmt.setString(2, uid);
            database.nonQuery(giveAccessStmt);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }
}
