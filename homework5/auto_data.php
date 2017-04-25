<?php
	$query = strtolower($_GET['q']);
	$response = file_get_contents("http://localhost:8983/solr/myexample/suggest?indent=on&wt=json&q=" . $query);
	$json = json_decode($response);
	$suggestions = $json->suggest->suggest->$query->suggestions;
	$result = array();
	foreach($suggestions as $suggestion){
		$result[] = $suggestion->term;
	}
	echo json_encode($result);
?>