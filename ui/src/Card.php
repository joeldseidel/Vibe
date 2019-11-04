<?php
namespace App;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

class Card implements MessageComponentInterface{
    protected $clients;
    private $connectedUsers;
    private $connectedClients;
    public function __construct()
    {
        $this->clients = new \SplObjectStorage();
        $this->connectedUsers = [];
        $this->connectedClients = [];
    }
    public function onOpen(ConnectionInterface $conn){
        $this->clients->attach($conn);
        $this->connectedUsers[$conn->resourceId] = $conn;
        echo "{$conn->resourceId} connected\n";
        $conn->send(json_encode(array(
            "type"=> "hi",
            "friendsHere"=> $this->connectedClients
        )));
    }
    public function onMessage(ConnectionInterface $from, $msg)
    {
        $msg = json_decode($msg);
        //Add the client id to the message
        $msg->id = $from->resourceId;
        switch($msg->type){
            case "new-client":
                $this->addClient($msg);
                break;
            case "update-triage":
                $this->commitTransaction($msg, "update");
                break;
            case "assign":
                $this->commitTransaction($msg, "assign");
                $msg->username = $this->connectedClients[$from->resourceId]->username;
                break;
            case "new-card":
                $cardHash = $this->generateCardHashString();
                echo "New Card Hash: " . $cardHash . "\n";
                $msg->cid = $cardHash;
                $this->commitTransaction($msg,"create");
                break;
        }
        //Send message to all connected clients
        $this->sendMessage($msg);
    }
    public function sendMessage($msg){
        $msg = json_encode($msg);
        //Send message to all connected clients
        foreach($this->connectedUsers as $user){
            $user->send($msg);
        }
    }
    public function onClose(ConnectionInterface $conn)
    {
        $this->clients->detach($conn);
        $this->sendClientLeft($conn->resourceId);
        $this->connectedClients[$conn->resourceId] = null;
    }
    public function onError(ConnectionInterface $conn, \Exception $e)
    {
        echo "ERROR OCCURRED. Thank you to {$conn->resourceId} for causing the error: {$e->getMessage()}";
    }
    public function addClient($cmd){
        $thisClient = new \stdClass();
        $thisClient->id = $cmd->id;
        $thisClient->uid = $cmd->uid;
        $thisClient->username = $cmd->username;
        $thisClient->color = $cmd->color;
        $thisClient->initials = $cmd->initials;
        $this->connectedClients[$cmd->id] = $thisClient;
    }

    /**
     * A client has just decided to head out - everyone still here needs to know they left
     */
    public function sendClientLeft($id){
        $leftMsg = array(
            "type" => "goodbye",
            "id" => $id
        );
        $this->sendMessage($leftMsg);
    }

    public function commitTransaction($cmd, $node){
        $cmd = json_encode($cmd);
        var_dump($cmd);
        try {
            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, "https://localhost/card/" . $node);
            curl_setopt($ch, CURLOPT_PORT, 6969);
            curl_setopt($ch, CURLOPT_POST, 1);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $cmd);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, 100);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,false);
            curl_exec($ch);
            curl_close($ch);
        } catch(Exception $e) {
            echo $e->getMessage();
        }
    }

    public function generateCardHashString($length = 64) {
        $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        $charactersLength = strlen($characters);
        $randomString = '';
        for ($i = 0; $i < $length; $i++) {
            $randomString .= $characters[rand(0, $charactersLength - 1)];
        }
        return $randomString;
    }
}