# -*- coding: utf-8 -*-

import requests
from lxml import etree
import datetime
import redis
import sys
reload(sys)
sys.setdefaultencoding('utf8')


# 组装请求
url = 'https://s.weibo.com/top/summary?cate=realtimehot'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url,headers=headers)
date = datetime.datetime.now().strftime("%Y%m%d")

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

# 判断对象是否为纯数字
def isLetter(params):
    for ch in params.decode('utf-8'):
        if u'\u4e00' <= ch <= u'\u9fff':
            return True
    return False

# 提取数据
selector = etree.HTML(response.text)
lists = selector.xpath("//div[@class='data']/table/tbody/tr")
i = 0
for video in lists:
    title = video.xpath("./td[@class='td-02']/a/text()")
    hot = video.xpath("./td[@class='td-02']/span/text()")
    uri = video.xpath("./td[@class='td-02']/a/@href")
    if ("javascript:void(0);" in uri):
        uri = video.xpath("./td[@class='td-02']/a/@href_to")
    link = 'https://s.weibo.com/{}'.format(uri[0])

    if (len(hot) != 0):
        mid = title[0]

        # 判断是否为纯字母
        if (isLetter(mid) is False):
            title = str(mid)

        # 写入redis
        i = i + 1
        mid = "title$$" + title[0] + '##' + "link$$" + link + '##' + "hot$$" + hot[0]
        con.zadd('weibo_{}'.format(date), {mid: i})

        # 读取redis
        # title = con.zrange('36kr_20201211', 0, -1, desc=False)
        # print (title)
        # break