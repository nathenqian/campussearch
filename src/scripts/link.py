import numpy as np
import matplotlib.pyplot as plt
from multiprocessing.dummy import Pool as ThreadPool
import json
import math

# parameters
ALPHA = 0.15
TN = 30

f = open('filelist.txt')
FN = f.readlines()
FN = [fn.strip() for fn in FN]
f.close()

fnode = open('node.map')
lnodes = fnode.readlines()
keys = []
nodes = {}
inlinks, outlinks = {}, {}
for l in lnodes:
	v, k = tuple(l.split('-->'))
	v = v.strip()
	k = k.strip()
	nodes[k] = v
	inlinks[k], outlinks[k] = [], []
	keys.append(k)
fnode.close()

print 'finish reading nodes'

flink = open('thu.graph')
llinks = flink.readlines()
for l in llinks:
	l = l.strip()
	h, n = l.split(':')
	for v in n.split(','):
		if len(v) == 0:
			continue
		outlinks[h].append(v)
		inlinks[v].append(h)
flink.close()

print 'finish reading links'

indeg, outdeg = {}, {}
inranks, outranks = [], []
incount, outcount = {}, {}
for k in keys:
	i = len(inlinks[k])
	o = len(outlinks[k])
	inranks.append(i)
	outranks.append(o)
	indeg[k] = i
	outdeg[k] = o
	if i in incount:
		incount[i] += 1
	else:
		incount[i] = 1
	if o in outcount:
		outcount[o] += 1
	else:
		outcount[o] = 1

print 'finish counting degrees'

def plot(d, name, color='red'):
	x, y = [], []
	for k, v in d.iteritems():
		if k == 0 or v == 0:
			continue
		x.append(k)
		y.append(v)
	c = zip(x, y)
	c = sorted(c, key=lambda x: x[0])
	x, y = zip(*c)
	x = np.log10(x)
	y = np.log10(y)
	plt.plot(x, y, label=name, color=color, linewidth=1)

plt.figure(figsize=(8,8))
plt.xlabel('degree, log')
plt.ylabel('# of pages, log')
plot(incount, 'inbound links')
plot(outcount, 'outbound links', 'green')
plt.title('Inbound / Outbound Degree Distribution')
plt.legend()
plt.savefig('inout_dis.png')
#plt.show()

print 'finish ploting degrees'

PR, I = {}, {}
S = 0
for k in keys:
	PR[k] = 1. / len(keys)
	I[k] = ALPHA / len(keys)
	if outdeg[k] == 0:
		S += PR[k]
for it in xrange(TN):
	for k in keys:
		for x in inlinks[k]:
			I[k] += (1-ALPHA) * PR[x] / outdeg[x]
	nS = 0
	for k in keys:
		PR[k] = I[k] + (1-ALPHA) * S / len(keys)
		I[k] = ALPHA / len(keys)
		if outdeg[k] == 0:
			nS += PR[k]
	S = nS
	print '\titer', it

prcount = {}
for k in keys:
	v = PR[k]
	v = math.log10(v)
	v = round(v*40)/40
	v = pow(10., v)
	if v not in prcount:
		prcount[v] = 0
	prcount[v] += 1

plt.figure(figsize=(8,8))
plt.xlabel('pagerank, log')
plt.ylabel('# of pages, log')
plot(prcount, 'pagerank')
plt.title('PageRank Distribution')
plt.legend()
plt.savefig('pr_dis.png')

print 'finish ploting degrees'

S = []
for k in keys:
	S.append((PR[k], indeg[k], nodes[k]))
S = sorted(S, key=lambda x: x[0])
f = open('results.txt', 'w')
for t in S:
	print >> f, t[0], t[1], t[2]
f.close()
