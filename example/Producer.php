<?php
require_once("util.php");
/**
 * Consumer.php
 *
 * @author   <lipengfeijs@dangdang.com>
 * @created  2016/12/21 12:36
 */
class Producer{
    private $_signature = '';
    private $_httpServer = '';
    private $_producerId = '';

    /**
     * Producer constructor.
     *
     * @param $hostname
     * @param $topic
     * @param $tag
     * @param $producerId
     */
    public function __construct($hostname, $topic, $tag, $producerId)
    {
        $this->_httpServer = $hostname;
        $this->_signature = implode('-', [$topic, $tag, time()]);
        $this->_producerId = $producerId;
    }

    /**
     * @param string $message
     * @return array
     */
    public function send($message){
        $result = Util::post($this->_httpServer, $this->buildHeader(), $message);
        return json_decode($result, true);
    }

    private function buildHeader(){
        $seriesId = time() . rand(1, 10000);
        return [
            "Content-Type:application/json",
            "sourceId:" . gethostbyaddr("127.0.0.1")  ,
            "signature:" . $this->_signature ,
            "producerId:" . $this->_producerId,
            "seriesId:" . $seriesId,
        ];
    }
}

$producer = new Producer("http://your.hostname/message", "topic", "tag", "producerId");
$producer->send(json_encode(["hello world"]));