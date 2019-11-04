/**
 * Build and handle grab requests to the API
 */

/**
 * Create login request
 * @param username
 * @param password
 */
function loginUser(username, password, onComplete) {
    let loginReq = {
        username: username,
        password: password
    };
    sendRequest(loginReq, apiNodeMap.auth, onComplete);
}
function registerUser(first, last, un, pass, onComplete){
    let registerReq = {
        firstName: first,
        lastName: last,
        username: un,
        password: pass
    };
    sendRequest(registerReq, apiNodeMap.registerUser, onComplete);
}
function giveAccess(uid, pid, onComplete){
    let giveAccessReq = {
        uid : uid,
        pid : pid
    };
    sendRequest(giveAccessReq, apiNodeMap.giveAccess, onComplete);
}