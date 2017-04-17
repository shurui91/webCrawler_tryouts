import networkx as nx

line = "test1 test2 test3 test4";
test = line.split();
print test;

G = nx.DiGraph();
pr = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None)