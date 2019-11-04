if(!"WebSocket" in window){
    alert("WebSockets is not supported in this browser");
}
let cardConn;
let users = [];
let cards = [];
let pid;
let me = {};
$(document).ready(function(){
    triageContainers.forEach(function(item, i){
        triageContainers[i].data("triage", i);
        triageContainers[i].droppable({
            accept: ".card-prototype",
            drop: function( event, ui ) {
                let droppable = $(this);
                let draggable = ui.draggable;
                draggable.appendTo(droppable);
                draggable.css({top : '0px', left : '0px'});
                draggable.data("triage", droppable.data("triage"));
                sendUpdateTriage(draggable.data("cid"), droppable.data("triage"));
            }
        });

    });
    //Get project id passed in the url
    pid = getPidArg();
    //getProjectInfo(pid, initProjectInfo);
    //Get everything that has already happened on this project before I showed up
    getProjectArtifacts(pid, initProjectArtifacts);
});
/**
 * Retrieve project id hash from the url
 * @returns {string} project id hash from url
 */
function getPidArg(){
    let pidParam = window.location.search.replace("?", '');
    pidParam = pidParam.replace("proj=", '');
    return pidParam;
}
let initProjectInfo = function(apiResponse){

};
/**
 * Take an API response to an artifact request and make the cards
 * @param apiResponse response from the API
 */
let initProjectArtifacts = function(apiResponse){
    if(apiResponse.success){
        let artifacts = apiResponse.artifacts;
        if(artifacts.length === 0){
            //There are no artifacts, which means this is an empty project
            initEmptyProject();
        } else {
            //There are artifacts, which means this is a real project with cards
            let cardPrototype = $('<div class="card-prototype"></div>');
            cardPrototype.load("../frags/card.html", function() {
                for(let i = 0; i < artifacts.length; i++){
                    let artifact = artifacts[i];
                    addCard(artifact, cardPrototype.clone());
                }
            });
        }
        //The artifacts have been loaded, can now set up the web sockets connection
        initWS();
    } else {
        alert("Could not load artifacts from API");
    }
};
/**
 * Initialize the WebSocket connection now that the project is fully loaded
 */
let initWS = function(){
    cardConn = new WebSocket(wsNodeMap.card);
    cardConn.onopen = cardOnOpen;
    cardConn.onmessage = cardOnMessage;
    cardConn.onclose = cardOnClose;
};
/**
 * There are no artifacts for this project - so its either new or just empty
 */
let initEmptyProject = function(){
    //TODO: put the backlog on top? maybe? idk something to make the user know they can create cards
};
//Triage container array shortcut (enum alias) ~ parallel with triage level names
let triageContainers = [$('#backlog-container'), $('#to-do-col'), $('#in-progress-col'), $('#done-col')];
let triageLevelNames = ["Backlog", "To-Do", "In Progress", "Done"];
/**
 * Add a card for an artifact record
 * @param card artifact to add
 * @param cardPrototype the element to make the card in
 */
function addCard(card, cardPrototype){
    cardPrototype.find('.card-title').text(card.title);
    cardPrototype.find('.card-text').text(card.text);
    let viewCardButton = cardPrototype.find('.view-card-button');
    viewCardButton.click(cardClickHandler);
    viewCardButton.data("cid", card.cid);
    let collabCardButton = cardPrototype.find('.collaborate-card-button');
    collabCardButton.data("cid", card.cid);
    collabCardButton.click(collabClickHandler);
    cardPrototype.data("cid", card.cid);
    cardPrototype.data("triage", card.triage);
    cardPrototype.draggable({ cursor : 'grabbing', helper: 'clone', appendTo : '#body-container' });
    cards.push(cardPrototype);
    cardPrototype.appendTo(triageContainers[card.triage]);
}
/**
 * Card click handler
 * @param e event arguments to get card meta
 */
let cardClickHandler = function(e){
    let cid = $(e.target).data("cid");
    getCardDetail(cid, showCardDetail);
};
/**
 * Handle collab click button
 * @param e event arguments to get card meta
 */
let collabClickHandler = function(e){
    let cid = $(e.target).data("cid");
    window.open("../pages/collab_view.html?cid=" + cid, '_blank');
};
function showCardDetail(apiResponse){
    if(apiResponse.success){
        let viewCardModal = $('#view-card-modal');
        viewCardModal.data("cid", apiResponse.cid);
        viewCardModal.find('#view-card-title').text(apiResponse.title);
        viewCardModal.find('#view-card-content').text(apiResponse.content);
        let viewCardTransactions = $('#view-card-transactions-container');
        viewCardTransactions.empty();
        for(let i = 0; i < apiResponse.transactions.length; i++){
            addCardTransactionRecord(viewCardTransactions, apiResponse.transactions[i]);
        }
        viewCardModal.modal();
    } else {
        alert("Could not fetch this card");
    }
}
/**
 * Add a new transaction record to the card display modal
 * @param transactionRecordContainer transaction record container DOM object
 * @param transaction transaction record object
 */
function addCardTransactionRecord(transactionRecordContainer, transaction){
    let transactionRecord = $('<div class="transaction-record"></div>');
    let transactionAction = $('<p class="transaction-action"></p>');
    let time = new Date(Date.parse(transaction.timeStamp.toString()));
    time = time.toLocaleDateString() + " "+ time.toLocaleTimeString();
    let description = time + ": " + transaction.name;
    if(transaction.type === "TRIAGE"){
        //This was a triage transaction, create a triage transaction description
        description += " moved to " + triageLevelNames[transaction.triage];
    } else if(transaction.type === "ASSIGNMENT") {
        description += " assigned to " + transaction.toUserName;
    }
    transactionAction.text(description);
    transactionAction.appendTo(transactionRecord);
    transactionRecord.appendTo(transactionRecordContainer);
}

/*                       *
 * WEBSOCKETS CONNECTION *
 *                       */

let cardOnOpen = function(){
    me.color = getRandomColor();
    sendNewClient();
};
let cardOnMessage = function(e){
    handleCommand(e);
};
let cardOnClose = function(e){
    //TODO: handle a lost real time issue
};
/**
 * Introduce this client to all of the other clients
 */
function sendNewClient(){
    let newClientCmd = {
        type : "new-client",
        uid : getUid(),
        username : getUsername(),
        initials : getInitials(),
        color : me.color
    };
    cardConn.send(JSON.stringify(newClientCmd));
}
/**
 * Tell everyone else that I have re-triaged a card
 * @param cid card id that I moved
 * @param newTriage the triage I moved it
 */
function sendUpdateTriage(cid, newTriage){
    let updateTriageCmd = {
        type : "update-triage",
        uid : getUid(),
        cid : cid,
        newTriage : newTriage,
        token : getToken()
    };
    cardConn.send(JSON.stringify(updateTriageCmd));
}
/**
 * Let everyone know that I have made a new card!
 * @param newCard the card that I made
 */
function sendCreateNewCard(newCard){
    let newCardCmd = {
        type: "new-card",
        uid: getUid(),
        pid: pid,
        newCard: newCard,
        token: getToken()
    };
    cardConn.send(JSON.stringify(newCardCmd));
}
function sendAssign(cid){
    let sendAssignCmd = {
        type: "assign",
        uid: getUid(),
        cid: cid,
        token: getToken()
    };
    cardConn.send(JSON.stringify(sendAssignCmd));
}
/**
 * Route commands by type to handle methods
 * @param e command from WebSocket
 */
function handleCommand(e){
    let cmd = JSON.parse(e.data);
    let type = cmd.type;
    if(type === "hi") {
        //Hello command - register this user as part of the client
        handleCardHello(cmd);
    } else if(type === "new-client"){
        //A new client has announced its arrival
        handleNewClient(cmd);
    } else if(type === "goodbye"){
        //Goodbye command - deallocate this user resources
        handleCardGoodbye(cmd);
    } else if(type === "update-triage"){
        //A card has had its triage updated
        handleUpdateTriage(cmd);
    } else if(type === "new-card") {
        //A new card has been created
        handleNewCard(cmd);
    } else if(type === "assign"){
        handleAssign(cmd);
    }
}
/**
 * complete the handshake with the WebSocket server and display the users that are here
 * @param cmd handshake acknowledgement
 */
function handleCardHello(cmd){
    //TODO: indefinite waiting modal show until this point
    let friendsHere = Object.values(cmd.friendsHere);
    let friendListing = $('#user-icon-container');
    //Create a display icon for each connected user
    for(let i = 0; i < friendsHere.length; i++){
        let thisFriend = friendsHere[i];
        if(thisFriend === null){
            continue;
        }
        //Set this user data to the local connected client array
        users[thisFriend.id] = {
            uid : thisFriend.uid,
            username : thisFriend.username,
            initials : thisFriend.initials,
            color : thisFriend.color
        };
        //Create connected user icon
        let thisFriendIcon = getUserIcon(thisFriend);
        thisFriendIcon.appendTo(friendListing);
    }
}
/**
 * A new client has joined. It might even be me
 * @param cmd new client command
 */
function handleNewClient(cmd){
    users[cmd.id] = {
        uid : cmd.uid,
        username : cmd.username,
        initials: cmd.initials,
        color : cmd.color
    };
    let userIcon = getUserIcon(cmd);
    $('#user-icon-container').append(userIcon);
    userIcon.fadeIn("slow");
}
/**
 * Handle an update card triage event
 * @param cmd update triage event command
 */
function handleUpdateTriage(cmd){
    //Go through each card and see if this was the one that moved
    cards.forEach(function(card){
        let cid = card.data("cid");
        let triage = card.data("triage");
        let newTriage = cmd.newTriage;
        //Determine if this is the card that moved
        if(cid === cmd.cid){
            //did my client already update the triage? That is, am I the client that moved it?
            if(triage === newTriage){
                //I am the client that moved it
                return;
            }
            //I didn't move, so I must show that someone else did
            card.fadeOut("slow", function(){
                //The card is transparent - append to the new container
                card.appendTo(triageContainers[newTriage]);
                //Change the color background to show who moved it
                let color = users[cmd.id].color;
                card.css("background-color", color);
                //Fade the card back in
                card.fadeIn("slow", function(){
                    //The card is now fully opaque, start going back to default white
                    card.animate({backgroundColor : '#fff'}, 2500);
                });
                //Update the card's internal triage
                card.data("triage", newTriage);
            });
        }
    });
}
function handleAssign(cmd){
    cards.forEach(function(card){
        let cid = card.data("cid");
        if(cid === cmd.cid){
            card.find('.card-assign').text("Assigned to: " + cmd.username);
        }
    });
}
function handleNewCard(cmd){
    let cardPrototype = $('<div class="card-prototype"></div>');
    cardPrototype.load("../frags/card.html", function() {
        addCard(cmd.newCard, cardPrototype);
    });
}
/**
 * Free up a user that headed out
 * @param cmd user resource id
 */
function handleCardGoodbye(cmd){
    let leavingUser = users[cmd.id];
    let leavingUserId = cmd.id;
    let userIconContainer = $('#user-icon-container');
    let userIcons = $('.user-icon');
    for(let i = 0; i < userIcons.length; i++){
        let thisIconId = $(userIcons[i]).data("id");
        if(thisIconId === leavingUserId){
            $(userIcons[i]).fadeOut("slow", function(){
                userIcons[i].remove();
            });
        }
    }
}
/**
 * Create a user icon for connected usernames
 * @param thisFriend the friend to make an icon for
 */
function getUserIcon(thisFriend){
    let userIcon = $('<div class="user-icon align-items-center d-flex"></div>');
    userIcon.data("id", thisFriend.id);
    userIcon.css("background-color", thisFriend.color);
    userIcon.text(thisFriend.initials);
    userIcon.css("display", "none");
    return userIcon;
}