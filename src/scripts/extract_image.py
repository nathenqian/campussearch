#!/usr/bin/python
# -*- coding: utf-8 -*-

import cPickle as pkl
import os
import sys
import subprocess
import re
import progressbar
import json
from urlparse import urljoin
from multiprocessing.dummy import Pool as ThreadPool
from bs4 import BeautifulSoup

f = open('filelist.txt')
FN = f.readlines()
FN = [fn.strip() for fn in FN]
f.close()

st = 0
cnt = 0
wlst = ['ico', 'logo', 'sem.jpg', 'weixin', 'semEN.jpg', 'top', 'bg']
imgs = {}
def cut(fn):
    global cnt
    cnt += 1
    if cnt % 100 == 0:
        print '{}/{}'.format(cnt, len(FN)), 1. * cnt / len(FN)

    if not fn.endswith('html'):
        return
        
    f = open(fn)
    R = f.readlines()
    R = ' '.join([r.strip() for r in R])
    f.close()

    f = open(fn.replace('html', 'json'))
    base_url = json.load(f)['url']
    f.close()

    BS = BeautifulSoup(R, 'lxml')
    lst = BS.select('body img')
    if len(lst) == 0:
        return

    img = ''
    for loc in xrange(len(lst)):
        attrs = lst[loc].attrs
        if 'src' in attrs:
            i = attrs['src']
            if not (i.endswith('jpg') or i.endswith('png') or i.endswith('jpeg')):
                continue
            cont = False
            for x in wlst:
                if x in i:
                    cont = True
                    break
            if cont:
                continue
            img = i
            break
    if img == '':
        return
    img = urljoin(base_url, img)

    imgs[fn] = img
    

pool = ThreadPool(10)
#while st < len(FN):
pool.map(cut, FN)
    #print len(imgs), len(FN)

import codecs
f = codecs.open('img.json', 'w', 'utf8')
json.dump(imgs, f, ensure_ascii=False, indent=4)
f.close()
