from logging import getLogger, DEBUG, Formatter, basicConfig, FileHandler, Formatter, StreamHandler
logger_map = {}

class Logger:
    @classmethod
    def create(cls, name):
        if name in logger_map:
            return logger_map[name]
        logger = getLogger(name)
        logger.setLevel(DEBUG)
        formatter = Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        fh = FileHandler(name + ".log")
        fh.setLevel(DEBUG)
        fh.setFormatter(formatter)
        ch = StreamHandler()
        ch.setLevel(DEBUG)
        ch.setFormatter(formatter)
        logger.addHandler(fh)
        logger.addHandler(ch)
        logger.debug("Start logger %s", name)
        logger_map[name] = logger
        return logger
