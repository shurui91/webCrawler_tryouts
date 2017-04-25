<?php
$limit = 10;
$words = array("NATO", "Dow Jones", "Rio Olympics", "Pokemon Go",
	"California Wild Fires", "Donald Trump", "Harry Potter", "Brazil");

// The Apache Solr Client library should be on the include path
// which is usually most easily accomplished by placing in the
// same directory as this script ( . or current directory is a default
// php include path entry in the php.ini)
require_once('./solr-php-client/Apache/Solr/Service.php');

// create a new solr service instance - host, port, and webapp
// path (all defaults in this example)
$solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');


foreach($words as $word)
{

	// if magic quotes is enabled then stripslashes will be needed
	if (get_magic_quotes_gpc() == 1)
	{
		$word = stripslashes($word);
	}
	$firstres = array();
	$count = 0;
	// in production code you'll always want to use a try /catch for any
	// possible exceptions emitted  by searching (i.e. connection
	// problems or a query parsing error)
	try
	{
		$results = $solr->search($word, 0, $limit, array('q.op' => 'AND'));
		foreach ($results->response->docs as $doc){
			$firstres[] = $doc->id;
		}
		$results = $solr->search($word, 0, $limit, array('q.op' => 'AND', 'sort' => 'pageRankFile desc'));
		foreach ($results->response->docs as $doc){
			if(in_array($doc->id, $firstres)){
				$count++;
			}
		}
		echo $word. ", " . $count . "</br>";
	}
	catch (Exception $e)
	{
		// in production you'd probably log or email this error to an admin
		// and then show a special message to the user but for this example
		// we're going to show the full exception
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	}
}

?>
