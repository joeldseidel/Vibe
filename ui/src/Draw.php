<?php
namespace App;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

class Draw implements MessageComponentInterface {
    protected $clients;
    private $connectedUsers;
    private $connectedRooms;
    public function __construct()
    {
        $this->clients = new \SplObjectStorage();
        $this->connectedUsers = [];
        $this->connectedRooms = array();
    }
    public function onOpen(ConnectionInterface $conn)
    {
        $this->clients->attach($conn);
        $this->connectedUsers[$conn->resourceId] = $conn;
        echo "{$conn->resourceId} connected\n";
        //Send to the connected client who is already here
        $connectedIds = array();
        foreach($this->connectedUsers as $user){
            array_push($connectedIds, $user->resourceId);
        }
        //Create the client welcome hand shake
        $welcomeMsg = new \stdClass();
        $welcomeMsg->type = "welcome";
        $welcomeMsg->friendsHere = $connectedIds;
        $welcomeMsg->me = $conn->resourceId;
        $conn->send(json_encode($welcomeMsg));
    }
    public function onMessage(ConnectionInterface $from, $msg)
    {
        $msg = json_decode($msg);
        //Add the client id to the message
        $msg->id = $from->resourceId;
        if($msg->type == "close-path"){
            $this->commitToData($msg->lineData, $msg->token, $msg->room);
        } else if($msg->type == "new-text"){
            $text_data = new \stdClass();
            $text_data->path = $msg->props;
            $this->commitToData($text_data, $msg->token, $msg->room);
        } else if($msg->type == "new-image"){
            $img_data = new \stdClass();
            $img_data->path = $msg->props;
            $this->commitToData($img_data, $msg->token, $msg->room);
        } else if($msg->type == "new-client"){
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
                "room" => $roomAssignment
            )));
            $msg->room = $roomAssignment;
        }
        //Send message to all connected clients
        $this->sendMessage($msg);
    }
    public function sendMessage($msg){
        $msg = json_encode($msg);
        foreach($this->connectedUsers as $user){
            $user->send($msg);
        }
    }
    public function onClose(ConnectionInterface $conn)
    {
        $this->clients->detach($conn);
        echo "{$conn->resourceId} disconnected\n";
    }
    public function onError(ConnectionInterface $conn, \Exception $e)
    {
        echo "Error occurred :[   ERROR MESSAGE: {$e->getMessage()}\n";
    }
    private function commitToData($data, $token, $room){
        $data->token = $token;
        //Get cid from room transformation
        $data->cid = $this->connectedRooms[$room];
        $data = json_encode($data);
        try{
            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, "https://localhost/card/artifact/add");
            curl_setopt($ch, CURLOPT_PORT, 6969);
            curl_setopt($ch, CURLOPT_POST, 1);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, 100);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,false);
            curl_exec($ch);
            curl_close($ch);
        } catch(Exception $e){
            echo $e->getMessage();
        }
    }
    private function getArtifacts(){
        try{
            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, "http://localhost/get-artifacts");
            curl_setopt($ch, CURLOPT_PORT, 6869);
            curl_setopt($ch, CURLOPT_POST, 1);
            //Give is a dummy payload to fool the server into not dropping the packet
            $payload = new \stdClass();
            $payload->content = "joel is neat";
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, 100);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,false);
            $artifacts = curl_exec($ch);
            curl_close($ch);
            return json_decode($artifacts);
        } catch(Exception $e){
            echo $e->getMessage();
        }
    }
}