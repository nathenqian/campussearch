from .url import Url
from .url_extractor import is_save_url
from json import dumps, loads
from os import mkdir
from os.path import exists, join

class FileManager:
    def __init__(self, file_dir):
        self.file_dir = file_dir

    def init(self, unfinished):
        g_uid = 0
        g_uid2url = {}
        g_url2uid = {}
        try:        
            with open(join(self.file_dir, "gid_url.txt")) as f:
                buff_dict = loads(f.read())
                for i in buff_dict:
                    g_uid = max(g_uid, int(i))
                    g_uid2url[i] = buff_dict[i]
                    g_url2uid[buff_dict[i]] = i
                g_uid += 1
            print "load gid success total %d webs" % (g_uid)
        except Exception, e:
            g_uid = 0
            g_uid2url = {}
            g_url2uid = {}
            print "load gid_url failed.\n"  + str(e)
        try:
            with open(join(self.file_dir, "unfinished.txt")) as f:
                buff_list = loads(f.read())
                for url in buff_list:
                    unfinished.append(url)
            print "load unfinished task success"
        except Exception, e:
            unfinished = []
            print "load unfinished task failed.\n" + str(e)
        return (g_uid, g_uid2url, g_url2uid)

    def save_url(self, url):
        uid = url.uid
        folder = str(uid / 1000)
        if not exists(join(self.file_dir, folder)):
            mkdir(join(self.file_dir, folder))
        path = join(join(self.file_dir, folder), str(uid) + ".json")
        with open(path, "w") as f:
            f.write(url.toJsonWithoutContent())
        if is_save_url(url.url) is None:
            path = join(join(self.file_dir, folder), str(uid) + ".html")
        else:
            path = join(join(self.file_dir, folder), str(uid) + "." + is_save_url(url.url))
        if url.content is not None:
            with open(path, "w") as f:
                f.write(url.content)


    def save_global(self, g_uid2url):
        with open(join(self.file_dir, "gid_url.txt"), "w") as f:        
            f.write(dumps(g_uid2url, indent = 4, sort_keys = True))

    def save_todo_list(self, todo_list):
        with open(join(self.file_dir, "unfinished.txt"), "w") as f:
            f.write(dumps(todo_list, indent = 4))