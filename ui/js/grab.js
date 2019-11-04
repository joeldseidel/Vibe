function requestToken(onComplete){
    sendNoParamRequest(apiNodeMap.token, onComplete);
}

function getNameFromUid(uid, onComplete){
    let getUserReq = {
        get : [
            "Firstname",
            "Lastname"
        ],
        with : "uid"
    };
    sendRequest(getUserReq, apiNodeMap.userData, onComplete);
}

function getProjectArtifacts(pid, onComplete){
    let artifactRequest = {
        pid : pid,
        uid : getUid()
    };
    sendRequest(artifactRequest, apiNodeMap.projArtifactReq, onComplete);
}

function getCardDetail(cid, onComplete){
    let getCardDetail = {
        cid : cid
    };
    sendRequest(getCardDetail, apiNodeMap.cardDetail, onComplete);
}

function getDrawingArtifacts(cid, onComplete){
    let getArtifactCmd = {
        cid : cid
    };
    sendRequest(getArtifactCmd, apiNodeMap.getDrawArtifacts, onComplete);
}

function getProjects(onComplete){
    let getProjectsCmd = {
        uid : getUid()
    };
    sendRequest(getProjectsCmd, apiNodeMap.getProjects, onComplete);
}