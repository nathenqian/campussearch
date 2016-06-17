import eventlet
from eventlet.green import urllib2
from time import sleep
from .logger import Logger

class GreenDownloader():
    def __init__(self, proxy, thread_number):
        self.pool = eventlet.GreenPool(thread_number)
        self.input_queue = eventlet.Queue()
        self.output_queue = eventlet.Queue()
        self.proxy = proxy
        self.logger = Logger.create("log")
        self.timeout = 10


    def get_data(self, argv):
        # argv = {"urls" : [], "worker" : , }
        content = None
        error_code = None
        self.logger.debug("start fetch " + argv["url"])
        try:
            url = argv["url"]
            try:
                with eventlet.Timeout(self.timeout, False):
                    headers = {
                        "User-Agent":"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1"
                    }
                    if self.proxy is None:
                        req = urllib2.Request(url, headers = headers)
                        res = urllib2.urlopen(req)
                        content = res.read()
                    else:
                        proxy_handler = urllib2.ProxyHandler(self.proxy)
                        opener = urllib2.build_opener(proxy_handler)
                        header_list = []
                        for header in headers:
                            header_list.append((header, headers[header]))
                        opener.addheaders = header_list
                        res = opener.open(url)
                        content = res.read()
            except urllib2.HTTPError, e:
                raise Exception(e.code)
            except urllib2.URLError, e:
                raise Exception("URLError")
            except Exception, e:
                raise Exception(str(e))
        except Exception, e:
            argv["error_code"] = str(e)
        argv["content"] = content
        self.output_queue.put(argv)

    def run(self):
        dispatch = False
        # eventlet.monkey_patch()
        while not self.input_queue.empty():
            # print "add obj"
            obj = self.input_queue.get()
            # print obj
            self.pool.spawn_n(self.get_data, obj)
            # print self.pool.running()
            dispatch = True
        # print self.pool.running()
        eventlet.sleep()
        return dispatch

