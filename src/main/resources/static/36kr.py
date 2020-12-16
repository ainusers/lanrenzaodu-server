# -*- coding: utf-8 -*-

import requests
import datetime
import redis
from lxml import etree
import sys
reload(sys)
sys.setdefaultencoding('utf8')


# 组装请求
url_date = datetime.datetime.now().strftime("%Y-%m-%d")
url = 'https://36kr.com/hot-list/renqi/{}/1'.format(url_date)
headers = {
            "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url,headers=headers)
date = datetime.datetime.now().strftime("%Y%m%d")

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)


# 提取数据
selector = etree.HTML(response.text)
lists = selector.xpath("//div[@class='list-wrapper']/div[@class='article-list']/div")
i = 0
for video in lists:
    title = video.xpath("./div[@class='kr-flow-article-item']/div[@class='kr-shadow-wrapper']/div[@class='kr-shadow-content']/div[@class='article-item-info clearfloat']/p/a/text()")
    uri = video.xpath("./div[@class='kr-flow-article-item']/div[@class='kr-shadow-wrapper']/div[@class='kr-shadow-content']/div[@class='article-item-info clearfloat']/p/a/@href")
    hot = video.xpath("./div[@class='kr-flow-article-item']/div[@class='kr-shadow-wrapper']/div[@class='kr-shadow-content']/div[@class='article-item-info clearfloat']/div[@class='kr-flow-bar']/span/span/text()")
    link = 'https://36kr.com{}'.format(uri[0])

    # 写入redis
    i = i + 1
    mid = "title$$" + title[0] + '##' + "link$$" + link + '##' + "hot$$" + hot[0]
    s = str(mid)
    con.zadd('36kr_{}'.format(date), {s:i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break