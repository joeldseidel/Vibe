/**
 * API connection utility functions
 */

//DO NOT HARD CODE THIS IN ANYWHERE ELSE SO HELP ME GOD
const apiUrl = "https://18.222.8.82:6969";
const wsUrl = "ws://18.222.8.82:6968";
let wsNodeMap = {
    card : wsUrl + "/card",
    draw : wsUrl + "/draw",
    chat : wsUrl + "/chat"
};
const apiNodeMap = {
    auth : "/user/auth",
    token : "/token/get",
    userData : "/user/data/get",
    projArtifactReq : "/project/artifacts/get",
    cardDetail : "/card/get",
    registerUser: "/user/register",
    getDrawArtifacts: "/card/artifacts/get",
    getProjects: "/user/projects/get",
    giveAccess: "/user/giveaccess"
};
/**
 * Send a request to the API
 * @param request request data/parameters (i.e. what the API needs to work with)
 * @param node node of the API to make the call to (i.e. what you want the API to do)
 * @param onComplete function to call on completion of the request
 */
function sendRequest(request, node, onComplete){
    request = addClientToken(request);
    let url = apiUrl + node;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "json",
        data: JSON.stringify(request),
        success: function(apiReturn){
            onComplete(apiReturn);
        }
    });
}
/**
 * Send a request without any data with it
 * @param node node to call on the API for
 * @param onComplete function to call on completion of the request
 */
function sendNoParamRequest(node, onComplete){
    let url = apiUrl + node;
    $.ajax({
        type: "POST",
        url: url,
        success: function(apiReturn){
            onComplete(JSON.parse(apiReturn));
        }
    });
}
/**
 * Add token attribute to a request data object
 * @param request request data object
 */
function addClientToken(request){
    request.token = getToken();
    return request;
}
/**
 * Set token in session storage
 * @param token token hash to store
 */
function setToken(token){
    sessionStorage.setItem("token", token);
}
/**
 * Get user session token form session storage
 */
function getToken(){
    return sessionStorage.getItem("token");
}
/**
 * Get user id hash from session storage
 */
function getUid(){
    return sessionStorage.getItem("uid");
}
/**
 * Set user id hash to session storage
 * @param uid user id hash
 */
function setUid(uid){
    sessionStorage.setItem("uid", uid);
}

function setUsername(username){
    sessionStorage.setItem("username", username);
}

function getUsername(){
    return sessionStorage.getItem("username");
}

function setFullName(firstname, lastname){
    sessionStorage.setItem("firstname", firstname);
    sessionStorage.setItem("lastname", lastname);
}

function getFullName(){
    let firstName = sessionStorage.getItem("firstname");
    let lastName = sessionStorage.getItem("lastname");
    return firstName + " " + lastName;
}

function getInitials(){
    let firstName = sessionStorage.getItem("firstname");
    let lastName = sessionStorage.getItem("lastname");
    return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
}

/**
 * Generate a random color for this client to announce itself as
 * @returns {string} random color string
 */
function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}