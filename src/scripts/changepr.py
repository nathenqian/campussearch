from json import loads,dumps
from math import log
with open("results.json", "r") as f:
	a = f.read()
b = loads(a)
for i in b:
	b[i] = (log(b[i], 2) + 22) * 1.5
with open("results.json3", "w") as f:
	f.write(dumps(b))
