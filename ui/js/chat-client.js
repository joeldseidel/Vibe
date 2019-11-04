if(!"WebSocket" in window){
    alert("WebSockets is not supported in this browser");
}
var CID = getCIDArg();
let chatRoomAssignment = -1;
//Lord forgive us
let userName = sessionStorage.getItem("firstname") + " " + sessionStorage.getItem("lastname");
var chatElement = $('#chat-view');
var chatConn = new WebSocket(wsNodeMap.chat);
var chatShown = true;

chatConn.onopen = function(){
    let helloCmd = {
        type : "new-client",
        username : userName,
        cid : CID,
        room : chatRoomAssignment
    };
    chatConn.send(JSON.stringify(helloCmd));
};

chatConn.onmessage = function(e){
    let cmd = JSON.parse(e.data);
    if(cmd.room === chatRoomAssignment || cmd.type === "roomAssignment"){
        formatChatMessage(e);
    }
};
if(window.innerWidth <= 768){
    //Start with mini chat open and big chat closed
    //Show the mobile drawing menus
    $('#mobile-tool-box').css("display", "block");
    $('#mobile-context').css("display", "block");
    chatShown = false;
}
$(document).ready(function(){
    var sendChatButton = $('#send-chat-button');
    var chatInput = $('#chat-input');
    var minChat = $('#close-chat-button');
    var openChat = $('#open-chat');
    chatInput.keypress(function (e) {
        var key = e.which;
        if(key === 13){
            sendChatButton.click();
            return false;
        }
    });
    sendChatButton.click(function(){
        var msg = chatInput.val();
        if(msg !== ''){
            let chatCmd = {
                type: "chat",
                msg: msg,
                room: chatRoomAssignment
            };
            chatConn.send(JSON.stringify(chatCmd));
            chatInput.val('');
        }
    });
    minChat.click(function(){
        var chat = $('#chat');
        var minichat = $('#mini-chat');
        chat.slideToggle();
        minichat.css("display", "block");
        if(window.innerWidth <= 768){
            //Show the mobile drawing menus
            $('#mobile-tool-box').css("display", "block");
            $('#mobile-context').css("display", "block");
        }
        chatShown = false;
    });
    openChat.click(function(){
        var chat = $('#chat');
        var minichat = $('#mini-chat');
        minichat.css("display", "none");
        $('#mobile-tool-box').css("display", "none");
        $('#mobile-context').css("display", "none");
        chat.slideToggle();
        unreadMessageCount = 0;
        updateUnread();
        chatShown = true;
    });
});

function getCIDArg(){
    var cid = window.location.search.replace("?", '');
    cid = cid.replace("cid=", '');
    return cid;
}

//Initially -1 because the first "message" from the server is the chat log and the second is the I joined the chat
//Therefore after receiving the logs, there will be 0 messages in the chat
var messageCount = 0;
var unreadMessageCount = 0;

function formatChatMessage(e){
    var message = JSON.parse(e.data);
    var messageElement = $('<div class="chat-message"></div>');
    if(message.type === "new-client"){
        let user = message.username;
        messageElement = createEventRecord(messageElement, user, "joined the workspace");
    } else if(message.type === "goodbye"){
        let user = message.username;
        messageElement = createEventRecord(messageElement, user, "left the workspace");
    } else if(message.type === "roomAssignment"){
        chatRoomAssignment = message.room;
        return;
    } else if(message.type ==="chat"){
        //Someone said something
        messageElement.addClass("msg");
        var usernameElement = $('<p class="chat-username"></p>');
        usernameElement.text(message.username);
        usernameElement.appendTo(messageElement);
        var textElement = $('<p class="chat-body"></p>');
        textElement.text(message.msg);
        textElement.appendTo(messageElement);
    }
    if(!chatShown && messageCount > 0){
        unreadMessageCount++;
        updateUnread();
    }
    messageCount++;
    messageElement.prependTo(chatElement);
}

function createEventRecord(messageElement, user, text){
    //Something happened. Someone joined
    messageElement.addClass("evt");
    var eventReportElement = $('<p class="chat-event"></p>');
    eventReportElement.text(user + " " + text);
    eventReportElement.appendTo(messageElement);
    return messageElement;
}

function updateUnread(){
    var notification = $('#notifications');
    if(unreadMessageCount === 0){
        notification.text("");
    } else {
        notification.text(unreadMessageCount + " unread");
    }
}