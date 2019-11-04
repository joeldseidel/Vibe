package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import org.json.JSONObject;


public class RegisterUserHandler extends HandlerPrototype implements HttpHandler {

    public RegisterUserHandler() {
        requiredKeys = new String[]{"firstName", "lastName", "username", "password"};
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        //get first name, last name, user name, and password from request
        String firstName = requestParams.getString("firstName");
        String lastName = requestParams.getString("lastName");
        String userName = requestParams.getString("username");
        String password = requestParams.getString("password");
        UserDataManager userDataManager = new UserDataManager();
        String uid = userDataManager.registerUser(firstName, lastName, userName, password);
        JSONObject uidObj = new JSONObject();
        uidObj.put("uid", uid);
        returnActionSuccess(uidObj);
    }
}