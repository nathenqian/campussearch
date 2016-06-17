from json import loads
from threading import Thread
from time import sleep, time
from eventlet import monkey_patch, GreenPool
from eventlet import sleep as esleep
from select import select
from sys import stdin

from .url import Url, g_init, get_url2uid, get_uid2url
from .logger import Logger
from .green_downloader import GreenDownloader
from .file_manager import FileManager
from .url_extractor import extract_urls, is_save_url



class Crawler:
    def __init__(self, file_dir):
        self.thread_number = 8
        self.save_interval = 10
        # file_dir must be a absolute dir
        self.file_dir = file_dir
        """
        init gid -> url
        init unfinished queue 
        """
        self.file_manager = FileManager(self.file_dir)
        self.todo_url = []

        (uid, uid2url, url2uid) = self.file_manager.init(self.todo_url)
        print "init already finish %d webs" % (len(uid2url))
        g_init(uid, uid2url, url2uid)

        self.todo_map = {x : True for x in self.todo_url}

        self.doing_url = 0
        self.downloader = GreenDownloader(None, self.thread_number)
        # self.todo_url = ["http://www.126.com" for i in range(1000)]
        self.logger = Logger.create("log")

        self.save_list = []

        # monkey_patch()
        # print g_uid
        self.run()

    def append_todourl(self, url):
        if url not in self.todo_map:
            self.todo_url.append(url)
            self.todo_map[url] = True


    def run(self):
        start_time = time()
        next_save_time = start_time + self.save_interval
        finish_task_cnt = 0
        # try:
        while True:
            rlist, _, _ = select([stdin], [], [], 0.1)
            if rlist:
                stdin_input = stdin.readline()
                if stdin_input[0] == "i":
                    index = 1
                    while stdin_input[index] == ' ':
                        index += 1
                    self.append_todourl(stdin_input[index:])
                    # self.todo_url.append(stdin_input[index:])
                elif stdin_input[:4] == "exit":
                    while self.downloader.pool.running() != 0:
                        print "still have %d jobs pls wait" % (self.downloader.pool.running())
                        esleep()
                        sleep(1)
                    for i in self.save_list:
                        self.file_manager.save_url(i)
                    self.file_manager.save_global(get_uid2url())
                    self.file_manager.save_todo_list(self.todo_url)
                    return

            #dispatch
            while self.doing_url < self.thread_number:
                if len(self.todo_url) == 0:
                    break
                url = self.todo_url[0]
                # print "start doing " + url
                self.todo_url = self.todo_url[1:]
                self.downloader.input_queue.put({
                    "url" : url
                })
                self.doing_url += 1

            dispatcher = self.downloader.run()


            #backcall
            while not self.downloader.output_queue.empty():
                obj = self.downloader.output_queue.get()
                url = obj["url"]
                self.doing_url -= 1
                if url in get_url2uid():
                    continue
                finish_task_cnt += 1
                if "error_code" not in obj and obj["content"] is not None:
                    # correct
                    # decode_content = guess_encode(obj["content"])
                    # if decode_content is not None:
                    # obj["content"] = decode_content
                    url = Url.create(url, obj["content"], {}, "DONE")
                    # todo parse url and add new url
                    if is_save_url(url.url) is None:
                        new_url = extract_urls(url.url, url.content)
                        for i in new_url:
                            if i not in get_url2uid():
                                self.append_todourl(i)
                                # self.todo_url.append(i)
                    # else:
                        # can't guess decode
                        # url = Url.create(url, None, {}, "ERROR")
                else:
                    # error
                    url = Url.create(url, None, {}, "ERROR")
                # self.logger.debug(url.url + ' ' + url.status + ' ' + url.content)
                self.save_list.append(url)
            # print "todo = " + str(len(self.todo_url))

            #saving
            if time() >= next_save_time:
                self.logger.debug("save " + str(len(self.save_list)) + " file ")
                self.logger.debug("last " + str(time() - start_time) + " second ")
                self.logger.debug("last interval finish " + str(finish_task_cnt)  + " task")
                self.logger.debug("running = " + str(self.doing_url) + " " + str(self.downloader.pool.running()))
                finish_task_cnt = 0
                next_save_time += self.save_interval
                for i in self.save_list:
                    self.file_manager.save_url(i)
                self.file_manager.save_global(get_uid2url())
                self.file_manager.save_todo_list(self.todo_url)
                self.save_list = []

        # except KeyboardInterrupt:
        #     pass
        # except Exception, e:
        #     print str(e)



