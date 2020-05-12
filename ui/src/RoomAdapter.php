<?php


namespace App;

use Ratchet\ConnectionInterface;

/**
 * Class RoomAdapter
 *      Abstracts away the implementation of a room object and provides helper methods for the handlers to interact with rooms
 * @package App
 */
class RoomAdapter
{
    //Array of open rooms
    private $open_rooms;

    /**
     * RoomAdapter constructor
     */
    public function __construct() {
        $this->open_rooms = array();
    }

    /**
     * Getter for room at a specified resource index
     * @param $resource_index int resource index of the room
     * @return Room the room at the resource index
     */
    public function get_room_at($resource_index){
        return $this->open_rooms[$resource_index];
    }

    /**
     * Assign the requesting client to the room for their entity id or create a new one if one does not exist
     * @param $client ConnectionInterface the client connection who wants to be assigned a room
     * @param $entity_id string the identifier of the entity the room will be dedicated to
     * @return Room the room this client was assigned to
     */
    public function assign_room($client, $entity_id) {
        $room = $this->find_room($entity_id);
        //Did a room get found for this entity id?
        if(is_null($room)){
            //A room was not found, create a new one
            $room = $this->create_new_room($entity_id);
        }
        //A room was either found or created, add the client to the room
        $room->add_member($client);
        return $room;
    }

    /**
     * Create a new room for an entity that does not have a dedicated room yet
     * @param $entity_id string the identifier of the entity the room will be dedicated to
     * @return Room room object that was created
     */
    private function create_new_room($entity_id){
        //Create a new room object for the entity id
        $new_room = new \App\Room($entity_id);
        //Add to the list of currently open rooms
        array_push($this->open_rooms, $new_room);
        //Get the resource index
        $new_room->set_resource_index(sizeof($this->open_rooms) - 1);
        echo "Created a new room for entity: " . $entity_id . "\n";
        return $new_room;
    }

    /**
     * Find the room in the room array and get the index/object reference of the room, if it is found
     * @param $entity_id string entity id of the room we are looking for
     * @return mixed|null either a room object or null if a room object is not found for the provided entity id
     */
    private function find_room($entity_id){
        $room = null;
        for($i = 0; $i < sizeof($this->open_rooms); $i++){
            //Is there a room at this id number?
            if(is_null($this->open_rooms[$i])){
                //Nope
                continue;
            }
            //Is this the room?
            if($this->open_rooms[$i]->get_entity_id() == $entity_id){
                //This room exists and is at the current index
                return $this->open_rooms[$i];
            }
        }
        return null;
    }
}