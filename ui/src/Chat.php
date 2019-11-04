<?php
namespace App;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

class Chat implements MessageComponentInterface{
    protected $clients;
    private $connectedUsers;
    private $logs;
    private $connectedUsernames;
    private $connectedRooms;

    public function __construct(){
        $this->clients = new \SplObjectStorage;
        $this->logs = [];
        $this->connectedUsers = [];
        $this->connectedUsernames = [];
        $this->connectedRooms = [];
    }
    public function onOpen(ConnectionInterface $conn){
        $this->clients->attach($conn);
        $this->connectedUsers[$conn->resourceId] = $conn;
    }
    public function onMessage(ConnectionInterface $from, $msg){
        $msg = json_decode($msg);
        $msg->id = $from->resourceId;
        switch($msg->type){
            case "chat":
                $msg->username = $this->connectedUsernames[$from->resourceId];
                $this->commitToData($msg);
                break;
            case "new-client":
                $this->connectedUsernames[$from->resourceId] = $msg->username;
                $doesRoomExist = false;
                $roomAssignment = -1;
                for($i = 0; $i < count($this->connectedRooms); $i++){
                    if($this->connectedRooms[$i] == $msg->cid){
                        $doesRoomExist = true;
                        $roomAssignment = $i;
                        break;
                    }
                }
                if(!$doesRoomExist){
                    array_push($this->connectedRooms, $msg->cid);
                    $roomAssignment = count($this->connectedRooms) - 1;
                }
                $from->send(json_encode(array(
                    "type" => "roomAssignment",
                    "room" => $roomAssignment
                )));
                $msg->room = $roomAssignment;
                break;
        }
        $this->sendMessage($msg);
    }
    private function commitToData($msg){

    }
    private function sendMessage($message){
        $message = json_encode($message);
        foreach($this->connectedUsers as $user){
            $user->send($message);
        }
    }
    public function onClose(ConnectionInterface $conn){
        $goodbyeCmd = new \stdClass();
        $goodbyeCmd->type = "goodbye";
        $goodbyeCmd->user = $this->connectedUsernames[$conn->resourceId];
        $this->sendMessage($goodbyeCmd);
        $this->clients->detach($conn);
    }
    public function onError(ConnectionInterface $conn, \Exception $e){
        echo "Error occurred :[   ERROR MESSAGE: {$e->getMessage()}\n";
    }
}