# File List for Homework 4
## Files need to be placed in two different places
* Your Solr environment, ```Solr-6.x.x``` and its subfolders
* Your Apache folder, ```/var/www/html```

## Details
### In ```/var/www/html```, there are two files/folders
* ```index.php```, the UI interface to allow user input, I named it as ```solr.php```
* ```solr-php-client```, this is a PHP library that supports Solr, https://github.com/PTCInc/solr-php-client

### In the root of Solr (Solr-6.x.x), I have a folder
* ```crawl_data```, which is all the HTML data I am responsible for. 

### In ```Solr-6.x.x/server/solr```, I have a folder called ```hw4```, which is the core I created for this project.
### Inside of ```hw4``` folder, there are two folders need to edit, ```conf``` and ```data```.
* ```conf``` has all the configuration files we need for Solr. Two files need to edit.
    * ```managed-schema```, need to edit this file based on the requirement.
    * ```solrconfig.xml```, need to edit this file based on the requirement.
* ```data``` is what I created, it has the ```external_pageRankFile.txt``` that we need for the Page Rank Algorithm.
    * ```external_pageRankFile.txt``` comes from a Python code I wrote, ```pagerank.py```
        * ```pagerank.py``` needs to read ```edgelist.txt``` and generate ```external_pageRankFile.txt```
        * ```edgelist.txt``` comes from a Java code I wrote, ```ExtractLinks.java```. It uses Jsoup library.