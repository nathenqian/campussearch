from math import log, exp
from json import dumps
ret = {}
with open("results.txt", "r") as f:
        l = f.readlines()
        l = [x.strip().split() for x in l]
        pr = zip(*l)[0]
        pr = [float(x) for x in pr]
	file = zip(*l)[2]
	for i in range(len(file)):
		ret[file[i]] = pr[i]

ret["pdf"] = 9.71549208564e-05;

with open("results.json", "w") as f:
	f.write(dumps(ret))

