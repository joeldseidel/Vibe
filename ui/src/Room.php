<?php

namespace App;
use Ratchet\ConnectionInterface;
/**
 * Class Room
 *      Set of clients all expecting the same commands
 *      Object for emit discrimination
 * @package app
 */
class Room
{
    //The entity id we are collaborating on ~ this is the metaphorical room number
    private $entityId;
    //The client connections who are subscribing to this room
    private $connected_clients;
    //The index this room is assigned - essentially the room number
    private $resource_index;

    /**
     * Room constructor.
     * @param $entityId string id of the entity this room is dedicated to
     */
    public function __construct($entityId)
    {
        $this->entityId = $entityId;
        $this->connected_clients = array();
    }

    /**
     * Add a member to this room
     * @param ConnectionInterface $client_conn connection object for the client to be added
     */
    public function add_member(ConnectionInterface $client_conn)
    {
        //Add the member to the data mailing list
        $this->connected_clients[$client_conn->resourceId] = $client_conn;
    }

    /**
     * Remove a member from this room
     * @param ConnectionInterface $client_conn
     */
    public function remove_member(ConnectionInterface $client_conn){
        $this->connected_clients[$client_conn->resourceId] = null;
    }

    /**
     * Getter for the connected clients within this room
     * @return array array of client connections representing the members of this room
     */
    public function get_members(){
        return $this->connected_clients;
    }

    /**
     * Getter for the entity id for this room
     * @return string entity id for this room
     */
    public function get_entity_id(){
        return $this->entityId;
    }

    /**
     * Set the assigned resource index ~ corresponding roughly to the position in the room array at the main server
     * @param $resource_index int resource index to set
     */
    public function set_resource_index($resource_index)
    {
        $this->resource_index = $resource_index;
    }

    /**
     * Getter for the resource index property
     * @return int resource index
     */
    public function get_resource_index(){
        return $this->resource_index;
    }
}