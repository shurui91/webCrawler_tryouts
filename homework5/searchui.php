<?php

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$defaultchecked = !isset($_REQUEST['sort']) || $_REQUEST['sort'] == 'default';
$results = false;
$id2url = array();

function generateSnippet($content, $query){
	$index = stripos($content, $query);
	if($index == false){
		return false;
	}
	$span = 15;
	$start = $index;
	$count = 0;
	$lastpunc = false;
	while($count <= $span && $start > 0){
		if(preg_match("/[\p{Pe} ]/", $content[$start])){
			if(!$lastpunc){
				$count++;
			}
			$lastpunc = true;
		}
		else {
			$lastpunc = false;
		}
		$start--;
	}
	$end = $index;
	$count = 0;
	$lastpunc = false;
	while($count <= $span && $end < strlen($content)){
		if(preg_match("/[\p{Pe} ]/", $content[$end])){
			if(!$lastpunc){
				$count++;
			}
			$lastpunc = true;
		}
		else {
			$lastpunc = false;
		}
		$end++;
	}
	return substr($content, $start + 1, $end - $start - 1);
}

if ($query)
{
	// The Apache Solr Client library should be on the include path
	// which is usually most easily accomplished by placing in the
	// same directory as this script ( . or current directory is a default
	// php include path entry in the php.ini)
	require_once('../solr-php-client/Apache/Solr/Service.php');
	require_once('SpellCorrector.php');
	$words = explode(" ", $query);
	$correctionResults = array();
	$spellerror = false;
	foreach($words as $word){
		$correctedword = SpellCorrector::correct($word);
		if($correctedword != strtolower($word)){
			$spellerror = true;
		}
		$correctionResults[] = $correctedword;
	}
	$correctedQuery = join(" ", $correctionResults);
	// create a new solr service instance - host, port, and webapp
	// path (all defaults in this example)
	$solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');

	// if magic quotes is enabled then stripslashes will be needed
	if (get_magic_quotes_gpc() == 1)
	{
		$query = stripslashes($query);
	}

	// in production code you'll always want to use a try /catch for any
	// possible exceptions emitted  by searching (i.e. connection
	// problems or a query parsing error)
	try
	{
		$results = $solr->search($query, 0, $limit, $defaultchecked ? array('q.op' => 'AND'):
			array('q.op' => 'AND', 'sort' => 'pageRankFile desc'));
		$lines = file("mapABCNewsFile.csv");
		foreach($lines as $line){
			$words = explode(",", $line);
			$id2url[$words[0]] = $words[1];
		}
		$lines = file("mapFoxNewsFile.csv");
		foreach($lines as $line){
			$words = explode(",", $line);
			$id2url[$words[0]] = $words[1];
		}
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
<html>
	<head>
		<title>PHP Solr Client Example</title>
		<link href="style.css" rel="stylesheet"></link>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
		<script src="auto_complete.js"></script>
	</head>
	<body>
		<form id="search" accept-charset="utf-8" method="get">
			<label for="q">Search:</label>
			<div class="dropdown">
				<input id="q" name="q" type="text" autocomplete="off" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
				<div class="suggestion-list" id="search-suggest"></div>
			</div>
			<input type="radio" name="sort" value="default" <?php if($defaultchecked) echo 'checked'; ?>>Default
			<input type="radio" name="sort" value="pageRank" <?php if(!$defaultchecked) echo 'checked'; ?>>PageRank
			<input type="submit" id="submit"/>
		</form>

<?php
if ($spellerror) {
	echo "<div> Showing results for $query</div>";
	echo "<div> Search instead for <a href = '/searchui.php?q=$correctedQuery'>$correctedQuery</a></div>";
}
// display results
if ($results)
{
	$total = (int) $results->response->numFound;
	$start = min(1, $total);
	$end = min($limit, $total);
	$num = 1;
?>
		<div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
		<hr>

<?php
// iterate result documents
	foreach ($results->response->docs as $doc)
	{
		// $id = substr($doc->id, 69);
		// $url = htmlspecialchars($id2url[$id], ENT_NOQUOTES, 'utf-8');
		$id = $doc->id;
		$url = $doc->og_url;
		$url = urldecode($url);
		$title = $doc->title;
		if(is_array($title)){
			$title = $title[0];
		}

		// generate snippet
		$words = explode(" ", $query);
		$content = file_get_contents($doc->id);
		$content = preg_replace("#<(script|style)(.*?)>(.*?)</(script|style)>#is", "", $content);
		$content = strip_tags($content);

		$snippet = generateSnippet($content, $query);
		$i = 0;
		while ($snippet == false && $i < count($words)) {
			$snippet = generateSnippet($content, $words[$i]);
			$i++;
		}

		if ($snippet == false) {
			continue;
		}

		foreach ($words as $word){
			$snippet = preg_replace("/(\b$word\b)/i", "<b>$1</b>", $snippet);
		}

		echo "<div class='searchresults'><a class='resultlink' href='$url'>$title</a><div class='snippet'>$snippet</div></div>";
		$num++;
	}
?>
	</div>
<?php
}
?>
	</body>
</html>