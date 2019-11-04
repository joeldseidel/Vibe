<?php
require '../vendor/autoload.php';
require '../src/Card.php';
require '../src/Draw.php';
require '../src/Chat.php';
use App\Card;
use App\Draw;
use App\Chat;

$app = new Ratchet\App("18.222.8.82", 6968, "172.31.4.201");
$app->route('/card', new Card, array('*'));
$app->route('/draw', new Draw, array('*'));
$app->route('/chat', new Chat, array('*'));
$app->run();