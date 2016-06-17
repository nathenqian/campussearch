from json import dumps
from threading import Lock

g_uid = 0
g_uid2url = {}
g_url2uid = {}
g_uid_mutex = Lock()

def get_uid2url():
    return g_uid2url

def get_url2uid():
    return g_url2uid


def g_init(uid, uid2url, url2uid):
    global g_uid, g_uid2url, g_url2uid
    g_uid = uid
    g_uid2url = uid2url
    g_url2uid = url2uid


class Url:
    def __init__(self):
        self.content = ""
        self.url = ""
        self.uid = -1
        self.parse = {}
        self.status = ""

    def toDictWithoutContent(self):
        return {
            "url" : self.url,
            "uid" : self.uid,
            "parse" : self.parse,
            "status" : self.status
        }

    def toJsonWithoutContent(self):
        return dumps(self.toDictWithoutContent(), indent = 4)

    def parseDict(self, dic):
        self.content = dic["content"]
        self.url = dic["url"]
        self.uid = dic["uid"]
        self.parse = dic["parse"]
        self.status = dic["status"]
        return self

    @classmethod
    def create(cls, url, content, parse, status):
        global g_uid, g_uid_mutex, g_uid2url, g_url2uid
        g_uid_mutex.acquire()
        uid = g_uid
        g_uid += 1
        g_uid_mutex.release()

        url_ = Url().parseDict({
            "content" : content,
            "url" : url,
            "uid" : uid,
            "parse" : parse,
            "status" : status
        })
        g_uid2url[uid] = url
        g_url2uid[url] = uid
        return url_

