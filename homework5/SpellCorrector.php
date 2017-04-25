<?php
/*
***************************************************************************
*   Copyright (C) 2008 by Felipe Ribeiro                                  *
*   felipernb@gmail.com                                                   *
*   http://www.feliperibeiro.com                                          *
*                                                                         *
*   Permission is hereby granted, free of charge, to any person obtaining *
*   a copy of this software and associated documentation files (the       *
*   "Software"), to deal in the Software without restriction, including   *
*   without limitation the rights to use, copy, modify, merge, publish,   *
*   distribute, sublicense, and/or sell copies of the Software, and to    *
*   permit persons to whom the Software is furnished to do so, subject to *
*   the following conditions:                                             *
*                                                                         *
*   The above copyright notice and this permission notice shall be        *
*   included in all copies or substantial portions of the Software.       *
*                                                                         *
*   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       *
*   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    *
*   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*
*   IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR     *
*   OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, *
*   ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR *
*   OTHER DEALINGS IN THE SOFTWARE.                                       *
***************************************************************************
*/


/**
 * This class implements the Spell correcting feature, useful for the
 * "Did you mean" functionality on the search engine. Using a dicionary of words
 * extracted from the product catalog.
 *
 * Based on the concepts of Peter Norvig: http://norvig.com/spell-correct.html
 *
 * @author Felipe Ribeiro <felipernb@gmail.com>
 * @date September 18th, 2008
 * @package catalog
 *
 */

ini_set('memory_limit', '1024M');
ini_set('max_execution_time', 300);

class SpellCorrector {
	private static $NWORDS;

	/**
	 * Reads a text and extracts the list of words
	 *
	 * @param string $text
	 * @return array The list of words
	 */
	private static function  words($text) {
		$matches = array();
		preg_match_all("/[a-z]+/",strtolower($text),$matches);
		return $matches[0];
	}

	/**
	 * Creates a table (dictionary) where the word is the key and the value is it's relevance
	 * in the text (the number of times it appear)
	 *
	 * @param array $features
	 * @return array
	 */
	private static function train(array $features) {
		$model = array();
		$count = count($features);
		for($i = 0; $i<$count; $i++) {
			$f = $features[$i];
			if(!isset($model[$f])) {
			$model[$f] = 0;
		 }
			$model[$f] += 1;
		}
		return $model;
	}



	/**
	 * Generates a list of possible "disturbances" on the passed string
	 *
	 * @param string $word
	 * @return array
	 */
	private static function edits1($word) {
		$sword = "a an and are at be for hs have has he if in is it no not of on or she so the there to us we was were will wont you youre";
		$stopwords=explode(" ",$sword);
		$alphabet = 'abcdefghijklmnopqrstuvwxyz';
		$alphabet = str_split($alphabet);
		$n = strlen($word);
		$edits = array();
		for($i = 0 ; $i<$n;$i++) {
/***************************
				$tmp = substr($word,0,$i).substr($word,$i+1); 		//deleting one char
				if(!in_array($tmp,$stopwords)){
				echo $tmp." ";
				$edits[] = $tmp;
				}
************************/
			foreach($alphabet as $c) {
				$tmp = substr($word,0,$i) . $c . substr($word,$i+1); //substituting one char
				if(!in_array($tmp,$stopwords)){
				$edits[] = $tmp;
				}
			}
		}
		for($i = 0; $i < $n-1; $i++) {
			$tmp = substr($word,0,$i).$word[$i+1].$word[$i].substr($word,$i+2); //swapping chars order
				if(!in_array($tmp,$stopwords)){
				$edits[] = $tmp;
				}
		}
		for($i=0; $i < $n+1; $i++) {
			foreach($alphabet as $c) {
			$tmp = substr($word,0,$i).$c.substr($word,$i); //inserting one char
				if(!in_array($tmp,$stopwords)){
				$edits[] = $tmp;
				}
			}
		}

		return $edits;
	}

	/**
	 * Generate possible "disturbances" in a second level that exist on the dictionary
	 *
	 * @param string $word
	 * @return array
	 */
	private static function known_edits2($word) {
		$known = array();
		foreach(self::edits1($word) as $e1) {
			foreach(self::edits1($e1) as $e2) {
				if(array_key_exists($e2,self::$NWORDS)) $known[] = $e2;
			}
		}
		return $known;
	}

	/**
	 * Given a list of words, returns the subset that is present on the dictionary
	 *
	 * @param array $words
	 * @return array
	 */
	private static function known(array $words) {
		$known = array();
		foreach($words as $w) {
			if(array_key_exists($w,self::$NWORDS)) {
				$known[] = $w;

			}
		}
		return $known;
	}


	/**
	 * Returns the word that is present on the dictionary that is the most similar (and the most relevant) to the
	 * word passed as parameter,
	 *
	 * @param string $word
	 * @return string
	 */
	public static function correct($word) {
		$word = trim($word);
		if(empty($word)) return;

		$word = strtolower($word);

		if(empty(self::$NWORDS)) {

			/* To optimize performance, the serialized dictionary can be saved on a file
			instead of parsing every single execution */
			if(!file_exists('serialized_dictionary.txt'))
			{
				/* */
				self::$NWORDS = array();
				$dir    = "/var/www/html/data/crawl_data/";
				$pages = scandir($dir);
				$count = count($pages);
				for($i = 2; $i < $count; $i++)
				{
					$myWords = self::train(self::words(file_get_contents($dir.$pages[$i])));
					//$fp = fopen("dictionaryTemp.txt","a");
					foreach($myWords as $field => $value)
					{
					//	fwrite($fp,$field."=>".$value."\n");
						if(!isset(self::$NWORDS[$field]))
							self::$NWORDS[$field] = $value;
						else
							self::$NWORDS[$field] += $value;
					}
					//fclose($fp);
				}

				/* *
				$fp = @fopen("dictionaryTemp.txt", "r");
				self::$NWORDS = array();
				if ($fp) {
					while($line = fgets($fp)){
						$stripNewLine = explode("\n",$line);
						$component = explode("=>",$stripNewLine[0]);
						if(!isset(self::$NWORDS["$component[0]"]))
							self::$NWORDS["$component[0]"] = intval($component[1]);
						else
							self::$NWORDS["$component[0]"] += intval($component[1]);
					}
				}
				fclose($fp);
				/* */
				//echo "done with Temp Dictionary";

				$fp = fopen("serialized_dictionary.txt","w");
				fwrite($fp,serialize(self::$NWORDS));
				fclose($fp);

				$fp = fopen("readable_dictionary.txt","w");
				foreach(self::$NWORDS as $field=>$value)
					fwrite($fp,$field."=>".$value."\n");
				fclose($fp);
			}
			else {
				self::$NWORDS = unserialize(file_get_contents("serialized_dictionary.txt"));
				/* *
				$fp = fopen("readable_dictionary.txt","w");
				foreach(self::$NWORDS as $field=>$value)
					fwrite($fp,$field."=>".$value."\n");
				fclose($fp);
				/* */
			}
		}
		$candidates = array();
		if(self::known(array($word))) {
			return $word;
		} elseif(($tmp_candidates = self::known(self::edits1($word)))) {
			foreach($tmp_candidates as $candidate) {
				$candidates[] = $candidate;
			}
		} elseif(($tmp_candidates = self::known_edits2($word))) {
			foreach($tmp_candidates as $candidate) {
				$candidates[] = $candidate;
			}
		} else {
			return $word;
		}
		$max = 0;
		foreach($candidates as $c) {
			$value = self::$NWORDS[$c];
			if( $value > $max) {
				$max = $value;
				$word = $c;
			}
		}
		return $word;
	}


}

?>
