#!/usr/bin/python
# -*- coding: utf-8 -*-

import cPickle as pkl
import os
import sys
import json
import re
from urlparse import urljoin
from multiprocessing.dummy import Pool as ThreadPool
from bs4 import BeautifulSoup


def _filter(orig_url, lst):

    def fix(url):
        if not url.startswith('http'):
            if  not url.startswith('/'):
                return None
            url = urljoin(orig_url, url)
        url = url.split('#')[0]
        return url

    lst = [fix(x) for x in lst]
    lst = [x for x in lst if x is not None]
    return lst


def extract_urls(url, html):
    soup = BeautifulSoup(html, 'lxml')
    url_lst = []
    for a in soup.findAll('a'):
        l = a.get('href')
        if l is not None:
            # print l
            url_lst.append(l)
    return _filter(url, url_lst)


f = open('filelist.txt')
FN = f.readlines()
FN = [fn.strip() for fn in FN]
f.close()

st = 0
cnt = 0
docs = {}
names = {}
links = {}
def cut(fn):
    if fn.endswith('html'):
        f = open(fn)
        R = f.readlines()
        R = '\n'.join([r.strip() for r in R])
        f.close()

        dat = json.load(open(fn.replace('html', 'json')))
        url = dat['url']
        uid = dat['uid']
        docs[url] = uid
        names[uid] = fn

        urls = extract_urls(url, R)
        links[uid] = urls

    global cnt
    cnt += 1
    if cnt % 10 == 0:
        print '{}/{}'.format(cnt, len(FN)), 1. * cnt / len(FN)


pool = ThreadPool(10)
while st < len(FN):
    pool.map(cut, FN[st:st+10000])
    print len(docs), len(FN)
    import gc
    gc.collect()
    st += 10000

import json
import codecs
f = open('node.map', 'w')
for k, v in docs.iteritems():
    print >> f, '{}-->{}'.format(names[docs[k]], v)
f.close()

for k, lst in links.iteritems():
    lst = [docs[url] for url in lst if url in docs and docs[url] != k]
    links[k] = lst

f = open('thu.graph', 'w')
for k, v in links.iteritems():
    v = [str(x) for x in v]
    print >> f, '{}:{}'.format(k, ','.join(v))
f.close()
