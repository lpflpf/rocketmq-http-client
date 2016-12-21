<?php
require_once("util.php");
/**
 * Consumer.php
 *
 * @author   <lipengfeijs@dangdang.com>
 * @created  2016/12/20 20:23
 */
class Consumer
{
    private $_signature = '';
    private $_httpServer = '';
    private $_consumerId = '';

    public function __construct($hostname, $topic, $tag, $consumerId)
    {

        $this->_httpServer = $hostname;
        $this->_signature = implode('-', [$topic, $tag, time()]);
        $this->_consumerId = $consumerId;
    }

    /**
     * @param callable $func
     */
    public function do(callable $func){
        $seriesId = time() . rand(1, 10000);
        $result = Util::get($this->_httpServer, [
            "Content-Type:application/json",
            "sourceid:" . gethostbyaddr("127.0.0.1")  ,
            "signature:" . $this->_signature ,
            "consumerId:" . $this->_consumerId,
            "seriesid:" . $seriesId,
        ]);
        $messages = json_decode($result, true);
        foreach($messages as $message){
            $func($message);
        }
    }
}

$consumer = new Consumer("dropw", "ITEM_BASE", "podRelationship", "podRelationShipSyncJob");
$consumer->do(function($message){
    if (!empty($message)){
        echo "bornTime:" . $message['bornTime'] . "\n"
            . "msgHandle" . $message['msgHandle'] . "\n"
            . "msgId" . $message['msgId'] . "\n"
            . "reconsumeTimes" . $message['reconsumeTimes'] . "\n" ;
        echo json_encode($message['body']) . "\n\n";
    }
});