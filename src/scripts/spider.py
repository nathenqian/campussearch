from crawler.crawler import Crawler
from eventlet import monkey_patch
from os import getcwd
from os.path import join
if __name__ == "__main__":
    # monkey_patch()
    crawler = Crawler(join(getcwd(), "file"))
    