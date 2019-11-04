package main.handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class RequestTokenHandler extends HandlerPrototype implements HttpHandler {
    @Override
    public void fulfillRequest(JSONObject requestParams){
        returnActionSuccess(new JSONObject().put("token", createToken()));
    }

    /**
     * Override the prototype validity check, a token request will always be valid as it contains nothing to validate
     * @param requestParams request parameter objects
     * @return true - always
     */
    @Override
    public boolean isRequestValid(JSONObject requestParams){
        return true;
    }

    /**
     * Generate token for client who has requested one
     * @return token string
     */
    private String createToken(){
        String token = "";
        try{
            Algorithm algorithm = Algorithm.HMAC256("secret");
            //Generate JWT token
            token = JWT.create().withIssuer("localhost:6969").sign(algorithm);
        } catch (UnsupportedEncodingException uEE){
            uEE.printStackTrace();
        }
        return token;
    }
}
