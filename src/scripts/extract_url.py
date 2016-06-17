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
imgs = {}
def cut(fn):
    global cnt
    cnt += 1
    if cnt % 100 == 0:
        print '{}/{}'.format(cnt, len(FN)), 1. * cnt / len(FN)

    lst = ['html', 'xlsx', 'xls', 'pptx', 'ppt', 'docx', 'doc', 'pdf']
    ofn = fn
    for suffix in lst:
        fn = fn.replace(suffix, 'json')
        fn = fn.replace(suffix.upper(), 'json')
    f = open(fn)
    try:
        base_url = json.load(f)['url']
    except:
        print fn
    f.close()

    imgs[ofn] = base_url
    

pool = ThreadPool(1)
#while st < len(FN):
map(cut, FN)
    #print len(imgs), len(FN)

import codecs
f = codecs.open('url.json', 'w', 'utf8')
json.dump(imgs, f, ensure_ascii=False, indent=4)
f.close()
