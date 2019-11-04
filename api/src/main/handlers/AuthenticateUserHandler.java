package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {
    public AuthenticateUserHandler(){
        requiredKeys = new String[] { "username", "password", "token" };
    }
    @Override
    public void fulfillRequest(JSONObject requestParams){
        //Get username and password parameters from request
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        UserDataManager userDataManager = new UserDataManager();
        //Determine username validity
        if(userDataManager.isUsernameValid(username)){
            //It worked - try the password now
            if(userDataManager.doesPasswordMatch(username, password)){
                //Everything is valid, the client will expect the user id hash of the user that was authenticated
                JSONObject uidObj = new JSONObject();
                String uid = userDataManager.getUidFromUsername(username);
                String[] fullName = userDataManager.getUserFullName(uid);
                uidObj.put("uid", uid);
                JSONArray nameArray = new JSONArray();
                for(String name : fullName){
                    nameArray.put(name);
                }
                uidObj.put("name", nameArray);
                //Good job, successful authentication
                returnActionSuccess(uidObj);
            } else {
                //Password is wrong. Client will want to know why. "It's the password you idiot" - the API
                returnActionFailure("password");
            }
        } else {
            //Username is invalid. Client will want to know why. "It's the username you idiot" - the API
            returnActionFailure("username");
        }
    }
}
