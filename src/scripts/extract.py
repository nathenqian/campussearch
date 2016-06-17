#!/usr/bin/python
# -*- coding: utf-8 -*-

import cPickle as pkl
import os
import sys
import subprocess
import re
import progressbar
from multiprocessing.dummy import Pool as ThreadPool
from bs4 import BeautifulSoup

META_CMD = 'java -jar tika.jar --metadata {}'
TEXT_CMD = 'java -jar tika.jar --text {}'
EXPR = u'[　\t]'

f = open('filelist.txt')
FN = f.readlines()
FN = [fn.strip() for fn in FN]
f.close()

st = 0
cnt = 0
docs = {}
def cut(fn):
	doc = {}

	if fn.endswith('html'):
		f = open(fn)
		R = f.readlines()
		R = ' '.join([r.strip() for r in R])
		f.close()

		BS = BeautifulSoup(R, 'lxml')
		dic = {'h1': [], 'h2': [], 'h3': [], 'a': [], 'title': []}
		dic['text'] = [t.get_text() for t in BS.select('body p')]
		for tag in dic.keys():
			for r in BS.select(tag):
				dic[tag].append(r.get_text())

		for k, v in dic.iteritems():
			doc[k] = ' '.join(v)

	else:
		f = os.popen(META_CMD.format(fn))
		M = f.readlines()
		M = [m for m in M if m.startswith('dc:title') or m.startswith('meta:author')]
		M = [m.strip().replace('dc:', '').replace('meta:', '').split(':') for m in M]
		M = [(m[0], ' '.join(m[1:])) for m in M]
		for k, v in M:
			doc[k] = v
		f.close()

		f = os.popen(TEXT_CMD.format(fn))
		T = f.readlines()
		T = ' '.join([' '.join(t.strip().split()) for t in T])
		doc['text'] = T
		f.close()

	for k, v in doc.iteritems():
		if isinstance(v, str):
			v = v.decode('utf8')
		v = re.sub(EXPR, u'', v)
		doc[k] = v

	docs[fn] = doc

	global cnt
	cnt += 1
	if cnt % 10 == 0:
		print '{}/{}'.format(cnt, len(FN)), 1. * cnt / len(FN)

pool = ThreadPool(10)
while st < len(FN):
	pool.map(cut, FN[st:st+10000])
	print len(docs), len(FN)

	import json
	import codecs
	f = codecs.open('index_{}.json'.format(st), 'w', 'utf8')
	json.dump(docs, f, ensure_ascii=False, indent=4)
	f.close()

	st += 10000
	docs = {}
