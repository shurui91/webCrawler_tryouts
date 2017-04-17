__author__ = 'Shurui Liu'
import networkx as nx

def generatePageRank():
	hashmap = readFile();
	G = nx.DiGraph();
	for key in hashmap:
		G.add_node(key);
		for url in hashmap[key]:
			G.add_edge(key, url);

	pr = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None);
	output = open("external_PageRank.txt", "w+");
	for key in pr:
		output.write("" + key + "=" + ("%.6f" % pr[key]) + "\n");
	output.close();

def readFile():
	hashmap = {};
	filename = "edgelist.txt";
	with open(filename, "r") as lines:
		for line in lines:
			urls = line.split(" ");

			if urls[0] not in hashmap:
				hashmap[urls[0]] = [];

			for url in urls[1:]:
				hashmap[urls[0]].append(url);
	return hashmap;

if __name__ == "__main__":
	generatePageRank();