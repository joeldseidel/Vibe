package main.managers;

import main.data.DatabaseInteraction;
import main.types.Project;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class UserDataManager {
    private DatabaseInteraction database;
    public UserDataManager(){
        database = new DatabaseInteraction();
    }

    /**
     * Get if username exists in the database
     * @param username username to check existence of
     * @return validity of username
     */
    public boolean isUsernameValid(String username){
        //Create get username validity query/prepare statement
        String getUsernameCountSql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        PreparedStatement getUsernameCountStmt = database.prepareStatement(getUsernameCountSql);
        boolean isValid;
        try{
            //Query for user name count
            getUsernameCountStmt.setString(1, username);
            ResultSet usernameCountResult = database.query(getUsernameCountStmt);
            //Username is valid if the query is successful and the count comes back as 1
            isValid = usernameCountResult.next() && usernameCountResult.getInt(1) == 1;
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
            //Not sure if it is valid or not but the database didn't work so ¯\_(ツ)_/¯
            isValid = false;
        }
        return isValid;
    }

    /**
     * Get if password input matches the password in the database
     * @param username username for the password
     * @param password password to attempt to match
     * @return does the password match?
     */
    public boolean doesPasswordMatch(String username, String password){
        //Get hashed username equivalent
        String hashedUsername = getHashedCredential(username);
        //Create get password hash by username statement
        String getUserPasswordSql = "SELECT Pass FROM User_Auth WHERE Username = ?";
        PreparedStatement getUserPasswordStmt = database.prepareStatement(getUserPasswordSql);
        boolean isValid;
        try {
            //Query for password hash by username
            getUserPasswordStmt.setString(1, hashedUsername);
            ResultSet passwordResult = database.query(getUserPasswordStmt);
            if(passwordResult.next()){
                //We got a password - elite - does it match the password input hash equivalent?
                String retrievedPasswordHash = passwordResult.getString("pass");
                String inputPasswordHash = getHashedCredential(password);
                isValid = inputPasswordHash.equals(retrievedPasswordHash);
            } else {
                //We didn't get a password
                isValid = false;
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            //Not sure if it is valid or not but the database didn't work so ¯\_(ツ)_/¯
            isValid = false;
        }
        return isValid;
    }

    /**
     * Get user id hash that corresponds to a username
     * @param username username for the uid we are looking for
     * @return uid hash string
     */
    public String getUidFromUsername(String username){
        //Create get uid by username statement
        String getUidFromUsernameSql = "SELECT Uid FROM Users WHERE Username = ?";
        PreparedStatement getUidStmt = database.prepareStatement(getUidFromUsernameSql);
        String uid = "";
        try {
            //Query for the uid
            getUidStmt.setString(1, username);
            ResultSet getUidResult = database.query(getUidStmt);
            //Get the uid or null if something went wrong (it shouldn't)
            uid = getUidResult.next() ? getUidResult.getString("Uid") : null;
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return uid;
    }

    public String[] getUserFullName(String uid){
        String getUserFullNameSql = "SELECT FirstName, LastName FROM Users WHERE uid = ?";
        PreparedStatement getUserFullNameStmt = database.prepareStatement(getUserFullNameSql);
        String[] userFullName = new String[2];
        try {
            getUserFullNameStmt.setString(1, uid);
            ResultSet getUserFullNameResults = database.query(getUserFullNameStmt);
            if(getUserFullNameResults.next()){
                userFullName[0] = getUserFullNameResults.getString("FirstName");
                userFullName[1] = getUserFullNameResults.getString("LastName");
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return userFullName;
    }

    public List<String> getUserAttributes(List<Object> get, String uid){
        //Vetted attribute array
        String[] attrArr = new String[get.size()];
        for(int i = 0; i < get.size(); i++){
            //Convert attribute object to string
            String attr = get.get(i).toString();
            //Throw away any attribute that isn't valid
            switch(attr){
                case "Username":
                case "Firstname":
                case "Lastname":
                    attrArr[i] = attr;
                    break;
            }
        }
        StringBuilder getUserAttrSql = new StringBuilder("SELECT ");
        for(int i = 0; i < attrArr.length; i++){
            getUserAttrSql.append(attrArr[i]);
            if(i < attrArr.length - 1){
                getUserAttrSql.append(", ");
            }
        }
        getUserAttrSql.append(" FROM Users WHERE Uid = ?");
        PreparedStatement getUserAttrStmt = database.prepareStatement(getUserAttrSql.toString());
        List<String> retrievedAttr = new ArrayList<>();
        try {
            getUserAttrStmt.setString(1, uid);
            ResultSet getUserAttrResult = database.query(getUserAttrStmt);
            if(getUserAttrResult.next()){
                for(String columnName : attrArr){
                    retrievedAttr.add(getUserAttrResult.getObject(columnName).toString());
                }
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return retrievedAttr;
    }

    /**
     * Get hashed string for a username or password
     * @param credential username or password string to hash
     * @return hashed text username or password
     */
    private String getHashedCredential(String credential) {
        try {
            //Hash the provided string in SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(credential.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            IntStream.range(0, hash.length).mapToObj(i -> Integer.toHexString(0xff & hash[i])).forEach(hex -> {
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            });
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String registerUser(String firstname, String lastname, String username, String password) {
        String uid = this.getHashedCredential(username);
        String hashedPass = this.getHashedCredential(password);
        String registerUserSql = "INSERT INTO Users (Uid, Username, FirstName, LastName) VALUES (?, ?, ?, ?)";
        String setUserAuthSql = "INSERT INTO User_Auth (Username, Pass) VALUES (?, ?)";
        PreparedStatement registerUserStmt = database.prepareStatement(registerUserSql);
        PreparedStatement setUserAuthStmt = database.prepareStatement(setUserAuthSql);
        try {
            registerUserStmt.setString(1, uid);
            registerUserStmt.setString(2, username);
            registerUserStmt.setString(3, firstname);
            registerUserStmt.setString(4, lastname);
            database.nonQuery(registerUserStmt);
            setUserAuthStmt.setString(1, uid);
            setUserAuthStmt.setString(2, hashedPass);
            database.nonQuery(setUserAuthStmt);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return uid;
    }

    public List<Project> getUserProjects(String uid){
        String getUserProjectSql = "SELECT ProjectAccess.pid AS pid, Projects.ProjName AS ProjName, Projects.ProjDesc AS ProjDesc FROM ProjectAccess INNER JOIN Projects ON Projects.pid = ProjectAccess.pid WHERE ProjectAccess.uid = ?";
        PreparedStatement getUserProjectStmt = database.prepareStatement(getUserProjectSql);
        List<Project> userProjects = new ArrayList<>();
        try {
            getUserProjectStmt.setString(1, uid);
            ResultSet getUserProjectResults = database.query(getUserProjectStmt);
            while(getUserProjectResults.next()){
                String pid = getUserProjectResults.getString("pid");
                String name = getUserProjectResults.getString("ProjName");
                String desc = getUserProjectResults.getString("ProjDesc");
                userProjects.add(new Project(name, desc, pid));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return userProjects;
    }
}
