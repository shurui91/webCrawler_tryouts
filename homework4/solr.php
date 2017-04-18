<?php
// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query) {
	// the solr-php-client folder is in /var/www, this has to set correctly
	// otherwise the code will not work
	require_once('../Apache/Solr/Service.php');
	
	// new solr service instance
	$solr = new Apache_Solr_Service('localhost', 8983, '/solr/hw4/');
	
	// if magic quotes is enabled then stripslashes will be needed
	if (get_magic_quotes_gpc() == 1) {
		$query = stripslashes($query);
	}
	$param = [];
	if (array_key_exists("pagerank", $_REQUEST)) {
		$param['sort'] ="pageRankFile desc";
	}
	
	try {
		// $results = $solr->search($query, 0, $limit, $param);
		$results = $solr->search($query, 0, $limit);
	}
	catch (Exception $e) {
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	}
}
?>

<!doctype html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>PHP Solr Client</title>
</head>
<body>
	<form accept-charset="utf-8" method="get">
		<label for="q">Search:</label>
		<input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
		<input type="submit"/>
		<br>
		<input type="checkbox" name="pagerank">Use Page Rank Algorithm<br>
		<br>
	</form>
	<?php if ($results): ?>
		<?php 
			$total = (int)$results->response->numFound;
			$start = min(1, $total);
			$end = min($limit, $total);
		?>
		<div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
		<hr>
		<?php foreach ($results->response->docs as $doc): ?>
			<?php 
				$id = $doc->id;
				$url = substr($id, 21);
				$url = urldecode($url);
			?>
			<a href="<?php echo $url; ?>">Document</a>
			<?php echo $doc->title ? $doc->title : "None"; ?>
			<p>
				Author: <?php echo $doc->author ? $doc->author : "None"; ?> | Size: <?php echo $doc->stream_size ? $doc->stream_size : "None"; ?>
			</p>
			<hr>
		<?php endforeach; ?>
	<?php endif; ?>
</body>
</html>
