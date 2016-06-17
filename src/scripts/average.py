from math import log, exp
with open("results.txt", "r") as f:
        l = f.readlines()
        l = [x.strip().split() for x in l]
        pr = zip(*l)[0]
        pr = [float(x) for x in pr]
        s = sum(pr)
	x = sorted(pr, reverse = True)
	r = 0
	for i in range(len(x)):
		r += x[i]
		if r > s / 2:
			print x[i]
			break
